package com.frutas.trazabilidad.dto;

import com.frutas.trazabilidad.entity.TipoRol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para usuarios.
 * No incluye el passwordHash por seguridad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private TipoRol rol;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime ultimoAcceso;

    // Información de la empresa
    private Long empresaId;
    private String empresaNombre;
}
