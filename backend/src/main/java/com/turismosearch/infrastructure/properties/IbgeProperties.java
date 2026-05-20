package com.turismosearch.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ibge")
public class IbgeProperties {
    private String baseUrl = "https://servicodados.ibge.gov.br/api/v1";
    private int timeoutSeconds = 10;
}
