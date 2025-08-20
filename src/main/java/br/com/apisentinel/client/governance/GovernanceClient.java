package br.com.apisentinel.client.governance;

import br.com.apisentinel.config.ApiSentinelProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GovernanceClient {
    private final WebClient webClient;
    private final ApiSentinelProperties props;

    public GovernanceClient(WebClient webClient, ApiSentinelProperties props) {
        this.webClient = webClient;
        this.props = props;
    }

    public Mono<GenericArtifactResponse> findByNameAndVersion(String typePlural, String name, String version) {
        String url = props.getGovernance().getBaseUrl() + "/governance/" + typePlural + "?name=" + name + "&version=" + version;
        return webClient.get().uri(url).retrieve().bodyToMono(GenericArtifactResponse.class);
    }

    public Mono<GenericArtifactResponse> findByName(String typePlural, String name) {
        String url = props.getGovernance().getBaseUrl() + "/governance/" + typePlural + "?name=" + name;
        return webClient.get().uri(url).retrieve().bodyToMono(GenericArtifactResponse.class);
    }

    public Mono<PublisherNewGenericArtifactResponse> create(String type, RestServiceArtifactRequest req) {
        String url = props.getGovernance().getBaseUrl() + "/publisher/assets?type=" + type;
        return webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON).bodyValue(req).retrieve().bodyToMono(PublisherNewGenericArtifactResponse.class);
    }

    public Mono<Void> update(String type, String id, RestServiceArtifactRequest req) {
        String url = props.getGovernance().getBaseUrl() + "/publisher/assets/" + id + "?type=" + type;
        return webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON).bodyValue(req).retrieve().bodyToMono(Void.class);
    }

    public Mono<GenericArtifactResponse> createAssociation(String path, AssociationRequest assoc) {
        String url = props.getGovernance().getBaseUrl() + "/resource/associations?path=" + path;
        return webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON).bodyValue(java.util.List.of(assoc)).retrieve().bodyToMono(GenericArtifactResponse.class);
    }
}
