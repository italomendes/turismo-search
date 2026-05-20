package com.turismosearch.domain.model;

import java.util.Map;

public record OverpassPoi(
    String name,
    double lat,
    double lng,
    String osmType,   // "tourism", "natural", "historic", "leisure"
    String osmValue,  // "waterfall", "museum", "attraction", etc.
    Map<String, String> tags
) {}
