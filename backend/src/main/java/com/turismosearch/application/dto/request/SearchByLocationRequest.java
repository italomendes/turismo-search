package com.turismosearch.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SearchByLocationRequest {

    @NotNull(message = "Latitude é obrigatória")
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Longitude é obrigatória")
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;

    @DecimalMin(value = "1.0") @DecimalMax(value = "300.0")
    private Double radiusKm = 50.0;

    private List<String> categories;

    @Min(1) @Max(50)
    private Integer maxResults = 20;
}
