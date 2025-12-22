package com.frutas.trazabilidad.module.produccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Actividad Agronómica.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActividadAgronomicarResponse {

    private Long id;
    private Long loteId;
    private String loteNombre;
    private String loteCodigoLote;
    private String tipoActividad;
    private LocalDate fechaActividad;
    private String productoAplicado;
    private String dosisoCantidad;
    private String unidadMedida;
    private String metodoAplicacion;
    private String responsable;
    private String numeroRegistroProducto;
    private Integer intervaloSeguridadDias;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información calculada
    private LocalDate fechaFinIntervaloSeguridad;
    private Boolean intervaloSeguridadCumplido;
}