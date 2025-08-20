package br.com.apisentinel.service;

import br.com.apisentinel.client.gitlab.GitLabClient;
import br.com.apisentinel.client.gitlab.Project;
import br.com.apisentinel.client.gitlab.Tag;
import br.com.apisentinel.client.governance.GenericArtifactResponse;
import br.com.apisentinel.client.governance.GovernanceClient;
import br.com.apisentinel.client.governance.PublisherNewGenericArtifactResponse;
import br.com.apisentinel.client.governance.RestServiceArtifactRequest;
import br.com.apisentinel.client.apisentinel.ApiManagerClient;
import br.com.apisentinel.client.apisentinel.ApiSentinelApi;
import br.com.apisentinel.config.ApiSentinelProperties;
import br.com.apisentinel.domain.AssetGW;
import br.com.apisentinel.domain.DiscoveryLog;
import br.com.apisentinel.repository.AssetGWRepository;
import br.com.apisentinel.repository.DiscoveryLogRepository;
import br.com.apisentinel.util.ChangeHashUtil;
import br.com.apisentinel.util.NameNormalizer;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class ApiGatewayDiscoveryService {
    private final ApiManagerClient apiManagerClient;
    private final AssetGWRepository repository;
    private final ApiSentinelProperties props;
    private final GovernanceClient governance;
    private final GitLabClient gitlab;
    private final DiscoveryLogRepository discoveryRepo;

    public ApiGatewayDiscoveryService(ApiManagerClient apiManagerClient,
                                      AssetGWRepository repository,
                                      ApiSentinelProperties props,
                                      GovernanceClient governance,
                                      GitLabClient gitlab,
                                      DiscoveryLogRepository discoveryRepo) {
        this.apiManagerClient = apiManagerClient;
        this.repository = repository;
        this.props = props;
        this.governance = governance;
        this.gitlab = gitlab;
        this.discoveryRepo = discoveryRepo;
    }

    private static String hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public void syncFromExternalApi() {
        var list = apiManagerClient.getApis().block();
        if (list == null) return;
        list.forEach(this::upsert);
    }

    private void upsert(ApiSentinelApi api) {
        // 1) Dados do ExternalApi
        String normalizedName = NameNormalizer.toKebab(api.getName());
        String majorVersion = api.getVersion() != null && !api.getVersion().isBlank()
                ? api.getVersion().split("\\.")[0] : null;
        String context = api.getBasePath();

        // 2) Fallback GitLab (se faltarem versão/contexto)
        if (isBlank(majorVersion) || isBlank(context)) {
            try {
                List<Project> projects = gitlab.findProjectByName(api.getName())
                        .onErrorResume(e -> reactor.core.publisher.Mono.just(List.of()))
                        .block();
                if (projects != null && !projects.isEmpty()) {
                    Optional<Project> exact = projects.stream()
                            .filter(p -> api.getName().equals(p.getName()))
                            .findFirst();
                    if (exact.isPresent()) {
                        Project p = exact.get();
                        if (isBlank(context) && p.getNamespace() != null && p.getNamespace().getName() != null) {
                            context = "/" + p.getNamespace().getName();
                        }
                        if (isBlank(majorVersion)) {
                            List<Tag> tags = gitlab.getTagsByProjectId(p.getId())
                                    .onErrorResume(e -> reactor.core.publisher.Mono.just(List.of()))
                                    .block();
                            if (tags != null && !tags.isEmpty()) {
                                String v = tags.get(0).getName();
                                if (v != null) majorVersion = v.contains(".") ? v.split("\\.")[0] : v;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 3) Calcula changeHash com o payload significativo
        byte[] key = props.getSync().getHmacKey().getBytes(StandardCharsets.UTF_8);
        String newChangeHash = ChangeHashUtil.compute(
                key,
                normalizedName,
                api.getVersion(),
                context,
                api.getDescription()
        );

        // 4) Busca por gw_asset_id (Long) e decide persist/merge
        Long gwId = api.getId();
        AssetGW entity = repository.findByGwAssetId(gwId).orElse(null);

        if (entity.getId() == null) {
            entity.setCreatedIn(Timestamp.valueOf(LocalDateTime.now()));
            entity.setGoverned(false);

            entity.setGwAssetId(gwId);
            entity.setName(normalizedName);
            entity.setFriendlyName(api.getName());
            entity.setVersion(majorVersion);
            entity.setContext(context);
            entity.setDescription(api.getDescription());
            if (api.getApiResponsible() != null && api.getApiResponsible().getUser() != null) {
                String owner = api.getApiResponsible().getUser().getName();
                if (owner != null && owner.contains("@")) owner = null;
                entity.setOwner(owner);
                entity.setOwnerEmail(api.getApiResponsible().getUser().getEmail());
            }
            entity.setGwVersion(api.getVersion());
            entity.setGwExposure("SENSEDIA");
            if (api.getCreationDate() != null) {
                entity.setGwCreatedIn(Timestamp.from(Instant.ofEpochMilli(api.getCreationDate()).atZone(ZoneId.systemDefault()).toInstant()));
            }
            entity.setUpdatedIn(Timestamp.valueOf(LocalDateTime.now()));
            final String oldHash = entity.getChangeHash();
            if (oldHash != null && !oldHash.equals(newChangeHash)) {
                // se houve delta de dados: registrar DiscoveryLog
                saveDiscovery(
                        "HASH_CHANGED",
                        "restservice",
                        entity.getGwAssetId() != null ? entity.getGwAssetId().toString() : "",
                        entity.getName(),
                        entity.getVersion(),
                        entity.getContext(),
                        "SENSEDIA"
                );
            }
            entity.setChangeHash(newChangeHash);

            repository.save(entity);
        } else {
            // === merge/update (preserva id/createdIn/governed) ===
            String oldHash = entity.getChangeHash();

            // Atualiza sempre campos mutáveis
            entity.setName(normalizedName);
            entity.setFriendlyName(api.getName());
            entity.setVersion(majorVersion);
            entity.setContext(context);
            entity.setDescription(api.getDescription());
            if (api.getApiResponsible() != null && api.getApiResponsible().getUser() != null) {
                String owner = api.getApiResponsible().getUser().getName();
                if (owner != null && owner.contains("@")) owner = null;
                entity.setOwner(owner);
                entity.setOwnerEmail(api.getApiResponsible().getUser().getEmail());
            }
            entity.setGwVersion(api.getVersion());
            entity.setGwExposure("SENSEDIA");
            if (api.getCreationDate() != null) {
                entity.setGwCreatedIn(Timestamp.from(Instant.ofEpochMilli(api.getCreationDate()).atZone(ZoneId.systemDefault()).toInstant()));
            }
            entity.setUpdatedIn(Timestamp.valueOf(LocalDateTime.now()));

            // Se o hash mudou, aplica o novo (indicando delta de dados)
            if (oldHash == null || !oldHash.equals(newChangeHash)) {
                entity.setChangeHash(newChangeHash);
            }

            repository.save(entity);
        }

        // 5) Sync com Governança + log de descoberta
        syncGovernanceFromApi(api, normalizedName, majorVersion, context);
    }


    private void syncGovernanceFromApi(ApiSentinelApi api, String normalizedName, String majorVersion, String context) {
        RestServiceArtifactRequest req = new RestServiceArtifactRequest();
        req.type = "restservice";
        String gatewayName = normalizedName != null ? normalizedName : NameNormalizer.toKebab(api.getName());
        if (gatewayName != null && !gatewayName.endsWith("-gtw")) gatewayName = gatewayName + "-gtw";

        req.name = gatewayName;
        req.friendlyName = api.getName();
        req.context = context;
        req.version = majorVersion;
        req.description = api.getDescription();
        req.apiGatewayExposure = "SENSEDIA";
        req.accessScope = "EXTERNAL";

        String typePlural = req.type + "s";
        try {
            GenericArtifactResponse existing = governance.findByNameAndVersion(typePlural, req.name, req.version).block();
            if (existing == null || existing.getAssets() == null || existing.getAssets().isEmpty()) {
                PublisherNewGenericArtifactResponse created = governance.create(req.type, req).block();
                saveDiscovery("CREATED", req.type, created != null ? created.getId() : "", req.name, req.version, req.context, "SENSEDIA");
            } else {
                String id = existing.getAssets().get(0).getId();
                governance.update(req.type, id, req).block();
                saveDiscovery("UPDATED", req.type, id, req.name, req.version, req.context, "SENSEDIA");
            }
        } catch (Exception ignored) {
            // manter processo resiliente
        }
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
}
