package com.frutas.trazabilidad.module.produccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Finca.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FincaResponse {

    private Long id;
    private Long empresaId;
    private String empresaNombre;
    private String codigoFinca;
    private String nombre;
    private String ubicacion;
    private String municipio;
    private String departamento;
    private String pais;
    private Double areaHectareas;
    private String propietario;
    private String encargado;
    private String telefono;
    private String email;
    private Double latitud;
    private Double longitud;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información resumida
    private Long totalLotes;
    private Long totalCertificacionesVigentes;
}