package com.turismosearch.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AttractionResponse {
    String id;
    String name;
    String description;
    String category;
    String categoryDisplayName;
    String subcategory;
    CoordinatesResponse coordinates;
    Double distanceKm;
    List<String> tags;
    List<String> highlights;
    String bestPeriod;
    String openingHours;
    String entryFee;
    String accessibilityInfo;
    List<String> tips;
    String address;
    Double aiConfidenceScore;
}
