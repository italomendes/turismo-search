package com.turismosearch.application.service;

import com.turismosearch.domain.model.*;
import com.turismosearch.domain.port.inbound.SearchAttractionsUseCase;
import com.turismosearch.domain.port.outbound.AiRecommendationPort;
import com.turismosearch.domain.port.outbound.GeocodingPort;
import com.turismosearch.domain.port.outbound.RealPoiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

        // 1. Resolve center coordinates for Overpass lookup
        Coordinates center = resolveCenter(query);
        if (center == null) {
            log.warn("Não foi possível resolver coordenadas do centro — abortando");
            return List.of();
        }

        // 2. Fetch REAL POIs from OpenStreetMap (source of truth for existence + coordinates)
        List<OverpassPoi> osmPois = realPoiPort.findRealPois(center, query.getRadiusKm());
        log.info("Overpass retornou {} POIs reais para {}", osmPois.size(), cityDisplayName);

        if (osmPois.isEmpty()) {
            log.warn("Nenhum POI real encontrado no OSM para {} — não retornando resultados (evitando alucinações)", cityDisplayName);
            return List.of();
        }

        // 3. Ask AI to ENRICH real OSM POIs (metadata only — AI never invents places)
        SearchQuery enrichedQuery = buildQueryWithOsmContext(query, osmPois);
        List<Attraction> attractions = aiRecommendationPort.recommendAttractions(enrichedQuery, cityDisplayName);

        // 4. CRITICAL: Override AI coordinates with verified OSM coordinates.
        //    Also filters out any AI-hallucinated place not present in OSM list.
        attractions = applyVerifiedOsmCoordinates(attractions, osmPois);
        log.info("Após validação OSM: {} atrações verificadas para {}", attractions.size(), cityDisplayName);

        // 5. Enrich with distance + sort + limit if user has GPS coordinates
        if (query.getUserLocation() != null) {
            attractions = attractions.stream()
                    .map(a -> enrichWithDistance(a, query.getUserLocation()))
                    .sorted((a, b) -> Double.compare(
                            a.getDistanceKm() != null ? a.getDistanceKm() : 999.0,
                            b.getDistanceKm() != null ? b.getDistanceKm() : 999.0))
                    .limit(query.getMaxResults())
                    .collect(Collectors.toList());
        }

        log.info("Retornando {} atrações verificadas para {}", attractions.size(), cityDisplayName);
        return attractions;
    }

    // -----------------------------------------------------------------------
    // OSM coordinate validation and override
    // -----------------------------------------------------------------------

    /**
     * For every AI-returned attraction, find the best-matching OSM POI by name and
     * replace the coordinates with the verified OSM lat/lng.
     * Attractions with no OSM match are discarded (they are AI hallucinations).
     */
    private List<Attraction> applyVerifiedOsmCoordinates(List<Attraction> attractions, List<OverpassPoi> osmPois) {
        return attractions.stream()
                .map(attraction -> {
                    Optional<OverpassPoi> match = findBestOsmMatch(attraction.getName(), osmPois);
                    if (match.isEmpty()) {
                        log.debug("Descartando atração sem correspondência OSM: '{}'", attraction.getName());
                        return null;
                    }
                    OverpassPoi poi = match.get();
                    Coordinates verifiedCoords = Coordinates.of(poi.lat(), poi.lng());
                    return Attraction.builder()
                            .id(attraction.getId())
                            .name(attraction.getName())
                            .description(attraction.getDescription())
                            .category(attraction.getCategory())
                            .subcategory(attraction.getSubcategory())
                            .coordinates(verifiedCoords)   // ← always from OSM
                            .distanceKm(attraction.getDistanceKm())
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
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Find the best matching OSM POI for a given attraction name using
     * normalized string comparison (handles accents, case, punctuation).
     */
    private Optional<OverpassPoi> findBestOsmMatch(String attractionName, List<OverpassPoi> osmPois) {
        String normAttraction = normalizeForMatch(attractionName);

        // 1. Exact match after normalization
        Optional<OverpassPoi> exact = osmPois.stream()
                .filter(p -> normalizeForMatch(p.name()).equals(normAttraction))
                .findFirst();
        if (exact.isPresent()) return exact;

        // 2. One contains the other (handles abbreviations / partial names)
        Optional<OverpassPoi> partial = osmPois.stream()
                .filter(p -> {
                    String normOsm = normalizeForMatch(p.name());
                    return normOsm.contains(normAttraction) || normAttraction.contains(normOsm);
                })
                .findFirst();
        if (partial.isPresent()) return partial;

        // 3. Fuzzy: at least 60% of words in common
        String[] attrWords = normAttraction.split("\\s+");
        return osmPois.stream()
                .filter(p -> {
                    String normOsm = normalizeForMatch(p.name());
                    long matches = java.util.Arrays.stream(attrWords)
                            .filter(w -> w.length() > 3 && normOsm.contains(w))
                            .count();
                    return attrWords.length > 0 && matches >= Math.ceil(attrWords.length * 0.6);
                })
                .findFirst();
    }

    private String normalizeForMatch(String name) {
        if (name == null) return "";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

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

    private SearchQuery buildQueryWithOsmContext(SearchQuery query, List<OverpassPoi> osmPois) {
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
