package com.frutas.trazabilidad.module.empaque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Control de Calidad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlCalidadResponse {

    private Long id;
    private Long clasificacionId;
    private String clasificacionCodigo;
    private Long palletId;
    private String palletCodigo;
    private String codigoControl;
    private LocalDate fechaControl;
    private String tipoControl;
    private String parametroEvaluado;
    private String valorMedido;
    private String valorEsperado;
    private Boolean cumpleEspecificacion;
    private String resultado;
    private String responsableControl;
    private String laboratorio;
    private String numeroCertificado;
    private String accionCorrectiva;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}