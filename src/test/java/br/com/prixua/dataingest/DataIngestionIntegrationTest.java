package br.com.prixua.dataingest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DataIngestionIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldIngestCsvAndReturnCorrectCount() {
        String csv = "name,age,city\nJoão,30,SP\nMaria,25,RJ\nPedro,35,SP\n";
        ResponseEntity<Map> response = postCsv("integration_test_" + System.currentTimeMillis(), csv);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("recordsIngested", 3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldIngestAndSearchByField() {
        String dataset = "search_test_" + System.currentTimeMillis();
        String csv = "name,age,city\nJoão,30,SP\nMaria,25,RJ\nPedro,35,SP\n";

        ResponseEntity<Map> ingestResponse = postCsv(dataset, csv);
        assertThat(ingestResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> searchResponse = restTemplate.exchange(
                "/api/v1/search?dataset={dataset}&field=city&value=SP",
                HttpMethod.GET,
                null,
                Map.class,
                dataset
        );

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).isNotNull();
        assertThat(((Number) searchResponse.getBody().get("total")).longValue()).isEqualTo(2L);

        List<Map<String, Object>> content = (List<Map<String, Object>>) searchResponse.getBody().get("content");
        assertThat(content).hasSize(2);
        assertThat(content).allMatch(record -> "SP".equals(record.get("city")));
    }

    @Test
    void shouldReturnEmptyResultsForNonExistentDataset() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/search?dataset=nonexistent&field=name&value=anyone",
                HttpMethod.GET,
                null,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) response.getBody().get("total")).longValue()).isZero();
    }

    private ResponseEntity<Map> postCsv(String dataset, String csv) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileResource = new ByteArrayResource(csv.getBytes()) {
            @Override
            public String getFilename() {
                return "data.csv";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        return restTemplate.exchange(
                "/api/v1/ingest?dataset=" + dataset,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
    }
}
