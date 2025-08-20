package br.com.apisentinel.batch;

import br.com.apisentinel.config.ApiSentinelProperties;
import br.com.apisentinel.dto.CsvAssetRow;
import br.com.apisentinel.service.CsvDiscoveryService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Configuration
public class ApiSentinelJobConfig {

    private static final String[] COLUMNS = new String[]{
            "type", "name", "friendlyName", "context", "version", "description", "application", "timeModelState",
            "accessScope", "host", "apiGatewayExposure", "communicationChannel", "communicationProtocol",
            "messageFormats", "authenticationType", "authorizationType", "secureCommunicationChannel",
            "credentialVaultLocation", "securityTestType", "usesSensitiveData", "rateLimitTps", "team",
            "technicalOwner", "technicalEmail", "businessOwner", "businessEmail"
    };

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final ApiSentinelProperties props;
    private final CsvDiscoveryService csvDiscoveryService;

    public ApiSentinelJobConfig(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              ApiSentinelProperties props,
                              CsvDiscoveryService csvDiscoveryService) {
        this.jobRepository = jobRepository;
        this.txManager = txManager;
        this.props = props;
        this.csvDiscoveryService = csvDiscoveryService;
    }

    @Bean(name = "csvReader")
    public FlatFileItemReader<CsvAssetRow> csvReader(
            @Value("${apisentinel.csv.input:}") String configuredPath
    ) {
        String path = (configuredPath != null && !configuredPath.isBlank())
                ? configuredPath
                : props.getCsv().getInput();

        Resource resource = new FileSystemResource(path);

        char delimiter = detectDelimiterByContent(resource, ',');

        FlatFileItemReader<CsvAssetRow> reader = new FlatFileItemReader<>();
        reader.setName("csvReader");
        reader.setResource(resource);
        reader.setEncoding(StandardCharsets.UTF_8.name());
        reader.setLinesToSkip(1);
        reader.setSkippedLinesCallback(headerSkipper());
        reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy() {
            @Override
            public boolean isEndOfRecord(String line) {
                return super.isEndOfRecord(line);
            }
        });

        DefaultLineMapper<CsvAssetRow> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(buildTokenizer(delimiter));
        BeanWrapperFieldSetMapper<CsvAssetRow> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CsvAssetRow.class);
        fieldSetMapper.setStrict(false);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        lineMapper.afterPropertiesSet();

        reader.setLineMapper(lineMapper);
        return reader;
    }

    private LineTokenizer buildTokenizer(char delimiter) {
        DelimitedLineTokenizer t = new DelimitedLineTokenizer();
        t.setDelimiter(String.valueOf(delimiter));
        t.setQuoteCharacter('"');
        t.setNames(COLUMNS);
        t.setStrict(false);
        int[] included = new int[COLUMNS.length];
        for (int i = 0; i < included.length; i++) included[i] = i;
        t.setIncludedFields(included);
        return t;
    }

    private LineCallbackHandler headerSkipper() {
        return line -> {
        };
    }

    private char detectDelimiterByContent(Resource resource, char defaultDelimiter) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;
                int commas = count(t, ',');
                int semicolons = count(t, ';');
                if (semicolons > commas) return ';';
                if (commas > semicolons) return ',';
                return defaultDelimiter;
            }
        } catch (Exception ignored) {
        }
        return defaultDelimiter;
    }

    private int count(@NonNull String s, char ch) {
        int c = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == ch) c++;
        return c;
    }


    @Bean(name = "csvWriter")
    public ItemWriter<CsvAssetRow> csvWriter() {
        return items -> {
            for (CsvAssetRow row : items) {
                csvDiscoveryService.process(row);
            }
        };
    }

    @Bean
    public Step csvDiscoveryStep(@Qualifier("csvReader") ItemReader<CsvAssetRow> reader,
                                 @Qualifier("csvWriter") ItemWriter<CsvAssetRow> writer) {
        return new StepBuilder("csvDiscoveryStep", jobRepository)
                .<CsvAssetRow, CsvAssetRow>chunk(200, txManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(org.springframework.batch.item.file.FlatFileParseException.class)
                .build();
    }

    @Bean
    public Job csvDiscoveryJob(Step csvDiscoveryStep) {
        return new JobBuilder("csvDiscoveryJob", jobRepository)
                .start(csvDiscoveryStep)
                .build();
    }
}
