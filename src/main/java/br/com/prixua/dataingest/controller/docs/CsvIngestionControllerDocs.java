package br.com.prixua.dataingest.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "CSV Ingestion", description = "Upload and import CSV files into named datasets")
public interface CsvIngestionControllerDocs {

    @Operation(
            summary = "Ingest a CSV file",
            description = "Uploads a CSV file and stores each row as a JSON document under the given dataset name. "
                    + "Column names are detected automatically from the first row.",
            parameters = {
                    @Parameter(name = "dataset", description = "Name of the dataset (collection key)", required = true, example = "customers_2026")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "File ingested successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(example = "{\"dataset\":\"customers_2026\",\"recordsIngested\":100}"))),
                    @ApiResponse(responseCode = "400", description = "Missing dataset or file parameter"),
                    @ApiResponse(responseCode = "500", description = "Error during ingestion")
            }
    )
    ResponseEntity<Map<String, Object>> ingest(
            @RequestParam String dataset,
            @Parameter(description = "CSV file to ingest", required = true)
            @RequestPart("file") MultipartFile file);
}
