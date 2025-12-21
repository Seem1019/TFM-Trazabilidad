package com.frutas.trazabilidad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {

    private String field;
    private String message;
    private Object rejectedValue;

    public ErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }
}