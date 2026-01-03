package com.frutas.trazabilidad.module.empaque.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar etiqueta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaRequest {

    @NotNull(message = "El ID de la clasificación es obligatorio")
    private Long clasificacionId;

    @NotBlank(message = "El código de etiqueta es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigoEtiqueta;

    @NotBlank(message = "El tipo de etiqueta es obligatorio")
    @Size(max = 30, message = "El tipo no puede exceder 30 caracteres")
    private String tipoEtiqueta; // CAJA, PALLET

    @Positive(message = "La cantidad debe ser positiva")
    private Double cantidadContenida;

    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    private String unidadMedida;

    @Positive(message = "El peso neto debe ser positivo")
    private Double pesoNeto;

    @Positive(message = "El peso bruto debe ser positivo")
    private Double pesoBruto;

    @PositiveOrZero(message = "El número de cajas debe ser cero o positivo")
    private Integer numeroCajas;

    private String observaciones;
}