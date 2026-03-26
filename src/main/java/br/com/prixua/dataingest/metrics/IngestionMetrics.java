package br.com.prixua.dataingest.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestionMetrics {

    private final MeterRegistry meterRegistry;

    public void recordIngestion(String dataset, int count) {
        Counter.builder("ingestion.records.total")
                .tag("dataset", dataset)
                .description("Total records ingested per dataset")
                .register(meterRegistry)
                .increment(count);

        Counter.builder("ingestion.files.total")
                .tag("dataset", dataset)
                .description("Total files ingested per dataset")
                .register(meterRegistry)
                .increment();
    }
}
