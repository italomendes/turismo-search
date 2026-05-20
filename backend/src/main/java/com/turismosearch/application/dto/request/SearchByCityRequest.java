package com.turismosearch.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SearchByCityRequest {

    private String ibgeCityCode;

    @NotBlank(message = "Nome da cidade é obrigatório")
    private String cityName;

    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2)
    private String stateCode;

    @DecimalMin(value = "1.0") @DecimalMax(value = "300.0")
    private Double radiusKm = 50.0;

    private List<String> categories;

    @Min(1) @Max(50)
    private Integer maxResults = 20;
}
