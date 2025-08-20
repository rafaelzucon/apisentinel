package br.com.apisentinel.batch.writer;

import br.com.apisentinel.config.ApiSentinelProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

public class CsvInconsistencyWriter {
    private final ApiSentinelProperties props;
    private final ReentrantLock lock = new ReentrantLock(true);

    public CsvInconsistencyWriter(ApiSentinelProperties props) {
        this.props = props;
    }

    public void write(String code, String... fields) {
        lock.lock();
        try {
            Path target = Path.of(props.getOutput().getInconsistencies());
            Files.createDirectories(target.getParent());
            String payload = String.join(";", fields);
            try (PrintWriter pw = new PrintWriter(new FileWriter(target.toFile(), true))) {
                pw.printf("%s;%s%n", code, payload);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
