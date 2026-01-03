package com.frutas.trazabilidad.module.logistica.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear/actualizar envíos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvioRequest {

    @NotBlank(message = "El código de envío es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigoEnvio;

    @NotNull(message = "La fecha de creación es obligatoria")
    private LocalDate fechaCreacion;

    private LocalDate fechaSalidaEstimada;

    @Size(max = 200, message = "El exportador no puede exceder 200 caracteres")
    private String exportador;

    @NotBlank(message = "El país de destino es obligatorio")
    @Size(max = 100, message = "El país destino no puede exceder 100 caracteres")
    private String paisDestino;

    @Size(max = 100, message = "El puerto destino no puede exceder 100 caracteres")
    private String puertoDestino;

    @Size(max = 100, message = "La ciudad destino no puede exceder 100 caracteres")
    private String ciudadDestino;

    @NotBlank(message = "El tipo de transporte es obligatorio")
    @Pattern(regexp = "MARITIMO|AEREO|TERRESTRE", message = "Tipo de transporte inválido")
    private String tipoTransporte;

    @Size(max = 50, message = "El código de contenedor no puede exceder 50 caracteres")
    private String codigoContenedor;

    @Size(max = 50, message = "El tipo de contenedor no puede exceder 50 caracteres")
    private String tipoContenedor;

    @DecimalMin(value = "-30.0", message = "La temperatura debe ser mayor a -30°C")
    @DecimalMax(value = "30.0", message = "La temperatura debe ser menor a 30°C")
    private Double temperaturaContenedor;

    @Size(max = 200, message = "El transportista no puede exceder 200 caracteres")
    private String transportista;

    @Size(max = 100, message = "El número de booking no puede exceder 100 caracteres")
    private String numeroBooking;

    @Size(max = 100, message = "El número de B/L no puede exceder 100 caracteres")
    private String numeroBL;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    @Size(max = 200, message = "El cliente importador no puede exceder 200 caracteres")
    private String clienteImportador;

    @Size(max = 10, message = "El incoterm no puede exceder 10 caracteres")
    @Pattern(regexp = "FOB|CIF|EXW|FCA|CPT|CIP|DAP|DPU|DDP",
            message = "Incoterm inválido",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String incoterm;

    /**
     * IDs de los pallets a asignar al envío.
     */
    private List<Long> palletsIds;
}