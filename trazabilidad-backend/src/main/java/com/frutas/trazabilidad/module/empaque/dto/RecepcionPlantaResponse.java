package com.frutas.trazabilidad.module.empaque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para RecepcionPlanta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionPlantaResponse {

    private Long id;
    private Long loteId;
    private String loteNombre;
    private String loteCodigoLote;
    private String fincaNombre;
    private String codigoRecepcion;
    private LocalDate fechaRecepcion;
    private String horaRecepcion;
    private Double cantidadRecibida;
    private String unidadMedida;
    private Double temperaturaFruta;
    private String estadoInicial;
    private String responsableRecepcion;
    private String vehiculoTransporte;
    private String conductor;
    private String observaciones;
    private String estadoRecepcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información calculada
    private Integer totalClasificaciones;
}