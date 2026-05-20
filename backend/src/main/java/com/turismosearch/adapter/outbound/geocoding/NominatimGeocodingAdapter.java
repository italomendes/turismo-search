package com.turismosearch.adapter.outbound.geocoding;

import com.turismosearch.adapter.outbound.geocoding.dto.NominatimResponse;
import com.turismosearch.domain.model.City;
import com.turismosearch.domain.model.Coordinates;
import com.turismosearch.domain.port.outbound.GeocodingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NominatimGeocodingAdapter implements GeocodingPort {

    private final WebClient nominatimWebClient;

    @Override
    @Cacheable(value = "geocoding-reverse", key = "#coordinates.latitude + ',' + #coordinates.longitude")
    public Optional<City> reverseGeocode(Coordinates coordinates) {
        log.debug("Reverse geocoding: {},{}", coordinates.getLatitude(), coordinates.getLongitude());
        try {
            NominatimResponse response = nominatimWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reverse")
                            .queryParam("lat", coordinates.getLatitude())
                            .queryParam("lon", coordinates.getLongitude())
                            .queryParam("format", "json")
                            .queryParam("accept-language", "pt-BR")
                            .build())
                    .retrieve()
                    .bodyToMono(NominatimResponse.class)
                    .block();

            if (response == null || response.getAddress() == null) return Optional.empty();

            return Optional.of(City.builder()
                    .name(response.getAddress().getCityName())
                    .stateCode(response.getAddress().getStateCode())
                    .coordinates(Coordinates.of(
                            Double.parseDouble(response.getLat()),
                            Double.parseDouble(response.getLon())))
                    .build());

        } catch (Exception e) {
            log.warn("Erro no reverse geocoding: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Cacheable(value = "geocoding-forward", key = "#cityName + '-' + #stateCode")
    public Optional<Coordinates> geocodeCity(String cityName, String stateCode) {
        log.debug("Geocoding city: {}, {}", cityName, stateCode);
        try {
            List<NominatimResponse> responses = nominatimWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", cityName + ", " + stateCode + ", Brasil")
                            .queryParam("format", "json")
                            .queryParam("limit", "1")
                            .queryParam("countrycodes", "br")
                            .build())
                    .retrieve()
                    .bodyToFlux(NominatimResponse.class)
                    .collectList()
                    .block();

            if (responses == null || responses.isEmpty()) return Optional.empty();

            NominatimResponse first = responses.get(0);
            return Optional.of(Coordinates.of(
                    Double.parseDouble(first.getLat()),
                    Double.parseDouble(first.getLon())));

        } catch (Exception e) {
            log.warn("Erro no geocoding de cidade: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
