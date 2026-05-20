package com.turismosearch.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nominatim")
public class NominatimProperties {
    private String baseUrl = "https://nominatim.openstreetmap.org";
    private String userAgent = "TurismoSearch/1.0";
    private int timeoutSeconds = 10;
}
