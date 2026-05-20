package com.turismosearch.domain.port.inbound;

import com.turismosearch.domain.model.Attraction;

import java.util.Optional;

public interface GetAttractionDetailsUseCase {
    Optional<Attraction> getAttractionById(String id);
}
