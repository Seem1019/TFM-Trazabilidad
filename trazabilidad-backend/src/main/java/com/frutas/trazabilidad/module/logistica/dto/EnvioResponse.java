package com.frutas.trazabilidad.module.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de envío.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvioResponse {

    private Long id;
    private String codigoEnvio;

    // Usuario creador
    private Long usuarioId;
    private String usuarioNombre;
    private Long empresaId;
    private String empresaNombre;

    // Fechas
    private LocalDate fechaCreacion;
    private LocalDate fechaSalidaEstimada;
    private LocalDate fechaSalidaReal;

    // Destino
    private String exportador;
    private String paisDestino;
    private String puertoDestino;
    private String ciudadDestino;

    // Transporte
    private String tipoTransporte;
    private String codigoContenedor;
    private String tipoContenedor;
    private Double temperaturaContenedor;
    private String transportista;
    private String numeroBooking;
    private String numeroBL;

    // Estado
    private String estado;

    // Totales calculados
    private Double pesoNetoTotal;
    private Double pesoBrutoTotal;
    private Integer numeroPallets;
    private Integer numeroCajas;

    // Comercial
    private String observaciones;
    private String clienteImportador;
    private String incoterm;

    // Blockchain
    private String hashCierre;
    private LocalDateTime fechaCierre;
    private String usuarioCierreNombre;

    // Metadata
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Contadores relacionados
    private Long numeroEventos;
    private Long numeroDocumentos;
}