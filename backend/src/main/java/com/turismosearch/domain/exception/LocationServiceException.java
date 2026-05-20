package com.turismosearch.domain.exception;

public class LocationServiceException extends RuntimeException {
    public LocationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
