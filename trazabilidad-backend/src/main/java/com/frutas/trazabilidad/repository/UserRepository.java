package com.frutas.trazabilidad.repository;

import com.frutas.trazabilidad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por email.
     * Usado para autenticación.
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado.
     */
    boolean existsByEmail(String email);

    /**
     * Obtiene todos los usuarios de una empresa específica.
     * Implementa aislamiento multiempresa.
     */
    List<User> findByEmpresaId(Long empresaId);

    /**
     * Obtiene usuarios activos de una empresa.
     */
    List<User> findByEmpresaIdAndActivoTrue(Long empresaId);
}