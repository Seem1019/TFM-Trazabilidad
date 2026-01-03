package com.frutas.trazabilidad.module.empaque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Clasificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionResponse {

    private Long id;
    private Long recepcionId;
    private String recepcionCodigo;
    private String loteNombre;
    private String codigoClasificacion;
    private LocalDate fechaClasificacion;
    private String calidad;
    private Double cantidadClasificada;
    private String unidadMedida;
    private String calibre;
    private Double porcentajeMerma;
    private Double cantidadMerma;
    private String motivoMerma;
    private String responsableClasificacion;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información calculada
    private Integer totalEtiquetas;
    private Integer totalControlesCalidad;
}