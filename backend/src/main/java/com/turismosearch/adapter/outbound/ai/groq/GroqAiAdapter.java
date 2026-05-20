package com.turismosearch.adapter.outbound.ai.groq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turismosearch.adapter.outbound.ai.ClaudePromptBuilder;
import com.turismosearch.domain.exception.AiServiceException;
import com.turismosearch.domain.model.*;
import com.turismosearch.domain.port.outbound.AiRecommendationPort;
import com.turismosearch.infrastructure.properties.GroqProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "groq", matchIfMissing = true)
public class GroqAiAdapter implements AiRecommendationPort {

    private final WebClient groqWebClient;
    private final ClaudePromptBuilder promptBuilder;
    private final GroqProperties properties;
    private final ObjectMapper objectMapper;

    private static final int OSM_THRESHOLD = 1; // ENRICH mode whenever OSM has any real POIs

    @Override
    @Cacheable(value = "attractions", key = "#cityDisplayName + '-' + #query.radiusKm + '-' + #query.maxResults")
    public List<Attraction> recommendAttractions(SearchQuery query, String cityDisplayName) {
        boolean hasOsmPois = query.getOsmPois() != null && query.getOsmPois().size() >= OSM_THRESHOLD;
        log.info("Consultando Groq API (Llama 3.3 70B) para: {} — modo: {} (cache MISS)",
                cityDisplayName, hasOsmPois ? "ENRICH" : "DIRECT");

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = hasOsmPois
                ? promptBuilder.buildEnrichPrompt(query.getOsmPois(), query, cityDisplayName)
                : promptBuilder.buildDirectPrompt(query, cityDisplayName);

        GroqRequest request = GroqRequest.builder()
                .model(properties.getModel())
                .maxTokens(properties.getMaxTokens())
                .messages(List.of(
                        GroqMessage.system(systemPrompt),
                        GroqMessage.user(userPrompt)
                ))
                .responseFormat(GroqRequest.ResponseFormat.builder().type("json_object").build())
                .build();

        try {
            GroqResponse response = groqWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GroqResponse.class)
                    .block();

            if (response == null) {
                throw new AiServiceException("Resposta nula da Groq API");
            }

            if (response.getUsage() != null) {
                log.info("Groq usage para {}: prompt={}, completion={}, total={}",
                        cityDisplayName,
                        response.getUsage().getPromptTokens(),
                        response.getUsage().getCompletionTokens(),
                        response.getUsage().getTotalTokens());
            }

            return parseAttractions(response.getTextContent());

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Erro ao consultar Groq API: " + e.getMessage(), e);
        }
    }

    private List<Attraction> parseAttractions(String jsonText) {
        try {
            String cleaned = jsonText.trim()
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "");

            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode attractionsNode = root.get("attractions");

            if (attractionsNode == null || !attractionsNode.isArray()) {
                log.warn("Resposta da IA não contém array 'attractions'. Resposta: {}", cleaned.substring(0, Math.min(200, cleaned.length())));
                return List.of();
            }

            List<Attraction> result = new ArrayList<>();
            for (JsonNode node : attractionsNode) {
                try {
                    result.add(mapNodeToAttraction(node));
                } catch (Exception e) {
                    log.warn("Erro ao mapear atração individual: {}", e.getMessage());
                }
            }
            return result;

        } catch (Exception e) {
            log.error("Erro ao parsear JSON da Groq: {}", e.getMessage());
            throw new AiServiceException("Erro ao processar resposta da IA");
        }
    }

    private Attraction mapNodeToAttraction(JsonNode node) {
        Coordinates coords = null;
        JsonNode coordsNode = node.get("coordinates");
        if (coordsNode != null) {
            double lat = coordsNode.path("lat").asDouble();
            double lng = coordsNode.path("lng").asDouble();
            if (lat != 0 || lng != 0) {
                coords = Coordinates.of(lat, lng);
            }
        }

        AttractionCategory category;
        try {
            category = AttractionCategory.valueOf(node.path("category").asText("OTHER").toUpperCase());
        } catch (IllegalArgumentException e) {
            category = AttractionCategory.OTHER;
        }

        return Attraction.builder()
                .name(node.path("name").asText("Atração sem nome"))
                .description(node.path("description").asText(""))
                .category(category)
                .subcategory(nullIfEmpty(node.path("subcategory").asText(null)))
                .coordinates(coords)
                .tags(readStringList(node.get("tags")))
                .highlights(readStringList(node.get("highlights")))
                .bestPeriod(nullIfEmpty(node.path("best_period").asText(null)))
                .openingHours(nullIfEmpty(node.path("opening_hours").asText(null)))
                .entryFee(nullIfEmpty(node.path("entry_fee").asText(null)))
                .accessibilityInfo(nullIfEmpty(node.path("accessibility_info").asText(null)))
                .tips(readStringList(node.get("tips")))
                .aiConfidenceScore(node.path("confidence_score").asDouble(0.75))
                .build();
    }

    private List<String> readStringList(JsonNode node) {
        if (node == null || !node.isArray()) return List.of();
        List<String> result = new ArrayList<>();
        node.forEach(n -> {
            String text = n.asText("").trim();
            if (!text.isEmpty()) result.add(text);
        });
        return result;
    }

    private String nullIfEmpty(String value) {
        if (value == null || value.isBlank() || value.equals("null")) return null;
        return value;
    }
}
