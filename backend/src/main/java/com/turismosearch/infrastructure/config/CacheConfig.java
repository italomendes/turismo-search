package com.turismosearch.infrastructure.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration("attractions",
                        cacheConfig(Duration.ofHours(24)))
                .withCacheConfiguration("ibge-cities",
                        cacheConfig(Duration.ofDays(7)))
                .withCacheConfiguration("ibge-states",
                        cacheConfig(Duration.ofDays(7)))
                .withCacheConfiguration("geocoding-reverse",
                        cacheConfig(Duration.ofDays(7)))
                .withCacheConfiguration("geocoding-forward",
                        cacheConfig(Duration.ofDays(7)));
    }

    private RedisCacheConfiguration cacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
