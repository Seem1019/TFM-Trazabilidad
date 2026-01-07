package com.frutas.trazabilidad.repository;

import com.frutas.trazabilidad.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token de recuperación por su valor.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Elimina todos los tokens de un usuario.
     */
    void deleteByUserId(Long userId);
}
