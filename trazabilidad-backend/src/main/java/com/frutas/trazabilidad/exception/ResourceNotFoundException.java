package com.frutas.trazabilidad.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(HttpStatus.NOT_FOUND, String.format("%s con ID %d no encontrado", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(HttpStatus.NOT_FOUND, String.format("%s '%s' no encontrado", resourceType, identifier));
    }
}