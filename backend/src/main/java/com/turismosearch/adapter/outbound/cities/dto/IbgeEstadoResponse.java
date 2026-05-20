package com.turismosearch.adapter.outbound.cities.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IbgeEstadoResponse {
    private Long id;
    private String sigla;
    private String nome;
}
