package com.frutas.trazabilidad.module.produccion.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para registrar actividad agronómica.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActividadAgronomicarRequest {

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId;

    @NotBlank(message = "El tipo de actividad es obligatorio")
    @Size(max = 100, message = "El tipo de actividad no puede exceder 100 caracteres")
    private String tipoActividad;

    @NotNull(message = "La fecha de actividad es obligatoria")
    @PastOrPresent(message = "La fecha de actividad no puede ser futura")
    private LocalDate fechaActividad;

    @Size(max = 200, message = "El producto aplicado no puede exceder 200 caracteres")
    private String productoAplicado;

    @Size(max = 100, message = "La dosis o cantidad no puede exceder 100 caracteres")
    private String dosisoCantidad;

    @Size(max = 50, message = "La unidad de medida no puede exceder 50 caracteres")
    private String unidadMedida;

    @Size(max = 100, message = "El método de aplicación no puede exceder 100 caracteres")
    private String metodoAplicacion;

    @Size(max = 150, message = "El nombre del responsable no puede exceder 150 caracteres")
    private String responsable;

    @Size(max = 100, message = "El número de registro no puede exceder 100 caracteres")
    private String numeroRegistroProducto;

    @PositiveOrZero(message = "El intervalo de seguridad debe ser cero o positivo")
    private Integer intervaloSeguridadDias;

    private String observaciones;
}