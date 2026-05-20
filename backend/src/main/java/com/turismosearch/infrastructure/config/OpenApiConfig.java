package com.turismosearch.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI turismoSearchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TurismoSearch API")
                        .description("API para busca de atrações turísticas com IA (Claude)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TurismoSearch")
                                .url("https://github.com/italomendes/turismo-search")));
    }
}
