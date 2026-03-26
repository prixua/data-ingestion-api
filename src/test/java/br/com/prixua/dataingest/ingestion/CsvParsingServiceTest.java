package br.com.prixua.dataingest.ingestion;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CsvParsingServiceTest {

    private final CsvParsingService service = new CsvParsingService();

    @Test
    void shouldParseCsvWithHeaders() throws IOException {
        String csv = "name,age,city\nJoão,30,SP\nMaria,25,RJ\n";
        ByteArrayInputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<Map<String, Object>> result = service.parse(input);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsEntry("name", "João");
        assertThat(result.get(0)).containsEntry("age", "30");
        assertThat(result.get(0)).containsEntry("city", "SP");
        assertThat(result.get(1)).containsEntry("name", "Maria");
    }

    @Test
    void shouldReturnEmptyListForHeaderOnlyCsv() throws IOException {
        String csv = "name,age\n";
        ByteArrayInputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<Map<String, Object>> result = service.parse(input);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldTrimWhitespace() throws IOException {
        String csv = "name, age\n João , 30 \n";
        ByteArrayInputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<Map<String, Object>> result = service.parse(input);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("name", "João");
        assertThat(result.get(0)).containsEntry("age", "30");
    }
}
