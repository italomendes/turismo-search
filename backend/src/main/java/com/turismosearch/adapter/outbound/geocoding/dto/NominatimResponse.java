package com.turismosearch.adapter.outbound.geocoding.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimResponse {
    @JsonProperty("place_id")
    private Long placeId;

    @JsonProperty("display_name")
    private String displayName;

    private String lat;
    private String lon;

    private Address address;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String city;
        private String town;
        private String village;
        private String municipality;

        @JsonProperty("state_district")
        private String stateDistrict;

        private String state;

        @JsonProperty("country_code")
        private String countryCode;

        public String getCityName() {
            if (city != null) return city;
            if (town != null) return town;
            if (village != null) return village;
            return municipality;
        }

        public String getStateCode() {
            // Nominatim retorna nome completo do estado; precisamos mapear para sigla
            return state;
        }
    }
}
