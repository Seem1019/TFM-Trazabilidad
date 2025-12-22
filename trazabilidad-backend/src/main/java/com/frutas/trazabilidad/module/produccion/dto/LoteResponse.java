package com.frutas.trazabilidad.module.produccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Lote.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteResponse {

    private Long id;
    private Long fincaId;
    private String fincaNombre;
    private String codigoLote;
    private String nombre;
    private String tipoFruta;
    private String variedad;
    private Double areaHectareas;
    private LocalDate fechaSiembra;
    private LocalDate fechaPrimeraCosechaEstimada;
    private Integer densidadSiembra;
    private String ubicacionInterna;
    private String estadoLote;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información calculada
    private Long edadEnDias;
    private Boolean listoParaCosechar;
    private Double totalCosechado;
    private Long totalActividades;
}