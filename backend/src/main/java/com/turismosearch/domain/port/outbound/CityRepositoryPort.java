package com.turismosearch.domain.port.outbound;

import com.turismosearch.domain.model.City;

import java.util.List;

public interface CityRepositoryPort {
    List<City> findCitiesByState(String stateCode);
    List<City> findStates();
}
