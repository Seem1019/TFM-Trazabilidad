package com.frutas.trazabilidad.module.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO para respuestas de eventos logísticos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoLogisticoResponse {

    private Long id;

    // Envío asociado
    private Long envioId;
    private String codigoEnvio;

    // Identificación del evento
    private String codigoEvento;
    private String tipoEvento;

    // Fecha y hora
    private LocalDate fechaEvento;
    private LocalTime horaEvento;
    private LocalDateTime fechaHoraEvento;

    // Ubicación
    private String ubicacion;
    private String ciudad;
    private String pais;
    private Double latitud;
    private Double longitud;

    // Responsables
    private String responsable;
    private String organizacion;

    // Condiciones registradas
    private Double temperaturaRegistrada;
    private Double humedadRegistrada;

    // Transporte
    private String vehiculo;
    private String conductor;
    private String numeroPrecinto;

    // Detalles
    private String observaciones;
    private String urlEvidencia;

    // Incidencias
    private Boolean incidencia;
    private String detalleIncidencia;

    // Metadata
    private Boolean activo;
    private LocalDateTime createdAt;
}