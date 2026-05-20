package com.turismosearch.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SearchQuery {
    Coordinates userLocation;
    String cityName;
    String stateCode;
    String ibgeCityCode;
    @Builder.Default
    double radiusKm = 50.0;
    List<AttractionCategory> categories;
    @Builder.Default
    int maxResults = 20;
    /** POIs reais obtidos do OpenStreetMap via Overpass API (pode ser null/vazio) */
    List<OverpassPoi> osmPois;
}
