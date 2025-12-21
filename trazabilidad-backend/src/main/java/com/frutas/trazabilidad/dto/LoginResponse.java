package com.frutas.trazabilidad.dto;

import com.frutas.trazabilidad.entity.TipoRol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de login exitoso.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String nombre;
        private String apellido;
        private TipoRol rol;
        private Long empresaId;
        private String empresaNombre;
    }
}