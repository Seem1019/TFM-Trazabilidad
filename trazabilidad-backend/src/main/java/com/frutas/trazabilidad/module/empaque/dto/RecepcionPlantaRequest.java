package com.frutas.trazabilidad.module.empaque.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear/actualizar recepción en planta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionPlantaRequest {

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId;

    @NotBlank(message = "El código de recepción es obligatorio")
    @Size(max = 50, message = "El código de recepción no puede exceder 50 caracteres")
    private String codigoRecepcion;

    @NotNull(message = "La fecha de recepción es obligatoria")
    @PastOrPresent(message = "La fecha de recepción no puede ser futura")
    private LocalDate fechaRecepcion;

    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$", message = "Formato de hora inválido (HH:mm)")
    private String horaRecepcion;

    @NotNull(message = "La cantidad recibida es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private Double cantidadRecibida;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    private String unidadMedida;

    @DecimalMin(value = "0.0", message = "La temperatura debe ser mayor o igual a 0")
    @DecimalMax(value = "50.0", message = "La temperatura debe ser menor o igual a 50")
    private Double temperaturaFruta;

    @Size(max = 50, message = "El estado inicial no puede exceder 50 caracteres")
    private String estadoInicial;

    @Size(max = 150, message = "El responsable no puede exceder 150 caracteres")
    private String responsableRecepcion;

    @Size(max = 50, message = "El vehículo no puede exceder 50 caracteres")
    private String vehiculoTransporte;

    @Size(max = 100, message = "El conductor no puede exceder 100 caracteres")
    private String conductor;

    private String observaciones;
}