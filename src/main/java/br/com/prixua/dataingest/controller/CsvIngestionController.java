package br.com.prixua.dataingest.controller;

import br.com.prixua.dataingest.controller.docs.CsvIngestionControllerDocs;
import br.com.prixua.dataingest.ingestion.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CsvIngestionController implements CsvIngestionControllerDocs {

    private final IngestionService ingestionService;

    @PostMapping(value = "/ingest", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<Map<String, Object>> ingest(
            @RequestParam String dataset,
            @RequestPart("file") MultipartFile file) {
        try {
            int count = ingestionService.ingest(dataset, file.getInputStream());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("dataset", dataset);
            response.put("recordsIngested", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to ingest file for dataset '{}': {}", dataset, e.getMessage(), e);

            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Failed to ingest file");
            error.put("message", e.getMessage());

            return ResponseEntity.internalServerError().body(error);
        }
    }
}
