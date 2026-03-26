package br.com.prixua.dataingest.ingestion.agent;

import java.util.Map;

// OCP interface — add new implementations without modifying existing code
public interface IngestionAgent {

    boolean supports(String dataset);

    Map<String, Object> process(Map<String, Object> row);
}
