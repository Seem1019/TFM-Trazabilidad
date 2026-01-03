package com.frutas.trazabilidad.module.logistica.repository;

import com.frutas.trazabilidad.module.logistica.entity.AuditoriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de eventos de auditoría.
 */
@Repository
public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Long> {

    /**
     * Lista eventos de auditoría por empresa, ordenados por fecha descendente.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId ORDER BY a.fechaEvento DESC")
    List<AuditoriaEvento> findByEmpresaIdOrderByFechaDesc(@Param("empresaId") Long empresaId);

    /**
     * Lista eventos de auditoría por tipo de entidad y ID de entidad.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.tipoEntidad = :tipoEntidad AND a.entidadId = :entidadId " +
            "ORDER BY a.fechaEvento DESC")
    List<AuditoriaEvento> findByTipoEntidadAndEntidadId(
            @Param("tipoEntidad") String tipoEntidad,
            @Param("entidadId") Long entidadId
    );

    /**
     * Lista eventos de auditoría por usuario.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.usuario.id = :usuarioId ORDER BY a.fechaEvento DESC")
    List<AuditoriaEvento> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Lista eventos de auditoría por tipo de operación y empresa.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId AND a.tipoOperacion = :tipoOperacion " +
            "ORDER BY a.fechaEvento DESC")
    List<AuditoriaEvento> findByEmpresaIdAndTipoOperacion(
            @Param("empresaId") Long empresaId,
            @Param("tipoOperacion") String tipoOperacion
    );

    /**
     * Lista eventos de auditoría por módulo y empresa.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId AND a.modulo = :modulo " +
            "ORDER BY a.fechaEvento DESC")
    List<AuditoriaEvento> findByEmpresaIdAndModulo(
            @Param("empresaId") Long empresaId,
            @Param("modulo") String modulo
    );

    /**
     * Lista eventos de auditoría por nivel de criticidad.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId AND a.nivelCriticidad = :nivel " +
            "ORDER BY a.fechaEvento DESC")
    List<AuditoriaEvento> findByEmpresaIdAndNivelCriticidad(
            @Param("empresaId") Long empresaId,
            @Param("nivel") String nivel
    );

    /**
     * Lista eventos de auditoría en un rango de fechas.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId " +
            "AND a.fechaEvento BETWEEN :desde AND :hasta " +
            "ORDER BY a.fechaEvento DESC")
    List<AuditoriaEvento> findByEmpresaIdAndFechaEventoBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    /**
     * Lista eventos que forman parte de la cadena blockchain.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId AND a.enCadena = true " +
            "ORDER BY a.fechaEvento ASC")
    List<AuditoriaEvento> findCadenaBlockchainByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Obtiene el último evento de la cadena blockchain.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId AND a.enCadena = true " +
            "ORDER BY a.fechaEvento DESC LIMIT 1")
    Optional<AuditoriaEvento> findUltimoEventoCadena(@Param("empresaId") Long empresaId);

    /**
     * Busca eventos por código de entidad.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId AND a.codigoEntidad = :codigoEntidad " +
            "ORDER BY a.fechaEvento DESC")
    List<AuditoriaEvento> findByEmpresaIdAndCodigoEntidad(
            @Param("empresaId") Long empresaId,
            @Param("codigoEntidad") String codigoEntidad
    );

    /**
     * Lista eventos críticos recientes.
     */
    @Query("SELECT a FROM AuditoriaEvento a WHERE a.empresaId = :empresaId AND a.nivelCriticidad = 'CRITICAL' " +
            "ORDER BY a.fechaEvento DESC LIMIT :limite")
    List<AuditoriaEvento> findEventosCriticosRecientes(
            @Param("empresaId") Long empresaId,
            @Param("limite") int limite
    );

    /**
     * Cuenta eventos por tipo de entidad.
     */
    @Query("SELECT COUNT(a) FROM AuditoriaEvento a WHERE a.empresaId = :empresaId AND a.tipoEntidad = :tipoEntidad")
    long countByEmpresaIdAndTipoEntidad(@Param("empresaId") Long empresaId, @Param("tipoEntidad") String tipoEntidad);
}