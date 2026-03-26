package br.com.prixua.dataingest.controller.docs;

import br.com.prixua.dataingest.dto.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "Data Search", description = "Single endpoint for querying ingested data")
public interface DataSearchControllerDocs {

    @Operation(
            summary = "Search ingested records",
            description = "Queries documents by dataset and a dynamic field/value filter. "
                    + "The 'field' parameter must match a CSV column name used during ingestion. "
                    + "Example: GET /api/v1/search?dataset=customers_2026&field=city&value=São Paulo",
            parameters = {
                    @Parameter(name = "dataset", description = "Dataset name", required = true, example = "customers_2026"),
                    @Parameter(name = "field", description = "CSV column name to filter by", required = true, example = "city"),
                    @Parameter(name = "value", description = "Value to match", required = true, example = "São Paulo"),
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Page size (default 10)", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Records returned",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(example = "{\"content\":[{\"name\":\"João\",\"city\":\"SP\"}],\"total\":1,\"page\":0,\"size\":10}"))),
                    @ApiResponse(responseCode = "400", description = "Missing required parameters")
            }
    )
    ResponseEntity<PagedResult<Map<String, Object>>> search(
            @RequestParam String dataset,
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);
}
