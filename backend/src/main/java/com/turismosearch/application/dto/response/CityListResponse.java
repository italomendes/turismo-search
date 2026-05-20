package com.turismosearch.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CityListResponse {
    List<CityResponse> cities;
}
