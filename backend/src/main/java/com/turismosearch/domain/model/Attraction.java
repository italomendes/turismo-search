package com.turismosearch.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class Attraction {
    @Builder.Default
    String id = UUID.randomUUID().toString();

    String name;
    String description;
    AttractionCategory category;
    String subcategory;
    Coordinates coordinates;
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
