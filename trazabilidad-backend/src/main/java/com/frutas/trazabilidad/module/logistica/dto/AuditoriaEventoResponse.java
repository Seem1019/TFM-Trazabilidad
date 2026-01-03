package com.frutas.trazabilidad.module.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de eventos de auditoría.
 * Solo lectura - no se permite crear/modificar directamente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaEventoResponse {

    private Long id;

    // Usuario
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;

    // Entidad afectada
    private String tipoEntidad;
    private Long entidadId;
    private String codigoEntidad;

    // Operación
    private String tipoOperacion;
    private String descripcionOperacion;

    // Datos (JSON)
    private String datosAnteriores;
    private String datosNuevos;
    private String camposModificados;

    // Blockchain
    private String hashEvento;
    private String hashAnterior;
    private Boolean enCadena;
    private Boolean integridadVerificada;

    // Contexto
    private String ipOrigen;
    private String userAgent;

    // Empresa
    private Long empresaId;
    private String empresaNombre;

    // Clasificación
    private String modulo;
    private String nivelCriticidad;
    private Boolean esCritico;

    // Timestamp
    private LocalDateTime fechaEvento;
}