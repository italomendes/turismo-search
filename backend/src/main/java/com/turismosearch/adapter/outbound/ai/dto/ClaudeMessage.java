package com.turismosearch.adapter.outbound.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaudeMessage {
    String role;
    Object content; // String ou List<ClaudeContentBlock>

    public static ClaudeMessage user(String text) {
        return ClaudeMessage.builder().role("user").content(text).build();
    }

    public static ClaudeMessage userWithCache(String text) {
        return ClaudeMessage.builder()
                .role("user")
                .content(List.of(ClaudeContentBlock.withCache(text)))
                .build();
    }
}
