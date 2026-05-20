package com.turismosearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TurismoSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(TurismoSearchApplication.class, args);
    }
}
