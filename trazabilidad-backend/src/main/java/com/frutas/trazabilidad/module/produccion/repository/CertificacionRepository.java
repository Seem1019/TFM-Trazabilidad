package com.frutas.trazabilidad.module.produccion.repository;

import com.frutas.trazabilidad.module.produccion.entity.Certificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Certificacion.
 */
@Repository
public interface CertificacionRepository extends JpaRepository<Certificacion, Long> {

    /**
     * Busca todas las certificaciones activas de una finca.
     */
    List<Certificacion> findByFincaIdAndActivoTrue(Long fincaId);

    /**
     * Busca certificaciones vigentes de una finca.
     */
    @Query("SELECT c FROM Certificacion c WHERE c.finca.id = :fincaId " +
            "AND c.activo = true AND c.estado = 'VIGENTE'")
    List<Certificacion> findCertificacionesVigentes(@Param("fincaId") Long fincaId);

    /**
     * Busca certificaciones por tipo en una finca.
     */
    List<Certificacion> findByFincaIdAndTipoCertificacionAndActivoTrue(Long fincaId, String tipoCertificacion);

    /**
     * Busca certificaciones próximas a vencer (en los próximos N días).
     */
    @Query("SELECT c FROM Certificacion c WHERE c.finca.empresa.id = :empresaId " +
            "AND c.activo = true AND c.estado = 'VIGENTE' " +
            "AND c.fechaVencimiento BETWEEN :hoy AND :fechaLimite")
    List<Certificacion> findProximasAVencer(@Param("empresaId") Long empresaId,
                                            @Param("hoy") LocalDate hoy,
                                            @Param("fechaLimite") LocalDate fechaLimite);

    /**
     * Cuenta certificaciones vigentes de una finca.
     */
    @Query("SELECT COUNT(c) FROM Certificacion c WHERE c.finca.id = :fincaId " +
            "AND c.activo = true AND c.estado = 'VIGENTE'")
    long countCertificacionesVigentes(@Param("fincaId") Long fincaId);
}