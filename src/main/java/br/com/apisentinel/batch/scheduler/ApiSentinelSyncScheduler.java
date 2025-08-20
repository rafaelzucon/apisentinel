package br.com.apisentinel.batch.scheduler;

import br.com.apisentinel.config.ApiSentinelProperties;
import br.com.apisentinel.service.OrchestrationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ApiSentinelSyncScheduler {
    private final ApiSentinelProperties props;
    private final OrchestrationService orchestration;

    public ApiSentinelSyncScheduler(ApiSentinelProperties props, OrchestrationService orchestration) {
        this.props = props;
        this.orchestration = orchestration;
    }

    @Scheduled(cron = "#{@apisentinelCron}")
    public void runExternalApiSync() {
        if (!props.getScheduling().isEnabled()) return;
        orchestration.runExternalApiSyncOnce();
    }
}
