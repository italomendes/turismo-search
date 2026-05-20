package com.turismosearch.adapter.inbound.rest;

import com.turismosearch.application.dto.request.SearchByCityRequest;
import com.turismosearch.application.dto.request.SearchByLocationRequest;
import com.turismosearch.application.dto.response.AttractionListResponse;
import com.turismosearch.application.dto.response.AttractionResponse;
import com.turismosearch.application.dto.response.CoordinatesResponse;
import com.turismosearch.domain.model.*;
import com.turismosearch.domain.port.inbound.SearchAttractionsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/attractions")
@RequiredArgsConstructor
@Tag(name = "Atrações Turísticas", description = "Busca de atrações turísticas via IA")
public class AttractionController {

    private final SearchAttractionsUseCase searchAttractionsUseCase;

    @PostMapping("/search/by-location")
    @Operation(summary = "Buscar atrações por coordenadas GPS")
    public ResponseEntity<AttractionListResponse> searchByLocation(
            @Valid @RequestBody SearchByLocationRequest request) {

        SearchQuery query = SearchQuery.builder()
                .userLocation(Coordinates.of(request.getLatitude(), request.getLongitude()))
                .radiusKm(request.getRadiusKm())
                .categories(parseCategories(request.getCategories()))
                .maxResults(request.getMaxResults())
                .build();

        List<Attraction> attractions = searchAttractionsUseCase.searchAttractions(query);
        return ResponseEntity.ok(toResponse(attractions, null, null));
    }

    @PostMapping("/search/by-city")
    @Operation(summary = "Buscar atrações por cidade")
    public ResponseEntity<AttractionListResponse> searchByCity(
            @Valid @RequestBody SearchByCityRequest request) {

        SearchQuery query = SearchQuery.builder()
                .cityName(request.getCityName())
                .stateCode(request.getStateCode())
                .ibgeCityCode(request.getIbgeCityCode())
                .radiusKm(request.getRadiusKm())
                .categories(parseCategories(request.getCategories()))
                .maxResults(request.getMaxResults())
                .build();

        List<Attraction> attractions = searchAttractionsUseCase.searchAttractions(query);
        return ResponseEntity.ok(toResponse(attractions, request.getCityName(), request.getStateCode()));
    }

    private List<AttractionCategory> parseCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) return null;
        return categories.stream()
                .map(c -> {
                    try { return AttractionCategory.valueOf(c.toUpperCase()); }
                    catch (Exception e) { return null; }
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    private AttractionListResponse toResponse(List<Attraction> attractions, String city, String state) {
        return AttractionListResponse.builder()
                .city(city)
                .state(state)
                .totalFound(attractions.size())
                .attractions(attractions.stream().map(this::toAttractionResponse).collect(Collectors.toList()))
                .build();
    }

    private AttractionResponse toAttractionResponse(Attraction a) {
        CoordinatesResponse coords = a.getCoordinates() != null
                ? CoordinatesResponse.builder()
                    .latitude(a.getCoordinates().getLatitude())
                    .longitude(a.getCoordinates().getLongitude())
                    .build()
                : null;

        return AttractionResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .description(a.getDescription())
                .category(a.getCategory() != null ? a.getCategory().name() : null)
                .categoryDisplayName(a.getCategory() != null ? a.getCategory().getDisplayName() : null)
                .subcategory(a.getSubcategory())
                .coordinates(coords)
                .distanceKm(a.getDistanceKm())
                .tags(a.getTags())
                .highlights(a.getHighlights())
                .bestPeriod(a.getBestPeriod())
                .openingHours(a.getOpeningHours())
                .entryFee(a.getEntryFee())
                .accessibilityInfo(a.getAccessibilityInfo())
                .tips(a.getTips())
                .address(a.getAddress())
                .aiConfidenceScore(a.getAiConfidenceScore())
                .build();
    }
}
