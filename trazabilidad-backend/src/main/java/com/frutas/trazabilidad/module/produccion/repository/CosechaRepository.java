package com.frutas.trazabilidad.module.produccion.repository;

import com.frutas.trazabilidad.module.produccion.entity.Cosecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Cosecha.
 */
@Repository
public interface CosechaRepository extends JpaRepository<Cosecha, Long> {

    /**
     * Busca todas las cosechas de un lote ordenadas por fecha descendente.
     */
    List<Cosecha> findByLoteIdAndActivoTrueOrderByFechaCosechaDesc(Long loteId);

    /**
     * Busca cosechas en un rango de fechas para un lote.
     */
    @Query("SELECT c FROM Cosecha c WHERE c.lote.id = :loteId " +
            "AND c.activo = true " +
            "AND c.fechaCosecha BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY c.fechaCosecha DESC")
    List<Cosecha> findByLoteIdAndFechaBetween(@Param("loteId") Long loteId,
                                              @Param("fechaInicio") LocalDate fechaInicio,
                                              @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total cosechado de un lote.
     */
    @Query("SELECT COALESCE(SUM(c.cantidadCosechada), 0) FROM Cosecha c " +
            "WHERE c.lote.id = :loteId AND c.activo = true")
    Double sumCantidadCosechadaByLoteId(@Param("loteId") Long loteId);

    /**
     * Busca cosechas de una finca.
     */
    @Query("SELECT c FROM Cosecha c WHERE c.lote.finca.id = :fincaId " +
            "AND c.activo = true ORDER BY c.fechaCosecha DESC")
    List<Cosecha> findByFincaId(@Param("fincaId") Long fincaId);

    /**
     * Busca cosechas de una empresa.
     */
    @Query("SELECT c FROM Cosecha c WHERE c.lote.finca.empresa.id = :empresaId " +
            "AND c.activo = true ORDER BY c.fechaCosecha DESC")
    List<Cosecha> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Busca cosechas recientes (últimos N días) de una empresa.
     */
    @Query("SELECT c FROM Cosecha c WHERE c.lote.finca.empresa.id = :empresaId " +
            "AND c.activo = true " +
            "AND c.fechaCosecha >= :fechaDesde " +
            "ORDER BY c.fechaCosecha DESC")
    List<Cosecha> findCosechasRecientes(@Param("empresaId") Long empresaId,
                                        @Param("fechaDesde") LocalDate fechaDesde);

    /**
     * Cuenta cosechas de un lote.
     */
    long countByLoteIdAndActivoTrue(Long loteId);

    /**
     * Busca cosechas por calidad inicial.
     */
    @Query("SELECT c FROM Cosecha c WHERE c.lote.finca.empresa.id = :empresaId " +
            "AND c.calidadInicial = :calidadInicial AND c.activo = true")
    List<Cosecha> findByEmpresaIdAndCalidadInicial(@Param("empresaId") Long empresaId,
                                                   @Param("calidadInicial") String calidadInicial);

    /**
     * Calcula total cosechado por empresa en un rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(c.cantidadCosechada), 0) FROM Cosecha c " +
            "WHERE c.lote.finca.empresa.id = :empresaId " +
            "AND c.activo = true " +
            "AND c.fechaCosecha BETWEEN :fechaInicio AND :fechaFin")
    Double sumCantidadByEmpresaIdAndFechaBetween(@Param("empresaId") Long empresaId,
                                                 @Param("fechaInicio") LocalDate fechaInicio,
                                                 @Param("fechaFin") LocalDate fechaFin);
}