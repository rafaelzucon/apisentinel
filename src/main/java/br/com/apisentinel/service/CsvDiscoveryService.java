package br.com.apisentinel.service;

import br.com.apisentinel.client.gitlab.GitLabClient;
import br.com.apisentinel.client.gitlab.Project;
import br.com.apisentinel.client.gitlab.Tag;
import br.com.apisentinel.client.governance.GenericArtifactResponse;
import br.com.apisentinel.client.governance.GovernanceClient;
import br.com.apisentinel.client.governance.RestServiceArtifact;
import br.com.apisentinel.client.governance.RestServiceArtifactRequest;
import br.com.apisentinel.client.governance.AssociationRequest;
import br.com.apisentinel.config.ApiSentinelProperties;
import br.com.apisentinel.domain.AssetGW;
import br.com.apisentinel.domain.DiscoveryLog;
import br.com.apisentinel.domain.InconsistencyLog;
import br.com.apisentinel.dto.CsvAssetRow;
import br.com.apisentinel.repository.AssetGWRepository;
import br.com.apisentinel.repository.DiscoveryLogRepository;
import br.com.apisentinel.repository.InconsistencyLogRepository;
import br.com.apisentinel.util.ChangeHashUtil;
import br.com.apisentinel.util.NameNormalizer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class CsvDiscoveryService {
    private final GovernanceClient governance;
    private final GitLabClient gitlab;
    private final AssetGWRepository assetGWRepository;
    private final DiscoveryLogRepository discoveryRepo;
    private final InconsistencyLogRepository inconsistencyRepo;
    private final ApiSentinelProperties props;

    public CsvDiscoveryService(GovernanceClient governance,
                               GitLabClient gitlab,
                               AssetGWRepository assetGWRepository,
                               DiscoveryLogRepository discoveryRepo,
                               InconsistencyLogRepository inconsistencyRepo,
                               ApiSentinelProperties props) {
        this.governance = governance;
        this.gitlab = gitlab;
        this.assetGWRepository = assetGWRepository;
        this.discoveryRepo = discoveryRepo;
        this.inconsistencyRepo = inconsistencyRepo;
        this.props = props;
    }

    public void process(CsvAssetRow row) {
        if (row.getName() == null || row.getName().isBlank()) {
            saveInconsistency("NAME_REQUIRED", "name");
            return;
        }

        String context = row.getContext() != null ? row.getContext() : "";
        String version = row.getVersion() != null ? row.getVersion() : "";

        if ("ON_PREMISE".equalsIgnoreCase(row.getHost()) || "AWS".equalsIgnoreCase(row.getHost())) {
            GenericArtifactResponse resp = governance
                    .findByName(row.getType().toLowerCase() + "s", row.getName())
                    .onErrorResume(e -> Mono.empty())
                    .block();
            if (resp != null && resp.getAssets() != null && !resp.getAssets().isEmpty()) {
                var r = resp.getAssets().get(0);
                version = r.getVersion();
                context = r.getContext();
            } else {
                List<Project> projects = gitlab.findProjectByName(row.getName())
                        .onErrorResume(e -> Mono.just(List.of()))
                        .block();
                if (projects != null && !projects.isEmpty()) {
                    Optional<Project> exact = projects.stream()
                            .filter(p -> row.getName().equals(p.getName()))
                            .findFirst();
                    if (exact.isPresent()) {
                        if (context.isBlank() && exact.get().getNamespace() != null)
                            context = "/" + exact.get().getNamespace().getName();
                        List<Tag> tags = gitlab.getTagsByProjectId(exact.get().getId())
                                .onErrorResume(e -> Mono.just(List.of()))
                                .block();
                        if (tags != null && !tags.isEmpty() && (version == null || version.isBlank())) {
                            version = tags.get(0).getName();
                            if (version != null && version.contains(".")) version = version.split("\\.")[0];
                        }
                    } else {
                        saveInconsistency("GITLAB_MULTIPLE_OR_NOT_FOUND", row.getName());
                    }
                } else {
                    saveInconsistency("GITLAB_PROJECT_NOT_FOUND", row.getName());
                }
            }
        }

        String normalizedName = NameNormalizer.normalize(
                row.getName(),
                row.getFriendlyName(),
                row.getApiGatewayExposure(),
                row.getAccessScope()
        );

        if (version == null || version.isBlank()) {
            saveInconsistency("VERSION_REQUIRED", normalizedName + ";version");
            return;
        }
        if (context == null || context.isBlank()) {
            saveInconsistency("CONTEXT_REQUIRED", normalizedName + ";context");
            return;
        }

        RestServiceArtifactRequest req = new RestServiceArtifactRequest();
        req.type = row.getType() != null ? row.getType().toLowerCase() : "restservice";
        req.name = normalizedName;
        req.friendlyName = row.getFriendlyName();
        req.context = context;
        req.version = version;
        req.description = row.getDescription();
        req.application = row.getApplication();

        req.accessScope = row.getAccessScope();
        req.host = row.getHost();
        req.apiGatewayExposure = row.getApiGatewayExposure();
        req.communicationChannel = row.getCommunicationChannel();
        req.communicationProtocol = row.getCommunicationProtocol();
        req.messageFormat = row.getMessageFormats();

        req.authenticationType = row.getAuthenticationType();
        req.authorizationType = row.getAuthorizationType();
        req.secureCommunicationChannel = row.getSecureCommunicationChannel();
        req.credentialVaultLocation = row.getCredentialVaultLocation();
        req.securityTestType = row.getSecurityTestType();
        req.usesSensitiveData = row.getUsesSensitiveData();

        if (row.getRateLimitTps() != null && !row.getRateLimitTps().isBlank()) {
            try {
                req.rateLimitTps = Integer.parseInt(row.getRateLimitTps());
            } catch (Exception ignored) {
            }
        }

        String typePlural = req.type + "s";
        GenericArtifactResponse existing = governance.findByNameAndVersion(typePlural, req.name, req.version).onErrorResume(e -> Mono.empty()).block();
        if (existing == null || existing.getAssets() == null || existing.getAssets().isEmpty()) {
            var created = governance.create(req.type, req).block();
            saveDiscovery("CREATED", req.type, created != null ? created.getId() : "", req.name, req.version, req.context, "CSV");
        } else {
            RestServiceArtifact r = existing.getAssets().get(0);
            governance.update(req.type, r.getId(), req).block();
            saveDiscovery("UPDATED", req.type, r.getId(), req.name, req.version, req.context, "CSV");
        }

        if ("EXTERNAL".equalsIgnoreCase(req.accessScope)) {
            String exposedName = req.name.endsWith("-gtw") ? req.name.substring(0, req.name.length() - 4) : req.name;
            GenericArtifactResponse exposed = governance.findByNameAndVersion(typePlural, exposedName, req.version).onErrorResume(e -> Mono.empty()).block();
            if (exposed != null && exposed.getAssets() != null && !exposed.getAssets().isEmpty()) {
                RestServiceArtifact base = exposed.getAssets().get(0);
                governance.createAssociation("/_system/governance/trunk/" + typePlural + "/" + req.version + "/" + req.name,
                        new AssociationRequest("/_system/governance/trunk/" + typePlural + "/" + base.getVersion() + "/" + base.getName(), "EXPOSES")).block();
                saveDiscovery("ASSOCIATION_CREATED", req.type, base.getId(), req.name, req.version, req.context, "CSV");
            } else {
                saveDiscovery("EXPOSED_ASSET_NOT_FOUND", req.type, "", exposedName, req.version, req.context, "CSV");
            }
        }

        upsertDb(req);
    }

    private void upsertDb(RestServiceArtifactRequest req) {
        AssetGW entity = assetGWRepository.findByNameAndVersion(req.name, req.version).orElseGet(AssetGW::new);
        final String oldHash = entity.getChangeHash();
        entity.setName(req.name);
        entity.setFriendlyName(req.friendlyName);
        entity.setVersion(req.version);
        entity.setContext(req.context);
        entity.setDescription(req.description);
        entity.setGwExposure(req.apiGatewayExposure);
        entity.setGwVersion(req.version);
        entity.setUpdatedIn(new java.sql.Timestamp(System.currentTimeMillis()));
        if (entity.getCreatedIn() == null) {
            entity.setCreatedIn(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        // calcula o novo hash com os campos atuais
        byte[] key = props.getSync().getHmacKey().getBytes(StandardCharsets.UTF_8);
        String newHash = ChangeHashUtil.compute(
                key,
                entity.getName(),
                entity.getGwVersion(),
                entity.getContext(),
                entity.getDescription()
        );
        if (oldHash != null && !oldHash.equals(newHash)) {
            // se já existia hash e mudou, registramos delta
            saveDiscovery(
                    "HASH_CHANGED",
                    req.type != null ? req.type : "restservice",
                    "", // sem governanceId no CSV neste ponto
                    entity.getName(),
                    entity.getVersion(),
                    entity.getContext(),
                    "CSV"
            );
        }
        entity.setChangeHash(newHash);
        assetGWRepository.save(entity);
    }

    private void saveDiscovery(String status, String type, String govId, String name, String version, String context, String source) {
        DiscoveryLog log = new DiscoveryLog();
        log.setStatus(status);
        log.setType(type);
        log.setGovernanceId(govId);
        log.setName(name);
        log.setVersion(version);
        log.setContext(context);
        log.setSource(source);
        discoveryRepo.save(log);
    }

    private void saveInconsistency(String code, String payload) {
        InconsistencyLog inc = new InconsistencyLog();
        inc.setCode(code);
        inc.setPayload(payload);
        inconsistencyRepo.save(inc);
    }
}
