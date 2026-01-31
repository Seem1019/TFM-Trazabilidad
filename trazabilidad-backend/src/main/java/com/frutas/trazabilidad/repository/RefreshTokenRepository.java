package com.frutas.trazabilidad.repository;

import com.frutas.trazabilidad.entity.RefreshToken;
import com.frutas.trazabilidad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un refresh token por su valor.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca todos los tokens activos de un usuario.
     */
    List<RefreshToken> findByUserAndRevokedFalse(User user);

    /**
     * Busca tokens de un usuario por ID.
     */
    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    /**
     * Revoca todos los tokens de un usuario (para logout global).
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    /**
     * Elimina tokens expirados (para limpieza periódica).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Cuenta los tokens activos de un usuario (para limitar sesiones).
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiryDate > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    /**
     * Verifica si existe un token válido.
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt " +
            "WHERE rt.token = :token AND rt.revoked = false AND rt.expiryDate > :now")
    boolean existsByTokenAndValid(@Param("token") String token, @Param("now") Instant now);
}
