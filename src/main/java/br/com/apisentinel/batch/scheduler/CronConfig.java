package br.com.apisentinel.batch.scheduler;

import br.com.apisentinel.config.ApiSentinelProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CronConfig {
    @Bean(name = "apisentinelCron")
    public String apisentinelCron(ApiSentinelProperties props) {
        return props.getScheduling().getCron();
    }
}
