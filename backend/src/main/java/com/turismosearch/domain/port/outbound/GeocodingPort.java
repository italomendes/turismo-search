package com.turismosearch.domain.port.outbound;

import com.turismosearch.domain.model.City;
import com.turismosearch.domain.model.Coordinates;

import java.util.Optional;

public interface GeocodingPort {
    /**
     * Converte coordenadas (GPS) em nome de cidade
     */
    Optional<City> reverseGeocode(Coordinates coordinates);

    /**
     * Converte nome de cidade em coordenadas
     */
    Optional<Coordinates> geocodeCity(String cityName, String stateCode);
}
