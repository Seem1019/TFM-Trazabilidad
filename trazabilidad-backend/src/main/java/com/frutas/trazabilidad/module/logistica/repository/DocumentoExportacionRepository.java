package com.frutas.trazabilidad.module.logistica.repository;

import com.frutas.trazabilidad.module.logistica.entity.DocumentoExportacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de documentos de exportación.
 */
@Repository
public interface DocumentoExportacionRepository extends JpaRepository<DocumentoExportacion, Long> {

    /**
     * Lista documentos por envío.
     */
    @Query("SELECT d FROM DocumentoExportacion d WHERE d.envio.id = :envioId AND d.activo = true ORDER BY d.fechaEmision DESC")
    List<DocumentoExportacion> findByEnvioId(@Param("envioId") Long envioId);

    /**
     * Lista documentos por tipo y envío.
     */
    @Query("SELECT d FROM DocumentoExportacion d WHERE d.envio.id = :envioId AND d.tipoDocumento = :tipoDocumento AND d.activo = true ORDER BY d.fechaEmision DESC")
    List<DocumentoExportacion> findByEnvioIdAndTipoDocumento(@Param("envioId") Long envioId, @Param("tipoDocumento") String tipoDocumento);

    /**
     * Busca un documento específico por número.
     */
    @Query("SELECT d FROM DocumentoExportacion d WHERE d.numeroDocumento = :numeroDocumento AND d.activo = true")
    Optional<DocumentoExportacion> findByNumeroDocumento(@Param("numeroDocumento") String numeroDocumento);

    /**
     * Verifica si existe un documento con el número especificado.
     */
    boolean existsByNumeroDocumento(String numeroDocumento);

    /**
     * Lista documentos por estado.
     */
    @Query("SELECT d FROM DocumentoExportacion d WHERE d.envio.id = :envioId AND d.estado = :estado AND d.activo = true ORDER BY d.fechaEmision DESC")
    List<DocumentoExportacion> findByEnvioIdAndEstado(@Param("envioId") Long envioId, @Param("estado") String estado);

    /**
     * Lista documentos obligatorios pendientes de un envío.
     */
    @Query("SELECT d FROM DocumentoExportacion d WHERE d.envio.id = :envioId " +
            "AND d.obligatorio = true " +
            "AND d.estado NOT IN ('APROBADO', 'FIRMADO') " +
            "AND d.activo = true")
    List<DocumentoExportacion> findObligatoriosPendientesByEnvioId(@Param("envioId") Long envioId);

    /**
     * Lista documentos próximos a vencer.
     */
    @Query("SELECT d FROM DocumentoExportacion d WHERE d.envio.usuario.empresa.id = :empresaId " +
            "AND d.fechaVencimiento BETWEEN :desde AND :hasta " +
            "AND d.activo = true " +
            "ORDER BY d.fechaVencimiento ASC")
    List<DocumentoExportacion> findProximosAVencer(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    /**
     * Lista documentos por entidad emisora.
     */
    @Query("SELECT d FROM DocumentoExportacion d WHERE d.envio.usuario.empresa.id = :empresaId " +
            "AND d.entidadEmisora LIKE %:entidadEmisora% " +
            "AND d.activo = true " +
            "ORDER BY d.fechaEmision DESC")
    List<DocumentoExportacion> findByEmpresaIdAndEntidadEmisora(
            @Param("empresaId") Long empresaId,
            @Param("entidadEmisora") String entidadEmisora
    );

    /**
     * Verifica si un documento pertenece a la empresa (a través del envío).
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DocumentoExportacion d " +
            "WHERE d.id = :documentoId AND d.envio.usuario.empresa.id = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("documentoId") Long documentoId, @Param("empresaId") Long empresaId);

    /**
     * Cuenta documentos aprobados para un envío.
     */
    @Query("SELECT COUNT(d) FROM DocumentoExportacion d WHERE d.envio.id = :envioId AND d.estado = 'APROBADO' AND d.activo = true")
    long countAprobadosByEnvioId(@Param("envioId") Long envioId);
}