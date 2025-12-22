package com.frutas.trazabilidad.module.produccion.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar una finca.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FincaRequest {

    @NotBlank(message = "El código de finca es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigoFinca;

    @NotBlank(message = "El nombre de la finca es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La ubicación no puede exceder 500 caracteres")
    private String ubicacion;

    @Size(max = 200, message = "El municipio no puede exceder 200 caracteres")
    private String municipio;

    @Size(max = 100, message = "El departamento no puede exceder 100 caracteres")
    private String departamento;

    @Size(max = 50, message = "El país no puede exceder 50 caracteres")
    private String pais;

    @Positive(message = "El área debe ser mayor a cero")
    private Double areaHectareas;

    @Size(max = 100, message = "El nombre del propietario no puede exceder 100 caracteres")
    private String propietario;

    @Size(max = 100, message = "El nombre del encargado no puede exceder 100 caracteres")
    private String encargado;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Formato de teléfono inválido")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Email(message = "El formato del email es inválido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitud;

    private String observaciones;
}