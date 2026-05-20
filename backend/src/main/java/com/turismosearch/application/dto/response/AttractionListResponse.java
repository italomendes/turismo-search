package com.turismosearch.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AttractionListResponse {
    String city;
    String state;
    int totalFound;
    List<AttractionResponse> attractions;
}
