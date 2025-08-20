package br.com.apisentinel.client.gitlab;

import br.com.apisentinel.config.ApiSentinelProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GitLabClient {
    private final WebClient webClient;
    private final ApiSentinelProperties props;

    public GitLabClient(WebClient webClient, ApiSentinelProperties props) {
        this.webClient = webClient;
        this.props = props;
    }

    public Mono<List<Project>> findProjectByName(String name) {
        String url = props.getGitlab().getBaseUrl() + "/projects?search=" + name;
        return webClient.get()
                .uri(url)
                .header("PRIVATE-TOKEN", props.getGitlab().getToken())
                .retrieve()
                .bodyToFlux(Project.class)
                .collectList();
    }

    public Mono<List<Tag>> getTagsByProjectId(Long projectId) {
        String url = props.getGitlab().getBaseUrl() + "/projects/" + projectId + "/repository/tags";
        return webClient.get()
                .uri(url)
                .header("PRIVATE-TOKEN", props.getGitlab().getToken())
                .retrieve()
                .bodyToFlux(Tag.class)
                .collectList();
    }
}
