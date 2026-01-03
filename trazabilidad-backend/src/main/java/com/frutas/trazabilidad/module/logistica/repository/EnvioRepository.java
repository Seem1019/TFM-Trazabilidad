package com.frutas.trazabilidad.module.logistica.repository;

import com.frutas.trazabilidad.module.logistica.entity.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de envíos/exportaciones.
 */
@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {

    /**
     * Busca un envío por su código único.
     */
    Optional<Envio> findByCodigoEnvio(String codigoEnvio);

    /**
     * Verifica si existe un envío con el código especificado.
     */
    boolean existsByCodigoEnvio(String codigoEnvio);

    /**
     * Lista envíos por empresa (a través del usuario creador).
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId AND e.activo = true ORDER BY e.createdAt DESC")
    List<Envio> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Lista envíos por estado y empresa.
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId AND e.estado = :estado AND e.activo = true ORDER BY e.createdAt DESC")
    List<Envio> findByEmpresaIdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") String estado);

    /**
     * Lista envíos por país de destino y empresa.
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId AND e.paisDestino = :paisDestino AND e.activo = true ORDER BY e.createdAt DESC")
    List<Envio> findByEmpresaIdAndPaisDestino(@Param("empresaId") Long empresaId, @Param("paisDestino") String paisDestino);

    /**
     * Lista envíos por rango de fechas de creación y empresa.
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId AND e.fechaCreacion BETWEEN :desde AND :hasta AND e.activo = true ORDER BY e.fechaCreacion DESC")
    List<Envio> findByEmpresaIdAndFechaCreacionBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    /**
     * Lista envíos pendientes de salida (fecha estimada próxima).
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId " +
            "AND e.estado IN ('CREADO', 'EN_PREPARACION', 'CARGADO') " +
            "AND e.fechaSalidaEstimada BETWEEN :desde AND :hasta " +
            "AND e.activo = true " +
            "ORDER BY e.fechaSalidaEstimada ASC")
    List<Envio> findPendientesSalidaProxima(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    /**
     * Busca envíos por exportador.
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId AND e.exportador LIKE %:exportador% AND e.activo = true ORDER BY e.createdAt DESC")
    List<Envio> findByEmpresaIdAndExportador(@Param("empresaId") Long empresaId, @Param("exportador") String exportador);

    /**
     * Busca envíos por transportista.
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId AND e.transportista LIKE %:transportista% AND e.activo = true ORDER BY e.createdAt DESC")
    List<Envio> findByEmpresaIdAndTransportista(@Param("empresaId") Long empresaId, @Param("transportista") String transportista);

    /**
     * Busca envíos por código de contenedor.
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId AND e.codigoContenedor = :codigoContenedor AND e.activo = true")
    Optional<Envio> findByEmpresaIdAndCodigoContenedor(@Param("empresaId") Long empresaId, @Param("codigoContenedor") String codigoContenedor);

    /**
     * Verifica si un envío pertenece a una empresa.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Envio e " +
            "WHERE e.id = :envioId AND e.usuario.empresa.id = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("envioId") Long envioId, @Param("empresaId") Long empresaId);

    /**
     * Lista envíos cerrados en un rango de fechas.
     */
    @Query("SELECT e FROM Envio e WHERE e.usuario.empresa.id = :empresaId " +
            "AND e.estado = 'CERRADO' " +
            "AND e.fechaCierre BETWEEN :desde AND :hasta " +
            "ORDER BY e.fechaCierre DESC")
    List<Envio> findCerradosEnRango(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );
}