package com.turismosearch.application.service;

import com.turismosearch.domain.model.*;
import com.turismosearch.domain.port.inbound.SearchAttractionsUseCase;
import com.turismosearch.domain.port.outbound.AiRecommendationPort;
import com.turismosearch.domain.port.outbound.GeocodingPort;
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

    @Override
    public List<Attraction> searchAttractions(SearchQuery query) {
        String cityDisplayName = resolveCityName(query);
        log.info("Buscando atrações para: {} num raio de {}km", cityDisplayName, query.getRadiusKm());

        List<Attraction> attractions = aiRecommendationPort.recommendAttractions(query, cityDisplayName);

        // Enriquecer com distância calculada se temos coordenadas do usuário
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
