package com.frutas.trazabilidad.repository;

import com.frutas.trazabilidad.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    /**
     * Busca una empresa por su NIT.
     */
    Optional<Empresa> findByNit(String nit);

    /**
     * Verifica si existe una empresa con el NIT dado.
     */
    boolean existsByNit(String nit);
}