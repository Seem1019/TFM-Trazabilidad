package com.frutas.trazabilidad.module.produccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Certificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificacionResponse {

    private Long id;
    private Long fincaId;
    private String fincaNombre;
    private String tipoCertificacion;
    private String entidadEmisora;
    private String numeroCertificado;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String estado; // VIGENTE, VENCIDA, SUSPENDIDA
    private String urlDocumento;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información calculada
    private Boolean vigente;
    private Long diasParaVencer;
}