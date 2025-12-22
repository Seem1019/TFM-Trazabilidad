package com.frutas.trazabilidad.module.produccion.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear/actualizar certificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificacionRequest {

    @NotNull(message = "El ID de la finca es obligatorio")
    private Long fincaId;

    @NotBlank(message = "El tipo de certificación es obligatorio")
    @Size(max = 100, message = "El tipo de certificación no puede exceder 100 caracteres")
    private String tipoCertificacion;

    @NotBlank(message = "La entidad emisora es obligatoria")
    @Size(max = 200, message = "La entidad emisora no puede exceder 200 caracteres")
    private String entidadEmisora;

    @Size(max = 100, message = "El número de certificado no puede exceder 100 caracteres")
    private String numeroCertificado;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @PastOrPresent(message = "La fecha de emisión no puede ser futura")
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDate fechaVencimiento;

    private String urlDocumento;

    private String observaciones;
}