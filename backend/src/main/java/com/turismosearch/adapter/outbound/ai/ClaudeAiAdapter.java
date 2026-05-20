package com.turismosearch.adapter.outbound.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turismosearch.adapter.outbound.ai.dto.ClaudeMessage;
import com.turismosearch.adapter.outbound.ai.dto.ClaudeRequest;
import com.turismosearch.adapter.outbound.ai.dto.ClaudeResponse;
import com.turismosearch.domain.exception.AiServiceException;
import com.turismosearch.domain.model.*;
import com.turismosearch.domain.port.outbound.AiRecommendationPort;
import com.turismosearch.infrastructure.properties.ClaudeProperties;
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
@ConditionalOnProperty(name = "ai.provider", havingValue = "claude")
public class ClaudeAiAdapter implements AiRecommendationPort {

    private final WebClient claudeWebClient;
    private final ClaudePromptBuilder promptBuilder;
    private final ClaudeProperties properties;
    private final ObjectMapper objectMapper;

    private static final int OSM_THRESHOLD = 3;

    @Override
    @Cacheable(value = "attractions", key = "#cityDisplayName + '-' + #query.radiusKm + '-' + #query.maxResults")
    public List<Attraction> recommendAttractions(SearchQuery query, String cityDisplayName) {
        boolean hasOsmPois = query.getOsmPois() != null && query.getOsmPois().size() >= OSM_THRESHOLD;
        log.info("Consultando Claude API para: {} — modo: {} (cache MISS)",
                cityDisplayName, hasOsmPois ? "ENRICH" : "DIRECT");

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = hasOsmPois
                ? promptBuilder.buildEnrichPrompt(query.getOsmPois(), query, cityDisplayName)
                : promptBuilder.buildDirectPrompt(query, cityDisplayName);

        ClaudeRequest request = ClaudeRequest.builder()
                .model(properties.getModel())
                .maxTokens(properties.getMaxTokens())
                .system(List.of(ClaudeRequest.ClaudeSystemBlock.withCache(systemPrompt)))
                .messages(List.of(ClaudeMessage.user(userPrompt)))
                .build();

        try {
            ClaudeResponse response = claudeWebClient.post()
                    .uri("/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClaudeResponse.class)
                    .block();

            if (response == null) {
                throw new AiServiceException("Resposta nula da Claude API");
            }

            logUsage(response, cityDisplayName);
            return parseAttractions(response.getTextContent());

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Erro ao consultar Claude API: " + e.getMessage(), e);
        }
    }

    private List<Attraction> parseAttractions(String jsonText) {
        try {
            // Limpar possível markdown residual
            String cleaned = jsonText.trim()
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "");

            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode attractionsNode = root.get("attractions");

            if (attractionsNode == null || !attractionsNode.isArray()) {
                log.warn("Resposta da IA não contém array 'attractions'");
                return List.of();
            }

            List<Attraction> result = new ArrayList<>();
            for (JsonNode node : attractionsNode) {
                try {
                    result.add(mapNodeToAttraction(node));
                } catch (Exception e) {
                    log.warn("Erro ao mapear atração: {}", e.getMessage());
                }
            }
            return result;

        } catch (Exception e) {
            log.error("Erro ao parsear resposta JSON da IA: {}", jsonText, e);
            throw new AiServiceException("Erro ao processar resposta da IA");
        }
    }

    private Attraction mapNodeToAttraction(JsonNode node) {
        Coordinates coords = null;
        JsonNode coordsNode = node.get("coordinates");
        if (coordsNode != null) {
            coords = Coordinates.of(
                    coordsNode.path("lat").asDouble(),
                    coordsNode.path("lng").asDouble()
            );
        }

        AttractionCategory category;
        try {
            category = AttractionCategory.valueOf(node.path("category").asText("OTHER").toUpperCase());
        } catch (IllegalArgumentException e) {
            category = AttractionCategory.OTHER;
        }

        List<String> tags = readStringList(node.get("tags"));
        List<String> highlights = readStringList(node.get("highlights"));
        List<String> tips = readStringList(node.get("tips"));

        return Attraction.builder()
                .name(node.path("name").asText())
                .description(node.path("description").asText())
                .category(category)
                .subcategory(node.path("subcategory").asText(null))
                .coordinates(coords)
                .tags(tags)
                .highlights(highlights)
                .bestPeriod(node.path("best_period").asText(null))
                .openingHours(node.path("opening_hours").asText(null))
                .entryFee(node.path("entry_fee").asText(null))
                .accessibilityInfo(node.path("accessibility_info").asText(null))
                .tips(tips)
                .aiConfidenceScore(node.path("confidence_score").asDouble(0.8))
                .build();
    }

    private List<String> readStringList(JsonNode node) {
        if (node == null || !node.isArray()) return List.of();
        List<String> result = new ArrayList<>();
        node.forEach(n -> result.add(n.asText()));
        return result;
    }

    private void logUsage(ClaudeResponse response, String city) {
        if (response.getUsage() != null) {
            var u = response.getUsage();
            log.info("Claude usage para {}: input={}, output={}, cacheHit={}, cacheCreate={}",
                    city, u.getInputTokens(), u.getOutputTokens(),
                    u.getCacheReadInputTokens(), u.getCacheCreationInputTokens());
        }
    }
}
