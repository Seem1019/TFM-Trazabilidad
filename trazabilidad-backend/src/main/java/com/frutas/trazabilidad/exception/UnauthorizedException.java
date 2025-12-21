package com.frutas.trazabilidad.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED, "No autorizado. Credenciales inválidas o token expirado");
    }
}