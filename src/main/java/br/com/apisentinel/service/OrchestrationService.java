package br.com.apisentinel.service;

import br.com.apisentinel.config.ApiSentinelProperties;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@Service
public class OrchestrationService {
    private final JobLauncher jobLauncher;
    private final Job csvDiscoveryJob;
    private final ApiSentinelProperties props;
    private final ApiGatewayDiscoveryService apiGatewayDiscoveryService;

    public OrchestrationService(JobLauncher jobLauncher,
                                Job csvDiscoveryJob,
                                ApiSentinelProperties props,
                                ApiGatewayDiscoveryService apiGatewayDiscoveryService) {
        this.jobLauncher = jobLauncher;
        this.csvDiscoveryJob = csvDiscoveryJob;
        this.props = props;
        this.apiGatewayDiscoveryService = apiGatewayDiscoveryService;
    }

    public void runCsvDiscoveryOnce() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("ts", Instant.now().toEpochMilli())
                .toJobParameters();
        jobLauncher.run(csvDiscoveryJob, params);

        // Após execução, renomeia o arquivo CSV para evitar reprocessamento
        Path input = Paths.get(props.getCsv().getInput());
        if (Files.exists(input)) {
            Path processed = input.resolveSibling("input_processed.csv");
            try {
                Files.move(input, processed, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // Logar ou relançar, conforme regra de negócio
                throw new RuntimeException("Falha ao renomear arquivo CSV para 'input_processed.csv'", e);
            }
        }
    }

    public void runExternalApiSyncOnce() {
        if (props.getSync().isEnabled()) {
            apiGatewayDiscoveryService.syncFromExternalApi();
        }
    }

    public void runAllOnce() throws Exception {
        runCsvDiscoveryOnce();
        runExternalApiSyncOnce();
    }
}
