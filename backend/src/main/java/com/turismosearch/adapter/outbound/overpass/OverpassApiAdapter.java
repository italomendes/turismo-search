package com.turismosearch.adapter.outbound.overpass;

import com.turismosearch.domain.model.Coordinates;
import com.turismosearch.domain.model.OverpassPoi;
import com.turismosearch.domain.port.outbound.RealPoiPort;
import com.turismosearch.infrastructure.properties.OverpassProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class OverpassApiAdapter implements RealPoiPort {

    private static final String USER_AGENT = "TurismoSearch/1.0 (educational tourism app)";

    private final List<String> mirrorUrls;
    private final int timeoutSeconds;

    private static final List<String> OSM_TYPE_PRIORITY = List.of(
            "tourism", "natural", "historic", "leisure", "amenity"
    );

    public OverpassApiAdapter(OverpassProperties properties) {
        this.timeoutSeconds = properties.getTimeoutSeconds();
        this.mirrorUrls = Stream.concat(
                Stream.of(properties.getBaseUrl()),
                properties.getFallbackUrls().stream()
        ).collect(Collectors.toList());
    }

    @Override
    public List<OverpassPoi> findRealPois(Coordinates center, double radiusKm) {
        int radiusMeters = (int) (radiusKm * 1000);
        double lat = center.getLatitude();
        double lng = center.getLongitude();
        String query = buildQuery(radiusMeters, lat, lng);
        String encodedData = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        for (String mirrorUrl : mirrorUrls) {
            try {
                log.info("Consultando Overpass [{}]: centro=({},{}) raio={}m", mirrorUrl, lat, lng, radiusMeters);
                List<OverpassPoi> pois = fetchFromMirror(mirrorUrl, encodedData);
                log.info("Overpass retornou {} POIs únicos via {}", pois.size(), mirrorUrl);
                return pois;
            } catch (Exception e) {
                log.warn("Overpass mirror {} falhou: {} — tentando próximo...", mirrorUrl, e.getMessage());
            }
        }

        log.error("Todos os mirrors Overpass falharam para ({},{})", lat, lng);
        return Collections.emptyList();
    }

    private List<OverpassPoi> fetchFromMirror(String baseUrl, String encodedData) {
        // Force HTTP/1.1 — avoids h2/alpn negotiation issues with some Overpass mirrors
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.HTTP11)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 8000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds))
                .doOnConnected(conn -> conn.addHandlerLast(
                        new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS)));

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        OverpassResponse response = client.post()
                .bodyValue(encodedData)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", USER_AGENT)
                .retrieve()
                .bodyToMono(OverpassResponse.class)
                .block();

        if (response == null || response.getElements() == null) {
            return Collections.emptyList();
        }

        return response.getElements().stream()
                .filter(el -> el.getTags() != null)
                .filter(el -> hasRelevantName(el.getTags()))
                .filter(el -> el.effectiveLat() != 0.0 || el.effectiveLon() != 0.0)
                .map(el -> {
                    Map<String, String> tags = el.getTags();
                    String osmType = resolveOsmType(tags);
                    String osmValue = resolveOsmValue(tags, osmType);
                    return new OverpassPoi(
                            tags.get("name"),
                            el.effectiveLat(),
                            el.effectiveLon(),
                            osmType,
                            osmValue,
                            tags
                    );
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                p -> p.name().toLowerCase().trim(),
                                p -> p,
                                (a, b) -> a
                        ),
                        m -> new ArrayList<>(m.values())
                ));
    }

    /**
     * Overpass QL query using individual tag filters (proven reliable pattern).
     * Covers tourism, natural features, historic sites, leisure areas in Brazil.
     * Uses `out body center` to get center coordinates for way elements.
     */
    private String buildQuery(int radiusMeters, double lat, double lng) {
        return String.format("""
                [out:json][timeout:25];
                (
                  node["tourism"](around:%1$d,%2$.6f,%3$.6f);
                  way["tourism"](around:%1$d,%2$.6f,%3$.6f);
                  node["natural"="waterfall"](around:%1$d,%2$.6f,%3$.6f);
                  node["natural"="cave_entrance"](around:%1$d,%2$.6f,%3$.6f);
                  node["natural"="peak"](around:%1$d,%2$.6f,%3$.6f);
                  node["natural"="beach"](around:%1$d,%2$.6f,%3$.6f);
                  node["natural"="hot_spring"](around:%1$d,%2$.6f,%3$.6f);
                  way["natural"="beach"](around:%1$d,%2$.6f,%3$.6f);
                  way["natural"="cave_entrance"](around:%1$d,%2$.6f,%3$.6f);
                  node["historic"](around:%1$d,%2$.6f,%3$.6f);
                  way["historic"](around:%1$d,%2$.6f,%3$.6f);
                  way["leisure"="park"](around:%1$d,%2$.6f,%3$.6f);
                  way["leisure"="nature_reserve"](around:%1$d,%2$.6f,%3$.6f);
                  node["leisure"="nature_reserve"](around:%1$d,%2$.6f,%3$.6f);
                );
                out body center;
                """,
                radiusMeters, lat, lng);
    }

    private boolean hasRelevantName(Map<String, String> tags) {
        String name = tags.get("name");
        return name != null && !name.isBlank() && name.length() > 2;
    }

    private String resolveOsmType(Map<String, String> tags) {
        for (String type : OSM_TYPE_PRIORITY) {
            if (tags.containsKey(type)) return type;
        }
        return "other";
    }

    private String resolveOsmValue(Map<String, String> tags, String osmType) {
        String value = tags.get(osmType);
        if (value != null && !value.isBlank() && !value.equals("yes")) return value;
        for (String key : tags.keySet()) {
            String v = tags.get(key);
            if (v != null && !v.equals("yes") && !v.isBlank()) return v;
        }
        return "attraction";
    }
}
