package com.frutas.trazabilidad.module.logistica.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para crear/actualizar eventos logísticos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoLogisticoRequest {

    @NotNull(message = "El ID del envío es obligatorio")
    private Long envioId;

    @Size(max = 50, message = "El código del evento no puede exceder 50 caracteres")
    private String codigoEvento;

    @NotBlank(message = "El tipo de evento es obligatorio")
    @Pattern(regexp = "CARGA|SALIDA_PLANTA|ARRIBO_PUERTO|CONSOLIDACION|DESPACHO|ARRIBO_DESTINO",
            message = "Tipo de evento inválido")
    private String tipoEvento;

    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDate fechaEvento;

    @NotNull(message = "La hora del evento es obligatoria")
    private LocalTime horaEvento;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 200, message = "La ubicación no puede exceder 200 caracteres")
    private String ubicacion;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String pais;

    @DecimalMin(value = "-90.0", message = "Latitud inválida")
    @DecimalMax(value = "90.0", message = "Latitud inválida")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "Longitud inválida")
    @DecimalMax(value = "180.0", message = "Longitud inválida")
    private Double longitud;

    @NotBlank(message = "El responsable es obligatorio")
    @Size(max = 200, message = "El responsable no puede exceder 200 caracteres")
    private String responsable;

    @Size(max = 200, message = "La organización no puede exceder 200 caracteres")
    private String organizacion;

    @DecimalMin(value = "-30.0", message = "Temperatura fuera de rango")
    @DecimalMax(value = "30.0", message = "Temperatura fuera de rango")
    private Double temperaturaRegistrada;

    @DecimalMin(value = "0.0", message = "Humedad no puede ser negativa")
    @DecimalMax(value = "100.0", message = "Humedad no puede exceder 100%")
    private Double humedadRegistrada;

    @Size(max = 20, message = "La placa del vehículo no puede exceder 20 caracteres")
    private String vehiculo;

    @Size(max = 200, message = "El conductor no puede exceder 200 caracteres")
    private String conductor;

    @Size(max = 100, message = "El número de precinto no puede exceder 100 caracteres")
    private String numeroPrecinto;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    @Size(max = 500, message = "La URL de evidencia no puede exceder 500 caracteres")
    private String urlEvidencia;

    private Boolean incidencia = false;

    @Size(max = 1000, message = "El detalle de incidencia no puede exceder 1000 caracteres")
    private String detalleIncidencia;
}