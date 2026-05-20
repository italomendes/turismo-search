package com.turismosearch.domain.model;

public enum AttractionCategory {
    WATERFALL("Cachoeira"),
    TRAIL("Trilha"),
    PARK("Parque / Área Natural"),
    BEACH("Praia / Lago"),
    HISTORICAL("Patrimônio Histórico"),
    CULTURAL("Cultura / Arte"),
    GASTRONOMIC("Gastronomia"),
    INN("Pousada / Hospedagem"),
    CAVE("Gruta / Caverna"),
    ADVENTURE("Aventura / Esportes"),
    RELIGIOUS("Religioso"),
    OTHER("Outros");

    private final String displayName;

    AttractionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
