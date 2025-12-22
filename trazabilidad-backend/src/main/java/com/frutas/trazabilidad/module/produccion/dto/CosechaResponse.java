package com.frutas.trazabilidad.module.produccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Cosecha.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosechaResponse {

    private Long id;
    private Long loteId;
    private String loteNombre;
    private String loteCodigoLote;
    private LocalDate fechaCosecha;
    private Double cantidadCosechada;
    private String unidadMedida;
    private String calidadInicial;
    private String estadoFruta;
    private String responsableCosecha;
    private Integer numeroTrabajadores;
    private String horaInicio;
    private String horaFin;
    private Double temperaturaAmbiente;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información calculada
    private Double rendimientoPorHectarea;
    private Boolean reciente;
}