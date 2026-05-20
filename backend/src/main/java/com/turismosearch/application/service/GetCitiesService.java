package com.turismosearch.application.service;

import com.turismosearch.domain.model.City;
import com.turismosearch.domain.port.inbound.GetCitiesUseCase;
import com.turismosearch.domain.port.outbound.CityRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCitiesService implements GetCitiesUseCase {

    private final CityRepositoryPort cityRepositoryPort;

    @Override
    public List<City> getCitiesByState(String stateCode, String query, int limit) {
        List<City> cities = cityRepositoryPort.findCitiesByState(stateCode.toUpperCase());
        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase().trim();
            cities = cities.stream()
                    .filter(c -> normalize(c.getName()).contains(normalize(q)))
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        return cities.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<City> getStates() {
        return cityRepositoryPort.findStates();
    }

    private String normalize(String s) {
        return s.toLowerCase()
                .replaceAll("[áàãâä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòõôö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c");
    }
}
