package com.turismosearch.adapter.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaudeContentBlock {
    String type;
    String text;

    @JsonProperty("cache_control")
    CacheControl cacheControl;

    public static ClaudeContentBlock withCache(String text) {
        return ClaudeContentBlock.builder()
                .type("text")
                .text(text)
                .cacheControl(new CacheControl("ephemeral"))
                .build();
    }

    public static ClaudeContentBlock plain(String text) {
        return ClaudeContentBlock.builder()
                .type("text")
                .text(text)
                .build();
    }

    public record CacheControl(String type) {}
}
