package com.turismosearch.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "overpass")
public class OverpassProperties {
    /**
     * Primary Overpass API endpoint.
     */
    private String baseUrl = "https://overpass-api.de/api/interpreter";

    /**
     * Fallback mirrors tried in order when primary returns 5xx/timeout.
     */
    private List<String> fallbackUrls = List.of(
            "https://overpass.private.coffee/api/interpreter",
            "https://z.overpass-api.de/api/interpreter"
    );

    private int timeoutSeconds = 28;
}
