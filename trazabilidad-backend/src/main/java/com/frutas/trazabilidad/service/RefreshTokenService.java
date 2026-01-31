package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.entity.RefreshToken;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.exception.UnauthorizedException;
import com.frutas.trazabilidad.repository.RefreshTokenRepository;
import com.frutas.trazabilidad.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para gestión de refresh tokens.
 * Permite renovar access tokens sin re-autenticación completa.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    private static final int MAX_ACTIVE_SESSIONS = 5; // Máximo de sesiones simultáneas por usuario

    /**
     * Crea un nuevo refresh token para el usuario.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        // Limitar sesiones activas
        long activeTokens = refreshTokenRepository.countActiveTokensByUserId(user.getId(), Instant.now());
        if (activeTokens >= MAX_ACTIVE_SESSIONS) {
            // Revocar el token más antiguo
            refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId()).stream()
                    .findFirst()
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                        log.info("Revocado token antiguo para usuario {} por límite de sesiones", user.getEmail());
                    });
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpirationMs()))
                .revoked(false)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token creado para usuario: {}", user.getEmail());

        return refreshToken;
    }

    /**
     * Valida un refresh token y retorna el objeto si es válido.
     */
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token no encontrado: {}", token.substring(0, 8) + "...");
                    return new UnauthorizedException("Refresh token inválido");
                });

        if (refreshToken.getRevoked()) {
            log.warn("Intento de uso de refresh token revocado para usuario: {}",
                    refreshToken.getUser().getEmail());
            throw new UnauthorizedException("Refresh token ha sido revocado");
        }

        if (refreshToken.isExpired()) {
            log.warn("Refresh token expirado para usuario: {}", refreshToken.getUser().getEmail());
            throw new UnauthorizedException("Refresh token ha expirado");
        }

        return refreshToken;
    }

    /**
     * Renueva un refresh token (rotación).
     * El token antiguo se revoca y se genera uno nuevo.
     */
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, String deviceInfo, String ipAddress) {
        // Revocar el token antiguo
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Crear nuevo token
        return createRefreshToken(oldToken.getUser(), deviceInfo, ipAddress);
    }

    /**
     * Revoca un refresh token específico (logout de una sesión).
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.info("Refresh token revocado para usuario: {}", refreshToken.getUser().getEmail());
        });
    }

    /**
     * Revoca todos los refresh tokens de un usuario (logout global).
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Todos los refresh tokens revocados para usuario ID: {}", userId);
    }

    /**
     * Limpia tokens expirados de la base de datos.
     * Se ejecuta cada hora.
     */
    @Scheduled(fixedRate = 3600000) // 1 hora
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.debug("Limpieza de refresh tokens expirados completada");
    }
}
