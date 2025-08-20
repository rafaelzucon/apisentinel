package br.com.apisentinel.client.apisentinel;

import br.com.apisentinel.config.ApiSentinelProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ApiManagerClient {
    private final WebClient webClient;
    private final ApiSentinelProperties properties;

    public ApiManagerClient(WebClient webClient, ApiSentinelProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public Mono<List<ApiSentinelApi>> getApis() {
        String url = properties.getExternalApi().getBaseUrl() + "/apis?internalAPI=false&apiType=REST&lastVersion=true";
        return webClient.get().uri(url).header("ExternalApi-Auth", properties.getExternalApi().getToken())
                .retrieve().bodyToFlux(ApiSentinelApi.class).collectList();
    }
}
