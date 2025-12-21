package com.frutas.trazabilidad.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends CustomException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public ConflictException(String resourceType, String field, String value) {
        super(HttpStatus.CONFLICT, String.format("%s con %s '%s' ya existe", resourceType, field, value));
    }
}