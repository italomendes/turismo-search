package com.turismosearch.infrastructure.config;

import com.turismosearch.infrastructure.properties.ClaudeProperties;
import com.turismosearch.infrastructure.properties.IbgeProperties;
import com.turismosearch.infrastructure.properties.NominatimProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final ClaudeProperties claudeProperties;
    private final NominatimProperties nominatimProperties;
    private final IbgeProperties ibgeProperties;

    @Bean("claudeWebClient")
    public WebClient claudeWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(claudeProperties.getTimeoutSeconds()))
                .doOnConnected(conn -> conn.addHandlerLast(
                        new ReadTimeoutHandler(claudeProperties.getTimeoutSeconds(), TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", claudeProperties.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("anthropic-beta", "prompt-caching-2024-07-31")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("nominatimWebClient")
    public WebClient nominatimWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(nominatimProperties.getTimeoutSeconds()));

        return WebClient.builder()
                .baseUrl(nominatimProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("User-Agent", nominatimProperties.getUserAgent())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("ibgeWebClient")
    public WebClient ibgeWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(ibgeProperties.getTimeoutSeconds()));

        return WebClient.builder()
                .baseUrl(ibgeProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
