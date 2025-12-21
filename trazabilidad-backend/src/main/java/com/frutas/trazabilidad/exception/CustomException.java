package com.frutas.trazabilidad.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    protected CustomException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    protected CustomException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.message = message;
    }
}