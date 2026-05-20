package com.turismosearch.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@JsonDeserialize(builder = Attraction.AttractionBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonPOJOBuilder(withPrefix = "")
    public static class AttractionBuilder {}
}
