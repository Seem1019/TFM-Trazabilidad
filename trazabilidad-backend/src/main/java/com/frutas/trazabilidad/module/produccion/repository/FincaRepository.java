package com.frutas.trazabilidad.module.produccion.repository;

import com.frutas.trazabilidad.module.produccion.entity.Finca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Finca.
 * Incluye queries con aislamiento multiempresa.
 */
@Repository
public interface FincaRepository extends JpaRepository<Finca, Long> {

    /**
     * Busca todas las fincas activas de una empresa.
     */
    List<Finca> findByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Busca todas las fincas de una empresa (activas e inactivas).
     */
    List<Finca> findByEmpresaId(Long empresaId);

    /**
     * Busca una finca por ID verificando que pertenezca a la empresa.
     */
    @Query("SELECT f FROM Finca f WHERE f.id = :id AND f.empresa.id = :empresaId")
    Optional<Finca> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Verifica si existe una finca con el código dado en la empresa.
     */
    boolean existsByCodigoFincaAndEmpresaId(String codigoFinca, Long empresaId);

    /**
     * Busca fincas por nombre parcial (LIKE) en una empresa.
     */
    @Query("SELECT f FROM Finca f WHERE f.empresa.id = :empresaId " +
            "AND f.activo = true AND LOWER(f.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Finca> findByEmpresaIdAndNombreContaining(@Param("empresaId") Long empresaId,
                                                   @Param("nombre") String nombre);

    /**
     * Busca fincas por municipio en una empresa.
     */
    List<Finca> findByEmpresaIdAndMunicipioAndActivoTrue(Long empresaId, String municipio);

    /**
     * Cuenta fincas activas de una empresa.
     */
    long countByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Busca fincas con área mayor a cierta cantidad.
     */
    @Query("SELECT f FROM Finca f WHERE f.empresa.id = :empresaId " +
            "AND f.activo = true AND f.areaHectareas >= :areaMinima")
    List<Finca> findByEmpresaIdAndAreaMayorA(@Param("empresaId") Long empresaId,
                                             @Param("areaMinima") Double areaMinima);
}