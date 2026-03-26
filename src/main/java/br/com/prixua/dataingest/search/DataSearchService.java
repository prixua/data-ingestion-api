package br.com.prixua.dataingest.search;

import br.com.prixua.dataingest.dto.PagedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DataSearchService {

    static final String COLLECTION = "records";

    private final MongoTemplate mongoTemplate;

    @SuppressWarnings("unchecked")
    public PagedResult<Map<String, Object>> search(String dataset, String field, String value, int page, int size) {
        Query countQuery = buildQuery(dataset, field, value);
        long total = mongoTemplate.count(countQuery, COLLECTION);

        Query pageQuery = buildQuery(dataset, field, value);
        pageQuery.with(PageRequest.of(page, size));

        List<Map> rawResults = mongoTemplate.find(pageQuery, Map.class, COLLECTION);

        List<Map<String, Object>> records = rawResults.stream()
                .map(doc -> (Map<String, Object>) doc.get("data"))
                .toList();

        return new PagedResult<>(records, total, page, size);
    }

    private Query buildQuery(String dataset, String field, String value) {
        String resolvedField = resolveFieldCaseInsensitive(dataset, field);
        Pattern valuePattern = Pattern.compile(Pattern.quote(value), Pattern.CASE_INSENSITIVE);

        return new Query()
                .addCriteria(Criteria.where("dataset").is(dataset))
                .addCriteria(Criteria.where("data." + resolvedField).regex(valuePattern));
    }

    private String resolveFieldCaseInsensitive(String dataset, String field) {
        Query sampleQuery = new Query(Criteria.where("dataset").is(dataset)).limit(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> sample = mongoTemplate.findOne(sampleQuery, Map.class, COLLECTION);

        if (sample == null) {
            return field;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) sample.get("data");
        if (data == null) {
            return field;
        }

        return data.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(field))
                .findFirst()
                .orElse(field);
    }
}
