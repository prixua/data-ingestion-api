package br.com.prixua.dataingest.controller;

import br.com.prixua.dataingest.controller.docs.DataSearchControllerDocs;
import br.com.prixua.dataingest.dto.PagedResult;
import br.com.prixua.dataingest.search.DataSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DataSearchController implements DataSearchControllerDocs {

    private final DataSearchService dataSearchService;

    @GetMapping("/search")
    @Override
    public ResponseEntity<PagedResult<Map<String, Object>>> search(
            @RequestParam String dataset,
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResult<Map<String, Object>> result = dataSearchService.search(dataset, field, value, page, size);
        return ResponseEntity.ok(result);
    }
}
