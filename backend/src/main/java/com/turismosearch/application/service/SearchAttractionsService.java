package com.turismosearch.application.service;

import com.turismosearch.domain.model.*;
import com.turismosearch.domain.port.inbound.SearchAttractionsUseCase;
import com.turismosearch.domain.port.outbound.AiRecommendationPort;
import com.turismosearch.domain.port.outbound.GeocodingPort;
import com.turismosearch.domain.port.outbound.RealPoiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAttractionsService implements SearchAttractionsUseCase {

    private final AiRecommendationPort aiRecommendationPort;
    private final GeocodingPort geocodingPort;
    private final RealPoiPort realPoiPort;

        @Override
    public List<Attraction> searchAttractions(SearchQuery query) {
        String cityDisplayName = resolveCityName(query);
        log.info("Buscando atrações para: {} num raio de {}km", cityDisplayName, query.getRadiusKm());

        // 1. Resolve center coordinates for OSM lookup
        Coordinates center = resolveCenter(query);

        // 2. Fetch real POIs from OpenStreetMap via Overpass API
        List<OverpassPoi> osmPois = List.of();
        if (center != null) {
            osmPois = realPoiPort.findRealPois(center, query.getRadiusKm());
            log.info("Overpass retornou {} POIs reais para {}", osmPois.size(), cityDisplayName);
        } else {
            log.warn("Não foi possível resolver coordenadas do centro — modo direto de IA");
        }

        // 3. Build enriched query and call AI
        SearchQuery enrichedQuery = buildQueryWithOsmContext(query, osmPois);
        List<Attraction> attractions = aiRecommendationPort.recommendAttractions(enrichedQuery, cityDisplayName);

        // 4. Enrich with distance if user has GPS coordinates
        if (query.getUserLocation() != null) {
            attractions = attractions.stream()
                    .map(a -> enrichWithDistance(a, query.getUserLocation()))
                    .sorted((a, b) -> Double.compare(
                            a.getDistanceKm() != null ? a.getDistanceKm() : 999.0,
                            b.getDistanceKm() != null ? b.getDistanceKm() : 999.0))
                    .limit(query.getMaxResults())
                    .collect(Collectors.toList());
        }

        log.info("Encontradas {} atrações para {}", attractions.size(), cityDisplayName);
        return attractions;
    }

    /**
     * Resolve the geographic center for the Overpass query.
     * For GPS mode: use user location.
     * For city mode: geocode the city name.
     */
    private Coordinates resolveCenter(SearchQuery query) {
        if (query.getUserLocation() != null) {
            return query.getUserLocation();
        }
        if (query.getCityName() != null && !query.getCityName().isBlank()) {
            try {
                return geocodingPort.geocodeCity(query.getCityName(), query.getStateCode()).orElse(null);
            } catch (Exception e) {
                log.warn("Falha ao geocodificar cidade para Overpass: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Attach OSM context to the query so the AI adapter can choose ENRICH vs DIRECT mode.
     */
    private SearchQuery buildQueryWithOsmContext(SearchQuery query, List<OverpassPoi> osmPois) {
        if (osmPois.isEmpty()) return query;
        return SearchQuery.builder()
                .cityName(query.getCityName())
                .stateCode(query.getStateCode())
                .userLocation(query.getUserLocation())
                .radiusKm(query.getRadiusKm())
                .maxResults(query.getMaxResults())
                .categories(query.getCategories())
                .osmPois(osmPois)
                .build();
    }

    private String resolveCityName(SearchQuery query) {
        if (query.getCityName() != null && !query.getCityName().isBlank()) {
            return query.getCityName() + ", " + query.getStateCode();
        }
        if (query.getUserLocation() != null) {
            return geocodingPort.reverseGeocode(query.getUserLocation())
                    .map(city -> city.getName() + ", " + city.getStateCode())
                    .orElse("localização atual");
        }
        return "localização desconhecida";
    }

    private Attraction enrichWithDistance(Attraction attraction, Coordinates userLocation) {
        if (attraction.getCoordinates() == null) return attraction;
        double distanceKm = userLocation.distanceKmTo(attraction.getCoordinates());
        return Attraction.builder()
                .id(attraction.getId())
                .name(attraction.getName())
                .description(attraction.getDescription())
                .category(attraction.getCategory())
                .subcategory(attraction.getSubcategory())
                .coordinates(attraction.getCoordinates())
                .distanceKm(Math.round(distanceKm * 10.0) / 10.0)
                .tags(attraction.getTags())
                .highlights(attraction.getHighlights())
                .bestPeriod(attraction.getBestPeriod())
                .openingHours(attraction.getOpeningHours())
                .entryFee(attraction.getEntryFee())
                .accessibilityInfo(attraction.getAccessibilityInfo())
                .tips(attraction.getTips())
                .address(attraction.getAddress())
                .aiConfidenceScore(attraction.getAiConfidenceScore())
                .build();
    }
}
