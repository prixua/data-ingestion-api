package br.com.prixua.dataingest.ingestion;

import br.com.prixua.dataingest.ingestion.agent.IngestionAgent;
import br.com.prixua.dataingest.metrics.IngestionMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    static final String COLLECTION = "records";

    private final CsvParsingService csvParsingService;
    private final MongoTemplate mongoTemplate;
    private final List<IngestionAgent> agents;
    private final IngestionMetrics metrics;

    public int ingest(String dataset, InputStream inputStream) throws IOException {
        List<Map<String, Object>> rows = csvParsingService.parse(inputStream);

        List<IngestionAgent> applicableAgents = agents.stream()
                .filter(agent -> agent.supports(dataset))
                .toList();

        int count = 0;
        for (Map<String, Object> row : rows) {
            Map<String, Object> processed = applyAgents(applicableAgents, row);

            Map<String, Object> document = new LinkedHashMap<>();
            document.put("dataset", dataset);
            document.put("data", processed);
            document.put("importedAt", Instant.now());

            mongoTemplate.save(document, COLLECTION);
            count++;
        }

        metrics.recordIngestion(dataset, count);
        log.info("Ingested {} records for dataset '{}'", count, dataset);

        return count;
    }

    private Map<String, Object> applyAgents(List<IngestionAgent> applicableAgents, Map<String, Object> row) {
        Map<String, Object> current = row;
        for (IngestionAgent agent : applicableAgents) {
            current = agent.process(current);
        }
        return current;
    }
}
