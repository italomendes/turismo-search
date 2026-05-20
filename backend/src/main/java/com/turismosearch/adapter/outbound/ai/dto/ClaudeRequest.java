package com.turismosearch.adapter.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ClaudeRequest {
    String model;

    @JsonProperty("max_tokens")
    int maxTokens;

    List<ClaudeSystemBlock> system;
    List<ClaudeMessage> messages;

    @Value
    @Builder
    public static class ClaudeSystemBlock {
        String type;
        String text;

        @JsonProperty("cache_control")
        ClaudeContentBlock.CacheControl cacheControl;

        public static ClaudeSystemBlock withCache(String text) {
            return ClaudeSystemBlock.builder()
                    .type("text")
                    .text(text)
                    .cacheControl(new ClaudeContentBlock.CacheControl("ephemeral"))
                    .build();
        }
    }
}
