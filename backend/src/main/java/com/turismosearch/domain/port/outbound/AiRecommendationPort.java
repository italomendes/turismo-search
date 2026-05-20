package com.turismosearch.domain.port.outbound;

import com.turismosearch.domain.model.Attraction;
import com.turismosearch.domain.model.SearchQuery;

import java.util.List;

public interface AiRecommendationPort {
    /**
     * Consulta a IA para obter recomendações de atrações turísticas.
     * A resposta é estruturada e pode ser cacheada.
     */
    List<Attraction> recommendAttractions(SearchQuery query, String cityDisplayName);
}
