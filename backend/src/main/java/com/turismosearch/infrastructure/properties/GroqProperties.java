package com.turismosearch.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "groq")
public class GroqProperties {
    private String apiKey = "";
    private String model = "llama-3.3-70b-versatile";
    private int maxTokens = 4096;
    private int timeoutSeconds = 60;
}
