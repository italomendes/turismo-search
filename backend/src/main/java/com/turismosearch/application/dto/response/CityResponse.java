package com.turismosearch.application.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CityResponse {
    String ibgeCode;
    String name;
    String stateCode;
    String stateName;
}
