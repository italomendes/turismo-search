package com.turismosearch.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Coordinates {
    double latitude;
    double longitude;

    public static Coordinates of(double latitude, double longitude) {
        return Coordinates.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    /**
     * Calcula distância em km usando fórmula de Haversine
     */
    public double distanceKmTo(Coordinates other) {
        final double R = 6371.0;
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
