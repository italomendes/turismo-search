package com.turismosearch.adapter.outbound.overpass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverpassResponse {

    @JsonProperty("elements")
    private List<Element> elements;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Element {

        @JsonProperty("type")
        private String type;  // "node" or "way"

        @JsonProperty("id")
        private Long id;

        @JsonProperty("lat")
        private Double lat;

        @JsonProperty("lon")
        private Double lon;

        /** For way elements, Overpass returns center when "out center" is used */
        @JsonProperty("center")
        private Center center;

        @JsonProperty("tags")
        private Map<String, String> tags;

        /** Effective latitude (node lat or way center lat) */
        public double effectiveLat() {
            if (lat != null) return lat;
            if (center != null) return center.getLat();
            return 0.0;
        }

        /** Effective longitude (node lon or way center lon) */
        public double effectiveLon() {
            if (lon != null) return lon;
            if (center != null) return center.getLon();
            return 0.0;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Center {
        @JsonProperty("lat")
        private double lat;
        @JsonProperty("lon")
        private double lon;
    }
}
