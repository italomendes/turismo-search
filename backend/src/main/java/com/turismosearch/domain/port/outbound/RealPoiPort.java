package com.turismosearch.domain.port.outbound;

import com.turismosearch.domain.model.Coordinates;
import com.turismosearch.domain.model.OverpassPoi;

import java.util.List;

public interface RealPoiPort {
    List<OverpassPoi> findRealPois(Coordinates center, double radiusKm);
}
