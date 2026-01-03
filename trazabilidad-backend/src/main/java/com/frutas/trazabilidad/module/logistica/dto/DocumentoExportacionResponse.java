package com.frutas.trazabilidad.module.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de documentos de exportación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoExportacionResponse {

    private Long id;

    // Envío asociado
    private Long envioId;
    private String codigoEnvio;

    // Identificación del documento
    private String tipoDocumento;
    private String numeroDocumento;

    // Fechas
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private Boolean estaVencido;

    // Emisión
    private String entidadEmisora;
    private String funcionarioEmisor;

    // Archivo
    private String urlArchivo;
    private String tipoArchivo;
    private Long tamanoArchivo;
    private String hashArchivo;
    private Boolean tieneArchivo;

    // Estado
    private String estado;
    private Boolean estaAprobado;

    // Descripción
    private String descripcion;

    // Valor
    private Double valorDeclarado;
    private String moneda;

    // Configuración
    private Boolean obligatorio;

    // Metadata
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}