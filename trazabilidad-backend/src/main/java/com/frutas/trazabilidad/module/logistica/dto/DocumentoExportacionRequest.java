package com.frutas.trazabilidad.module.logistica.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear/actualizar documentos de exportación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoExportacionRequest {

    @NotNull(message = "El ID del envío es obligatorio")
    private Long envioId;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "PACKING_LIST|CERTIFICADO_FITOSANITARIO|FACTURA_COMERCIAL|BL|CERTIFICADO_ORIGEN|LISTA_EMPAQUE|OTRO",
            message = "Tipo de documento inválido")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 100, message = "El número de documento no puede exceder 100 caracteres")
    private String numeroDocumento;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    private LocalDate fechaVencimiento;

    @Size(max = 200, message = "La entidad emisora no puede exceder 200 caracteres")
    private String entidadEmisora;

    @Size(max = 200, message = "El funcionario emisor no puede exceder 200 caracteres")
    private String funcionarioEmisor;

    @Size(max = 500, message = "La URL del archivo no puede exceder 500 caracteres")
    private String urlArchivo;

    @Size(max = 50, message = "El tipo de archivo no puede exceder 50 caracteres")
    private String tipoArchivo;

    private Long tamanoArchivo;

    @Size(max = 64, message = "El hash del archivo debe ser SHA-256 (64 caracteres)")
    private String hashArchivo;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @DecimalMin(value = "0.0", message = "El valor declarado no puede ser negativo")
    private Double valorDeclarado;

    @Size(max = 10, message = "La moneda no puede exceder 10 caracteres")
    @Pattern(regexp = "USD|EUR|COP|GBP|JPY",
            message = "Moneda inválida",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String moneda;

    private Boolean obligatorio = false;
}