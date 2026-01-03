package com.frutas.trazabilidad.module.empaque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Etiqueta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaResponse {

    private Long id;
    private Long clasificacionId;
    private String clasificacionCodigo;
    private String calidad;
    private String codigoEtiqueta;
    private String codigoQr;
    private String tipoEtiqueta;
    private Double cantidadContenida;
    private String unidadMedida;
    private Double pesoNeto;
    private Double pesoBruto;
    private Integer numeroCajas;
    private String estadoEtiqueta;
    private String urlQr;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información de trazabilidad
    private String loteOrigen;
    private String fincaOrigen;
}