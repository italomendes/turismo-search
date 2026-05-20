package com.turismosearch.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "anthropic")
public class ClaudeProperties {
    private String apiKey;
    private String model = "claude-sonnet-4-5";
    private int maxTokens = 4096;
    private int timeoutSeconds = 30;
}
