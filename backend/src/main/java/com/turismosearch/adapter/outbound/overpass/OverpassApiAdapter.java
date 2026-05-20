package com.turismosearch.adapter.outbound.overpass;

import com.turismosearch.domain.model.Coordinates;
import com.turismosearch.domain.model.OverpassPoi;
import com.turismosearch.domain.port.outbound.RealPoiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverpassApiAdapter implements RealPoiPort {

    private final WebClient overpassWebClient;

    private static final List<String> OSM_TYPE_PRIORITY = List.of(
            "tourism", "natural", "historic", "leisure"
    );

    @Override
    public List<OverpassPoi> findRealPois(Coordinates center, double radiusKm) {
        int radiusMeters = (int) (radiusKm * 1000);
        double lat = center.getLatitude();
        double lng = center.getLongitude();

        String query = buildQuery(radiusMeters, lat, lng);
        log.info("Consultando Overpass API: centro=({},{}) raio={}m", lat, lng, radiusMeters);

        try {
            OverpassResponse response = overpassWebClient.post()
                    .bodyValue("data=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .retrieve()
                    .bodyToMono(OverpassResponse.class)
                    .block();

            if (response == null || response.getElements() == null) {
                log.warn("Overpass API retornou resposta vazia");
                return Collections.emptyList();
            }

            List<OverpassPoi> pois = new ArrayList<>();
            for (OverpassResponse.Element el : response.getElements()) {
                Map<String, String> tags = el.getTags();
                if (tags == null) continue;

                String name = tags.get("name");
                if (name == null || name.isBlank()) continue;

                double elLat = el.effectiveLat();
                double elLon = el.effectiveLon();
                if (elLat == 0.0 && elLon == 0.0) continue;

                String osmType = resolveOsmType(tags);
                String osmValue = resolveOsmValue(tags, osmType);

                pois.add(new OverpassPoi(name, elLat, elLon, osmType, osmValue, tags));
            }

            log.info("Overpass API retornou {} POIs reais com nome", pois.size());
            return pois;

        } catch (Exception e) {
            log.error("Erro ao consultar Overpass API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private String buildQuery(int radiusMeters, double lat, double lng) {
        return String.format("""
                [out:json][timeout:30];
                (
                  node["tourism"](around:%d,%.6f,%.6f);
                  way["tourism"](around:%d,%.6f,%.6f);
                  node["natural"="waterfall"](around:%d,%.6f,%.6f);
                  node["natural"="peak"](around:%d,%.6f,%.6f);
                  node["leisure"="nature_reserve"](around:%d,%.6f,%.6f);
                  way["leisure"="park"](around:%d,%.6f,%.6f);
                  way["leisure"="nature_reserve"](around:%d,%.6f,%.6f);
                  node["historic"](around:%d,%.6f,%.6f);
                  way["historic"](around:%d,%.6f,%.6f);
                  node["amenity"="place_of_worship"]["heritage"](around:%d,%.6f,%.6f);
                );
                out body center 30;
                """,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng,
                radiusMeters, lat, lng
        );
    }

    private String resolveOsmType(Map<String, String> tags) {
        for (String type : OSM_TYPE_PRIORITY) {
            if (tags.containsKey(type)) return type;
        }
        return "other";
    }

    private String resolveOsmValue(Map<String, String> tags, String osmType) {
        String value = tags.get(osmType);
        if (value != null && !value.isBlank()) return value;
        // Fallback: check known keys
        for (String key : tags.keySet()) {
            String v = tags.get(key);
            if (v != null && !v.equals("yes")) return v;
        }
        return "attraction";
    }
}
