package com.frutas.trazabilidad.module.produccion.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para registrar una cosecha.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosechaRequest {

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId;

    @NotNull(message = "La fecha de cosecha es obligatoria")
    @PastOrPresent(message = "La fecha de cosecha no puede ser futura")
    private LocalDate fechaCosecha;

    @NotNull(message = "La cantidad cosechada es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double cantidadCosechada;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 50, message = "La unidad de medida no puede exceder 50 caracteres")
    private String unidadMedida;

    @Size(max = 50, message = "La calidad inicial no puede exceder 50 caracteres")
    private String calidadInicial;

    @Size(max = 100, message = "El estado de la fruta no puede exceder 100 caracteres")
    private String estadoFruta;

    @Size(max = 150, message = "El nombre del responsable no puede exceder 150 caracteres")
    private String responsableCosecha;

    @Positive(message = "El número de trabajadores debe ser mayor a cero")
    private Integer numeroTrabajadores;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Formato de hora inválido (HH:mm)")
    private String horaInicio;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Formato de hora inválido (HH:mm)")
    private String horaFin;

    private Double temperaturaAmbiente;

    private String observaciones;
}