package com.turismosearch.adapter.outbound.ai.groq;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GroqMessage {
    String role;   // "system" | "user" | "assistant"
    String content;

    public static GroqMessage system(String content) {
        return GroqMessage.builder().role("system").content(content).build();
    }

    public static GroqMessage user(String content) {
        return GroqMessage.builder().role("user").content(content).build();
    }
}
