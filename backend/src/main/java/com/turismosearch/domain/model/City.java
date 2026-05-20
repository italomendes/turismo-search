package com.turismosearch.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class City {
    String ibgeCode;
    String name;
    String stateCode;
    String stateName;
    Coordinates coordinates;
}
