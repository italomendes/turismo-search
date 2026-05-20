package com.turismosearch.adapter.inbound.rest;

import com.turismosearch.application.dto.response.CityListResponse;
import com.turismosearch.application.dto.response.CityResponse;
import com.turismosearch.domain.port.inbound.GetCitiesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
@Tag(name = "Cidades", description = "Busca de cidades e estados via IBGE")
public class CityController {

    private final GetCitiesUseCase getCitiesUseCase;

    @GetMapping("/states")
    @Operation(summary = "Listar todos os estados brasileiros")
    public ResponseEntity<CityListResponse> getStates() {
        var states = getCitiesUseCase.getStates().stream()
                .map(c -> CityResponse.builder()
                        .ibgeCode(c.getIbgeCode())
                        .name(c.getName())
                        .stateCode(c.getStateCode())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(CityListResponse.builder().cities(states).build());
    }

    @GetMapping
    @Operation(summary = "Autocomplete de municípios por estado")
    public ResponseEntity<CityListResponse> getCities(
            @RequestParam String stateCode,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "10") int limit) {

        var cities = getCitiesUseCase.getCitiesByState(stateCode, query, limit).stream()
                .map(c -> CityResponse.builder()
                        .ibgeCode(c.getIbgeCode())
                        .name(c.getName())
                        .stateCode(c.getStateCode())
                        .stateName(c.getStateName())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(CityListResponse.builder().cities(cities).build());
    }
}
