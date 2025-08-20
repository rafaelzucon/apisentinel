package br.com.apisentinel.batch.writer;

import br.com.apisentinel.config.ApiSentinelProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

public class CsvDiscoveryWriter {
    private final ApiSentinelProperties props;
    private final ReentrantLock lock = new ReentrantLock(true);

    public CsvDiscoveryWriter(ApiSentinelProperties props) {
        this.props = props;
    }

    public void writeLine(String status, String type, String id, String name, String version, String context) {
        lock.lock();
        try {
            Path target = Path.of(props.getOutput().getDiscovery());
            Files.createDirectories(target.getParent());
            try (PrintWriter pw = new PrintWriter(new FileWriter(target.toFile(), true))) {
                pw.printf("%s;%s;%s;%s;%s;%s%n", status, type, id, name, version, context);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
