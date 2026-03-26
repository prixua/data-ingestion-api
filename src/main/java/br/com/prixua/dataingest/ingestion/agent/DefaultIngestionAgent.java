package br.com.prixua.dataingest.ingestion.agent;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultIngestionAgent implements IngestionAgent {

    @Override
    public boolean supports(String dataset) {
        return true;
    }

    @Override
    public Map<String, Object> process(Map<String, Object> row) {
        return row;
    }
}
