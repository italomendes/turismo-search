package com.turismosearch.domain.exception;

public class AttractionNotFoundException extends RuntimeException {
    public AttractionNotFoundException(String id) {
        super("Atração não encontrada: " + id);
    }
}
