package com.frutas.trazabilidad.module.logistica.repository;

import com.frutas.trazabilidad.module.logistica.entity.EventoLogistico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para gestión de eventos logísticos (tracking de envíos).
 */
@Repository
public interface EventoLogisticoRepository extends JpaRepository<EventoLogistico, Long> {

    /**
     * Lista eventos logísticos por envío, ordenados cronológicamente.
     */
    @Query("SELECT e FROM EventoLogistico e WHERE e.envio.id = :envioId AND e.activo = true " +
            "ORDER BY e.fechaEvento ASC, e.horaEvento ASC")
    List<EventoLogistico> findByEnvioIdOrderByFechaAsc(@Param("envioId") Long envioId);

    /**
     * Lista eventos logísticos por tipo de evento y envío.
     */
    @Query("SELECT e FROM EventoLogistico e WHERE e.envio.id = :envioId AND e.tipoEvento = :tipoEvento AND e.activo = true " +
            "ORDER BY e.fechaEvento DESC")
    List<EventoLogistico> findByEnvioIdAndTipoEvento(@Param("envioId") Long envioId, @Param("tipoEvento") String tipoEvento);

    /**
     * Lista eventos con incidencias por envío.
     */
    @Query("SELECT e FROM EventoLogistico e WHERE e.envio.id = :envioId AND e.incidencia = true AND e.activo = true " +
            "ORDER BY e.fechaEvento DESC")
    List<EventoLogistico> findIncidenciasByEnvioId(@Param("envioId") Long envioId);

    /**
     * Obtiene el último evento logístico de un envío.
     */
    @Query("SELECT e FROM EventoLogistico e WHERE e.envio.id = :envioId AND e.activo = true " +
            "ORDER BY e.fechaEvento DESC, e.horaEvento DESC LIMIT 1")
    EventoLogistico findUltimoEventoByEnvioId(@Param("envioId") Long envioId);

    /**
     * Lista eventos logísticos por empresa y rango de fechas.
     */
    @Query("SELECT e FROM EventoLogistico e WHERE e.envio.usuario.empresa.id = :empresaId " +
            "AND e.fechaEvento BETWEEN :desde AND :hasta " +
            "AND e.activo = true " +
            "ORDER BY e.fechaEvento DESC")
    List<EventoLogistico> findByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    /**
     * Lista eventos logísticos por ubicación.
     */
    @Query("SELECT e FROM EventoLogistico e WHERE e.envio.usuario.empresa.id = :empresaId " +
            "AND e.ubicacion LIKE %:ubicacion% " +
            "AND e.activo = true " +
            "ORDER BY e.fechaEvento DESC")
    List<EventoLogistico> findByEmpresaIdAndUbicacion(@Param("empresaId") Long empresaId, @Param("ubicacion") String ubicacion);

    /**
     * Verifica si un evento pertenece a la empresa (a través del envío).
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EventoLogistico e " +
            "WHERE e.id = :eventoId AND e.envio.usuario.empresa.id = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("eventoId") Long eventoId, @Param("empresaId") Long empresaId);

    /**
     * Cuenta eventos por tipo para un envío.
     */
    @Query("SELECT COUNT(e) FROM EventoLogistico e WHERE e.envio.id = :envioId AND e.tipoEvento = :tipoEvento AND e.activo = true")
    long countByEnvioIdAndTipoEvento(@Param("envioId") Long envioId, @Param("tipoEvento") String tipoEvento);

    /**
     * Busca evento por ID validando pertenencia a empresa.
     */
    @Query("SELECT e FROM EventoLogistico e WHERE e.id = :id AND e.envio.usuario.empresa.id = :empresaId")
    java.util.Optional<EventoLogistico> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Lista eventos activos por envío ordenados por fecha.
     */
    @Query("SELECT e FROM EventoLogistico e WHERE e.envio.id = :envioId AND e.activo = true " +
            "ORDER BY e.fechaEvento ASC, e.horaEvento ASC")
    List<EventoLogistico> findByEnvioIdAndActivoTrueOrderByFechaEventoAsc(@Param("envioId") Long envioId);
}