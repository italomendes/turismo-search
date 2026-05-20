package com.turismosearch.adapter.outbound.cities.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IbgeMunicipioResponse {
    private Long id;
    private String nome;
    private Microrregiao microrregiao;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Microrregiao {
        private Mesorregiao mesorregiao;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mesorregiao {
        private UF UF;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UF {
        private Long id;
        private String sigla;
        private String nome;
    }

    public String getStateCode() {
        try {
            return microrregiao.getMesorregiao().getUF().getSigla();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
