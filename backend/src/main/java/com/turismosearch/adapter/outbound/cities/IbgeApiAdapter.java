package com.turismosearch.adapter.outbound.cities;

import com.turismosearch.adapter.outbound.cities.dto.IbgeEstadoResponse;
import com.turismosearch.adapter.outbound.cities.dto.IbgeMunicipioResponse;
import com.turismosearch.domain.model.City;
import com.turismosearch.domain.port.outbound.CityRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class IbgeApiAdapter implements CityRepositoryPort {

    private final WebClient ibgeWebClient;

    @Override
    @Cacheable(value = "ibge-cities", key = "#stateCode")
    public List<City> findCitiesByState(String stateCode) {
        log.debug("Buscando municípios do estado: {} (cache MISS)", stateCode);
        try {
            List<IbgeMunicipioResponse> municipios = ibgeWebClient.get()
                    .uri("/localidades/estados/{uf}/municipios?orderBy=nome", stateCode)
                    .retrieve()
                    .bodyToFlux(IbgeMunicipioResponse.class)
                    .collectList()
                    .block();

            if (municipios == null) return List.of();

            return municipios.stream()
                    .map(m -> City.builder()
                            .ibgeCode(String.valueOf(m.getId()))
                            .name(m.getNome())
                            .stateCode(stateCode)
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erro ao buscar municípios do IBGE para {}: {}", stateCode, e.getMessage());
            return List.of();
        }
    }

    @Override
    @Cacheable(value = "ibge-states")
    public List<City> findStates() {
        log.debug("Buscando estados do IBGE (cache MISS)");
        try {
            List<IbgeEstadoResponse> estados = ibgeWebClient.get()
                    .uri("/localidades/estados?orderBy=nome")
                    .retrieve()
                    .bodyToFlux(IbgeEstadoResponse.class)
                    .collectList()
                    .block();

            if (estados == null) return List.of();

            return estados.stream()
                    .map(e -> City.builder()
                            .ibgeCode(String.valueOf(e.getId()))
                            .name(e.getNome())
                            .stateCode(e.getSigla())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erro ao buscar estados do IBGE: {}", e.getMessage());
            return List.of();
        }
    }
}
