package com.turismosearch.application.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CoordinatesResponse {
    double latitude;
    double longitude;
}
