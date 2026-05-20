package com.turismosearch.domain.port.inbound;

import com.turismosearch.domain.model.Attraction;
import com.turismosearch.domain.model.SearchQuery;

import java.util.List;

public interface SearchAttractionsUseCase {
    List<Attraction> searchAttractions(SearchQuery query);
}
