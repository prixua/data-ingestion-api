package br.com.prixua.dataingest.ingestion;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvParsingService {

    public List<Map<String, Object>> parse(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        Charset charset = detectCharset(bytes);
        String raw = new String(bytes, charset);

        char delimiter = detectDelimiter(raw.indexOf('\n') > 0 ? raw.substring(0, raw.indexOf('\n')) : raw);

        String fixed = fixMalformedQuotes(raw, delimiter);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();

        List<Map<String, Object>> rows = new ArrayList<>();

        try (CSVParser parser = format.parse(new StringReader(fixed))) {
            for (CSVRecord record : parser) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String header : parser.getHeaderNames()) {
                    row.put(header, record.isSet(header) ? record.get(header) : "");
                }
                rows.add(row);
            }
        }

        return rows;
    }

    private Charset detectCharset(byte[] bytes) {
        // BOM UTF-8
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return StandardCharsets.UTF_8;
        }
        // Try decoding as UTF-8 - if it fails, fall back to Windows-1252 (ANSI)
        try {
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            // Re-encode and check roundtrip fidelity
            if (decoded.getBytes(StandardCharsets.UTF_8).length == bytes.length) {
                return StandardCharsets.UTF_8;
            }
        } catch (Exception ignored) {
            // ignore
        }
        return Charset.forName("Windows-1252");
    }

    private char detectDelimiter(String headerLine) {
        long semicolons = headerLine.chars().filter(c -> c == ';').count();
        long commas = headerLine.chars().filter(c -> c == ',').count();
        long tabs = headerLine.chars().filter(c -> c == '\t').count();
        if (semicolons >= commas && semicolons >= tabs) return ';';
        if (tabs >= commas) return '\t';
        return ',';
    }

    private String fixMalformedQuotes(String content, char delimiter) {
        StringBuilder sb = new StringBuilder(content.length() + 64);
        boolean insideQuotes = false;
        int len = content.length();

        for (int i = 0; i < len; i++) {
            char c = content.charAt(i);

            if (c != '"') {
                sb.append(c);
                continue;
            }

            if (!insideQuotes) {
                insideQuotes = true;
                sb.append(c);
                continue;
            }

            char next = (i + 1 < len) ? content.charAt(i + 1) : 0;

            if (next == '"') {
                // already escaped ""
                sb.append("\"\"");
                i++;
            } else if (next == delimiter || next == '\n' || next == '\r' || next == 0) {
                // proper closing quote
                insideQuotes = false;
                sb.append(c);
            } else {
                // malformed inner quote — escape it
                sb.append("\"\"");
            }
        }

        return sb.toString();
    }
}
