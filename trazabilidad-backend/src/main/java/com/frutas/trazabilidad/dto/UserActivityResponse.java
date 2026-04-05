package com.frutas.trazabilidad.dto;

import com.frutas.trazabilidad.entity.TipoRol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa la actividad de un usuario para monitorización.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private TipoRol rol;
    private Boolean activo;

    // Actividad
    private LocalDateTime ultimoAcceso;
    private Integer intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private Boolean bloqueado;

    // Sesiones
    private Long sesionesActivas;
}
