package com.frutas.trazabilidad.module.empaque.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear/actualizar pallet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalletRequest {

    @NotBlank(message = "El código de pallet es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigoPallet;

    @NotNull(message = "La fecha de paletizado es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fechaPaletizado;

    @Size(max = 50, message = "El tipo de pallet no puede exceder 50 caracteres")
    private String tipoPallet;

    @NotNull(message = "El número de cajas es obligatorio")
    @Positive(message = "El número de cajas debe ser positivo")
    private Integer numeroCajas;

    @Positive(message = "El peso neto debe ser positivo")
    private Double pesoNetoTotal;

    @Positive(message = "El peso bruto debe ser positivo")
    private Double pesoBrutoTotal;

    @Positive(message = "La altura debe ser positiva")
    private Double alturaPallet;

    @Size(max = 100, message = "El tipo de fruta no puede exceder 100 caracteres")
    private String tipoFruta;

    @Size(max = 50, message = "La calidad no puede exceder 50 caracteres")
    private String calidad;

    @Size(max = 200, message = "El destino no puede exceder 200 caracteres")
    private String destino;

    @DecimalMin(value = "-20.0", message = "Temperatura mínima: -20°C")
    @DecimalMax(value = "30.0", message = "Temperatura máxima: 30°C")
    private Double temperaturaAlmacenamiento;

    @Size(max = 150, message = "El responsable no puede exceder 150 caracteres")
    private String responsablePaletizado;

    private String observaciones;

    // IDs de etiquetas a asociar
    private List<Long> etiquetasIds;
}