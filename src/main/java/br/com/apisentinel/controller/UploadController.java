package br.com.apisentinel.controller;

import br.com.apisentinel.config.ApiSentinelProperties;
import br.com.apisentinel.service.OrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
        name = "Ingestions",
        description = "Endpoints para ingestão de arquivos (CSV) e disparo dos pipelines de descoberta."
)
@RestController
@RequestMapping("/ingestions")
public class UploadController {

    private final ApiSentinelProperties props;
    private final OrchestrationService orchestration;

    public UploadController(ApiSentinelProperties props, OrchestrationService orchestration) {
        this.props = props;
        this.orchestration = orchestration;
    }

    @Operation(
            summary = "Upload de CSV e início do processamento",
            description = """
                    Recebe um arquivo **CSV** via `multipart/form-data` no campo **file**, \
                    salva no caminho configurado em `extractor.csv.input`, \
                    e dispara `orchestration.runAllOnce()` (pipeline CSV + Sensedia, conforme flags).
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Arquivo recebido e processamento iniciado.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UploadResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "status": "accepted",
                                              "message": "Arquivo recebido e processamento iniciado.",
                                              "savedTo": "./data/input.csv",
                                              "timestamp": "2025-08-22T13:45:22Z"
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erro ao salvar o arquivo ou iniciar o processamento.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UploadError.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "status": "error",
                                              "message": "Detalhes do erro"
                                            }
                                            """)
                            )
                    )
            }
    )

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

    public static class UploadResponse {
        @Schema(example = "accepted")
        public String status;
        @Schema(example = "Arquivo recebido e processamento iniciado.")
        public String message;
        @Schema(example = "./data/input.csv")
        public String savedTo;
        @Schema(example = "2025-08-22T13:45:22Z")
        public String timestamp;
    }

    public static class UploadError {
        @Schema(example = "error")
        public String status;
        @Schema(example = "Detalhes do erro")
        public String message;
    }
}
