package com.frutas.trazabilidad.module.produccion.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear/actualizar un lote.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteRequest {

    @NotNull(message = "El ID de la finca es obligatorio")
    private Long fincaId;

    @NotBlank(message = "El código del lote es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigoLote;

    @NotBlank(message = "El nombre del lote es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de fruta es obligatorio")
    @Size(max = 100, message = "El tipo de fruta no puede exceder 100 caracteres")
    private String tipoFruta;

    @Size(max = 100, message = "La variedad no puede exceder 100 caracteres")
    private String variedad;

    @Positive(message = "El área debe ser mayor a cero")
    private Double areaHectareas;

    private LocalDate fechaSiembra;

    private LocalDate fechaPrimeraCosechaEstimada;

    @Positive(message = "La densidad de siembra debe ser mayor a cero")
    private Integer densidadSiembra;

    @Size(max = 200, message = "La ubicación interna no puede exceder 200 caracteres")
    private String ubicacionInterna;

    private String observaciones;
}