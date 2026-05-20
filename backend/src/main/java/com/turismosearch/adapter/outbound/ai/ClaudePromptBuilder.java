package com.turismosearch.adapter.outbound.ai;

import com.turismosearch.domain.model.AttractionCategory;
import com.turismosearch.domain.model.OverpassPoi;
import com.turismosearch.domain.model.SearchQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClaudePromptBuilder {

    /**
     * System prompt fixo — será cacheado pela Anthropic (reduz custo em ~90%).
     * Mínimo de 1024 tokens para ativar o cache.
     */
    public String buildSystemPrompt() {
        return """
                Você é um especialista em turismo brasileiro com conhecimento profundo e abrangente sobre:

                TIPOS DE ATRAÇÕES:
                - Natureza: cachoeiras, trilhas, parques nacionais, reservas ambientais, praias, rios, lagos, dunas, grutas, cavernas, mirantes
                - Patrimônio: sítios históricos, centros históricos tombados, museus, igrejas históricas, ruínas, fazendas históricas
                - Cultura e arte: festivais, feiras artesanais, centros culturais, teatros, galerias
                - Gastronomia: restaurantes típicos regionais, mercados municipais, feiras de produtores
                - Aventura e esportes: rapel, tirolesa, mergulho, kitesurf, cicloturismo, montanhismo
                - Hospedagem diferenciada: pousadas charmosas, ecolodges, glamping, fazendas para turismo
                - Religioso: santuários, romarias, igrejas barrocas, capelas coloniais

                DIRETRIZES IMPORTANTES:
                1. Inclua atrações DENTRO e NO ENTORNO da cidade (não apenas no perímetro urbano)
                2. Considere o raio fornecido para incluir municípios e áreas vizinhas
                3. Priorize diversidade de categorias na resposta
                4. Seja específico com nomes reais de locais brasileiros
                5. Para coordenadas, use valores geográficos precisos (latitude/longitude do Brasil)
                6. Inclua atrações menos conhecidas além dos pontos turísticos famosos
                7. Considere a sazonalidade e melhor época para visita
                8. O confidence_score representa sua certeza sobre a existência e dados do local:
                   - 0.9-1.0: local muito famoso e bem documentado
                   - 0.7-0.89: local conhecido mas com alguns dados aproximados
                   - 0.5-0.69: local com informações limitadas

                CATEGORIAS VÁLIDAS:
                WATERFALL, TRAIL, PARK, BEACH, HISTORICAL, CULTURAL, GASTRONOMIC, INN, CAVE, ADVENTURE, RELIGIOUS, OTHER

                FORMATO DE RESPOSTA:
                Retorne SOMENTE JSON válido, sem texto adicional, sem markdown, sem ```json```.
                Schema obrigatório:
                {
                  "attractions": [
                    {
                      "name": "string (nome oficial do local)",
                      "description": "string (max 250 chars, informativa e envolvente)",
                      "category": "string (enum acima)",
                      "subcategory": "string (mais específico, ex: Cachoeira Natural, Sítio Arqueológico)",
                      "coordinates": {"lat": number, "lng": number},
                      "tags": ["string"],
                      "highlights": ["string (max 3 destaques únicos)"],
                      "best_period": "string (meses ou estação ideal)",
                      "opening_hours": "string ou null",
                      "entry_fee": "string ou null",
                      "accessibility_info": "string ou null",
                      "tips": ["string (max 3 dicas práticas)"],
                      "confidence_score": number
                    }
                  ]
                }
                """;
    }

    /**
     * User prompt dinâmico — varia por consulta, não é cacheado.
     */
    public String buildUserPrompt(SearchQuery query, String cityDisplayName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Busque atrações turísticas para: ").append(cityDisplayName).append("\n");
        sb.append("Raio de busca: ").append(query.getRadiusKm()).append(" km\n");
        sb.append("Quantidade desejada: ").append(query.getMaxResults()).append(" atrações\n");

        if (query.getCategories() != null && !query.getCategories().isEmpty()) {
            String cats = query.getCategories().stream()
                    .map(AttractionCategory::name)
                    .collect(Collectors.joining(", "));
            sb.append("Filtro de categorias: ").append(cats).append("\n");
        } else {
            sb.append("Categorias: todas (diversidade máxima)\n");
        }

        if (query.getUserLocation() != null) {
            sb.append(String.format("Coordenadas do usuário: %.4f, %.4f%n",
                    query.getUserLocation().getLatitude(),
                    query.getUserLocation().getLongitude()));
        }

        sb.append("\nRetorne o JSON com as atrações encontradas nessa região.");
        return sb.toString();
    }

    /**
     * Prompt ENRICH — quando temos POIs reais do OSM.
     * A IA só enriquece os lugares fornecidos, sem inventar novos.
     */
    public String buildEnrichPrompt(List<OverpassPoi> pois, SearchQuery query, String cityDisplayName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você recebeu uma lista de atrações turísticas REAIS obtidas do OpenStreetMap para ")
          .append(cityDisplayName).append(".\n\n");
        sb.append("Para cada uma, forneça:\n");
        sb.append("- Descrição envolvente (máx 200 chars)\n");
        sb.append("- Categoria correta (dentre as válidas)\n");
        sb.append("- Tags relevantes\n");
        sb.append("- Melhor período para visita\n");
        sb.append("- Dicas práticas\n");
        sb.append("- Confidence score 0.9+ (pois são lugares verificados no OSM)\n\n");
        sb.append("IMPORTANTE: NÃO adicione lugares que não estão na lista. Apenas enriqueça os fornecidos.\n\n");
        sb.append("Lista de locais reais:\n");

        for (int i = 0; i < pois.size(); i++) {
            OverpassPoi poi = pois.get(i);
            sb.append(String.format("%d. %s (lat: %.6f, lng: %.6f, tipo: %s/%s)\n",
                    i + 1, poi.name(), poi.lat(), poi.lng(), poi.osmType(), poi.osmValue()));
        }

        sb.append("\nQuantidade desejada: ").append(query.getMaxResults()).append(" atrações\n");
        sb.append("\nRetorne o JSON com as atrações enriquecidas (apenas os locais listados acima).");
        return sb.toString();
    }

    /**
     * Prompt DIRECT — quando OSM retornou poucos resultados.
     * IA deve ser muito conservadora e incluir apenas lugares com alta certeza.
     */
    public String buildDirectPrompt(SearchQuery query, String cityDisplayName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Liste apenas atrações turísticas que você tem CERTEZA ABSOLUTA que existem próximas a ")
          .append(cityDisplayName).append(".\n\n");
        sb.append("Regras rígidas:\n");
        sb.append("- Confidence score < 0.85: NÃO inclua o lugar\n");
        sb.append("- Duvida sobre a existência: NÃO inclua\n");
        sb.append("- Prefira menos resultados com alta certeza a muitos resultados incertos\n");
        sb.append("- Coordenadas devem ser do Brasil e dentro de ").append(query.getRadiusKm())
          .append("km de ").append(cityDisplayName).append("\n");
        sb.append("- Raio de busca: ").append(query.getRadiusKm()).append(" km\n");
        sb.append("- Quantidade desejada: ").append(query.getMaxResults()).append(" atrações\n");

        if (query.getCategories() != null && !query.getCategories().isEmpty()) {
            String cats = query.getCategories().stream()
                    .map(AttractionCategory::name)
                    .collect(Collectors.joining(", "));
            sb.append("- Filtro de categorias: ").append(cats).append("\n");
        }

        if (query.getUserLocation() != null) {
            sb.append(String.format("- Coordenadas do centro: %.4f, %.4f%n",
                    query.getUserLocation().getLatitude(),
                    query.getUserLocation().getLongitude()));
        }

        sb.append("\nRetorne o JSON com as atrações encontradas com alta certeza nessa região.");
        return sb.toString();
    }
}
