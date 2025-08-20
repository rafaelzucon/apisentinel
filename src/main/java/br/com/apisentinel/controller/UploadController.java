package br.com.apisentinel.controller;

import br.com.apisentinel.config.ApiSentinelProperties;
import br.com.apisentinel.service.OrchestrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/ingestions")
public class UploadController {

    private final ApiSentinelProperties props;
    private final OrchestrationService orchestration;

    public UploadController(ApiSentinelProperties props, OrchestrationService orchestration) {
        this.props = props;
        this.orchestration = orchestration;
    }

    @PostMapping(
            path = "/csv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Map<String, Object>>> uploadCsv(@RequestPart("file") FilePart file) {
        Path target = Paths.get(props.getCsv().getInput());

        return Mono.fromCallable(() -> {
                    Files.createDirectories(target.getParent());
                    return true;
                })
                .then(file.transferTo(target))
                .then(Mono.fromCallable(() -> {
                    orchestration.runAllOnce();
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                            "status", "accepted",
                            "message", "Arquivo recebido e processamento iniciado.",
                            "savedTo", (Object) target.toString(),
                            "timestamp", (Object) Instant.now().toString()
                    ));
                }))
                .onErrorResume(ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                                "status", "error",
                                "message", ex.getMessage()
                        ))
                ));
    }
}
