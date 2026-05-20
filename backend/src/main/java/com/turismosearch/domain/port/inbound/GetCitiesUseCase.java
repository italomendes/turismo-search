package com.turismosearch.domain.port.inbound;

import com.turismosearch.domain.model.City;

import java.util.List;

public interface GetCitiesUseCase {
    List<City> getCitiesByState(String stateCode, String query, int limit);
    List<City> getStates();
}
