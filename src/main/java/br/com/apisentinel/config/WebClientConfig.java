package br.com.apisentinel.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
    private final ApiSentinelProperties props;

    public WebClientConfig(ApiSentinelProperties props) {
        this.props = props;
    }

    @Bean
    public WebClient webClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build();
        HttpClient httpClient = HttpClient.create();
        if (props.getSsl().isInsecure()) {
            try {
                var ssl = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                httpClient = httpClient.secure(t -> t.sslContext(ssl));
            } catch (Exception ignored) {
            }
        }
        return WebClient.builder().exchangeStrategies(strategies).clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }
}
