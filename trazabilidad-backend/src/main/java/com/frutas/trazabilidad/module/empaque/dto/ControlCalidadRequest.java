package com.frutas.trazabilidad.module.empaque.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear/actualizar control de calidad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlCalidadRequest {

    private Long clasificacionId; // Opcional si se controla un pallet

    private Long palletId; // Opcional si se controla una clasificación

    @NotBlank(message = "El código de control es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigoControl;

    @NotNull(message = "La fecha de control es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fechaControl;

    @NotBlank(message = "El tipo de control es obligatorio")
    @Size(max = 100, message = "El tipo no puede exceder 100 caracteres")
    private String tipoControl;

    @Size(max = 200, message = "El parámetro no puede exceder 200 caracteres")
    private String parametroEvaluado;

    @Size(max = 100, message = "El valor medido no puede exceder 100 caracteres")
    private String valorMedido;

    @Size(max = 100, message = "El valor esperado no puede exceder 100 caracteres")
    private String valorEsperado;

    private Boolean cumpleEspecificacion;

    @NotBlank(message = "El resultado es obligatorio")
    @Size(max = 30, message = "El resultado no puede exceder 30 caracteres")
    private String resultado; // APROBADO, RECHAZADO, CONDICIONAL

    @NotBlank(message = "El responsable es obligatorio")
    @Size(max = 150, message = "El responsable no puede exceder 150 caracteres")
    private String responsableControl;

    @Size(max = 200, message = "El laboratorio no puede exceder 200 caracteres")
    private String laboratorio;

    @Size(max = 100, message = "El número de certificado no puede exceder 100 caracteres")
    private String numeroCertificado;

    private String accionCorrectiva;

    private String observaciones;
}