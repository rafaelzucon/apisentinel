package br.com.apisentinel.batch.scheduler;

import br.com.apisentinel.config.ApiSentinelProperties;
import br.com.apisentinel.service.OrchestrationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledLauncher {
    private final ApiSentinelProperties props;
    private final OrchestrationService orchestration;

    public ScheduledLauncher(ApiSentinelProperties props, OrchestrationService orchestration) {
        this.props = props;
        this.orchestration = orchestration;
    }

    @Scheduled(cron = "#{@apisentinelCron}")
    public void runCsvDiscovery() throws Exception {
        if (!props.getScheduling().isEnabled()) return;
        orchestration.runCsvDiscoveryOnce();
    }
}
