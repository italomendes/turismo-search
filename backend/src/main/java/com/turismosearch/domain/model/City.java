package com.turismosearch.domain.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = City.CityBuilder.class)
public class City {
    String ibgeCode;
    String name;
    String stateCode;
    String stateName;
    Coordinates coordinates;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CityBuilder {}
}
