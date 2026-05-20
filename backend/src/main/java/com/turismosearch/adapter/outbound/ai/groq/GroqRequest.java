package com.turismosearch.adapter.outbound.ai.groq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

// Groq usa formato compatível com OpenAI Chat Completions
@Value
@Builder
public class GroqRequest {
    String model;

    @JsonProperty("max_tokens")
    int maxTokens;

    List<GroqMessage> messages;

    @JsonProperty("response_format")
    ResponseFormat responseFormat;

    @Value
    @Builder
    public static class ResponseFormat {
        String type; // "json_object" força resposta em JSON
    }
}
