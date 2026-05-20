package com.turismosearch.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "overpass")
public class OverpassProperties {
    private String baseUrl = "https://overpass-api.de/api/interpreter";
    private int timeoutSeconds = 30;
}
