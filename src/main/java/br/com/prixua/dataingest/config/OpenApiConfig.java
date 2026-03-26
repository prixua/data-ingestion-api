package br.com.prixua.dataingest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Data Ingestion API")
                        .description("Dynamic CSV ingestion and search — no hardcoded domain classes. Each CSV row is stored as a JSON document in MongoDB.")
                        .version("1.0.0"));
    }
}
