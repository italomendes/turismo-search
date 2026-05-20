package com.turismosearch.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Controla qual provedor de IA está ativo.
 * ai.provider=groq   → usa Groq (free tier, Llama 3.3 70B)
 * ai.provider=claude → usa Anthropic Claude (requer créditos)
 * ai.provider=gemini → usa Google Gemini Flash (free tier)
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProviderProperties {
    private String provider = "groq"; // padrão: groq (gratuito)
}
