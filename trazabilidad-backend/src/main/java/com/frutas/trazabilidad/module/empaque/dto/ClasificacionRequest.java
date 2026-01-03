package com.frutas.trazabilidad.module.empaque.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear/actualizar clasificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionRequest {

    @NotNull(message = "El ID de la recepción es obligatorio")
    private Long recepcionId;

    @NotBlank(message = "El código de clasificación es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigoClasificacion;

    @NotNull(message = "La fecha de clasificación es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fechaClasificacion;

    @NotBlank(message = "La calidad es obligatoria")
    @Size(max = 50, message = "La calidad no puede exceder 50 caracteres")
    private String calidad;

    @NotNull(message = "La cantidad clasificada es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private Double cantidadClasificada;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    private String unidadMedida;

    @Size(max = 30, message = "El calibre no puede exceder 30 caracteres")
    private String calibre;

    @DecimalMin(value = "0.0", message = "El porcentaje de merma debe ser mayor o igual a 0")
    @DecimalMax(value = "100.0", message = "El porcentaje de merma debe ser menor o igual a 100")
    private Double porcentajeMerma;

    @PositiveOrZero(message = "La cantidad de merma debe ser cero o positiva")
    private Double cantidadMerma;

    private String motivoMerma;

    @Size(max = 150, message = "El responsable no puede exceder 150 caracteres")
    private String responsableClasificacion;

    private String observaciones;
}