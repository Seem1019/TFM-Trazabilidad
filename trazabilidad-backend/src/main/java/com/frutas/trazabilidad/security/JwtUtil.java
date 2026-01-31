package com.frutas.trazabilidad.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.frutas.trazabilidad.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Utilidad para generación y validación de tokens JWT.
 * Soporta access tokens (corta duración) y refresh tokens (larga duración).
 */
@Component
public class JwtUtil {

    @Value("${spring.security.jwt.secret}")
    private String secret;

    @Value("${spring.security.jwt.access-token-expiration:900000}") // 15 minutos por defecto
    private Long accessTokenExpiration;

    @Value("${spring.security.jwt.refresh-token-expiration:604800000}") // 7 días por defecto
    private Long refreshTokenExpiration;

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";

    /**
     * Genera un access token JWT para un usuario.
     * Duración corta (15 minutos por defecto).
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("userId", user.getId())
                .withClaim("empresaId", user.getEmpresa().getId())
                .withClaim("rol", user.getRol().name())
                .withClaim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * @deprecated Use generateAccessToken instead
     * Mantiene compatibilidad con código existente.
     */
    @Deprecated
    public String generateToken(User user) {
        return generateAccessToken(user);
    }

    /**
     * Valida un token JWT y retorna el email del usuario.
     * Verifica firma, expiración y tipo de token.
     */
    public String validateTokenAndGetEmail(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);

            // Verificar que es un access token (no refresh token)
            String tokenType = jwt.getClaim(TOKEN_TYPE_CLAIM).asString();
            if (tokenType != null && !ACCESS_TOKEN_TYPE.equals(tokenType)) {
                throw new RuntimeException("Tipo de token inválido");
            }

            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token JWT inválido o expirado", e);
        }
    }

    /**
     * Extrae el email del usuario sin validar el token.
     * Útil para obtener información antes de validar.
     */
    public String getEmailFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si un token ha expirado.
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extrae el empresaId del token.
     */
    public Long extractEmpresaId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("empresaId").asLong();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo extraer empresaId del token", e);
        }
    }

    /**
     * Extrae el userId del token.
     */
    public Long extractUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo extraer userId del token", e);
        }
    }

    /**
     * Extrae el rol del token.
     */
    public String extractRol(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("rol").asString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo extraer rol del token", e);
        }
    }

    /**
     * Obtiene el tiempo de expiración del access token en segundos.
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Obtiene el tiempo de expiración del refresh token en milisegundos.
     */
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpiration;
    }

    /**
     * Valida la firma del token sin verificar expiración.
     * Útil para refresh tokens donde queremos verificar autenticidad.
     */
    public boolean isTokenSignatureValid(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .acceptExpiresAt(Long.MAX_VALUE / 1000) // Ignora expiración
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
