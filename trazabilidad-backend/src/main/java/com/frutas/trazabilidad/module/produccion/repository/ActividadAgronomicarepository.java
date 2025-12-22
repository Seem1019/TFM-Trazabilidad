package com.frutas.trazabilidad.module.produccion.repository;

import com.frutas.trazabilidad.module.produccion.entity.ActividadAgronomica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad ActividadAgronomica.
 */
@Repository
public interface ActividadAgronomicarepository extends JpaRepository<ActividadAgronomica, Long> {

    /**
     * Busca todas las actividades de un lote ordenadas por fecha descendente.
     */
    List<ActividadAgronomica> findByLoteIdAndActivoTrueOrderByFechaActividadDesc(Long loteId);

    /**
     * Busca actividades por tipo en un lote.
     */
    List<ActividadAgronomica> findByLoteIdAndTipoActividadAndActivoTrue(Long loteId, String tipoActividad);

    /**
     * Busca actividades en un rango de fechas para un lote.
     */
    @Query("SELECT a FROM ActividadAgronomica a WHERE a.lote.id = :loteId " +
            "AND a.activo = true " +
            "AND a.fechaActividad BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY a.fechaActividad DESC")
    List<ActividadAgronomica> findByLoteIdAndFechaBetween(@Param("loteId") Long loteId,
                                                          @Param("fechaInicio") LocalDate fechaInicio,
                                                          @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca actividades recientes de un lote (últimos N días).
     */
    @Query("SELECT a FROM ActividadAgronomica a WHERE a.lote.id = :loteId " +
            "AND a.activo = true " +
            "AND a.fechaActividad >= :fechaDesde " +
            "ORDER BY a.fechaActividad DESC")
    List<ActividadAgronomica> findActividadesRecientes(@Param("loteId") Long loteId,
                                                       @Param("fechaDesde") LocalDate fechaDesde);

    /**
     * Cuenta actividades de un tipo específico en un lote.
     */
    long countByLoteIdAndTipoActividadAndActivoTrue(Long loteId, String tipoActividad);

    /**
     * Busca todas las actividades de los lotes de una empresa.
     */
    @Query("SELECT a FROM ActividadAgronomica a WHERE a.lote.finca.empresa.id = :empresaId " +
            "AND a.activo = true ORDER BY a.fechaActividad DESC")
    List<ActividadAgronomica> findAllByEmpresaId(@Param("empresaId") Long empresaId);
}