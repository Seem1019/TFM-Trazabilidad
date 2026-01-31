package com.frutas.trazabilidad.dto;

import com.frutas.trazabilidad.entity.TipoRol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de login exitoso.
 * Incluye access token, refresh token e información del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // Segundos hasta expiración del access token
    private UserInfo user;

    /**
     * @deprecated Use accessToken instead
     * Mantiene compatibilidad con frontend existente.
     */
    @Deprecated
    public String getToken() {
        return accessToken;
    }

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
