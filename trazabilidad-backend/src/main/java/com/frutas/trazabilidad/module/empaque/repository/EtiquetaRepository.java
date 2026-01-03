package com.frutas.trazabilidad.module.empaque.repository;

import com.frutas.trazabilidad.module.empaque.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    // Buscar por código QR (para consulta pública)
    Optional<Etiqueta> findByCodigoQr(String codigoQr);

    // Buscar por código etiqueta
    Optional<Etiqueta> findByCodigoEtiqueta(String codigoEtiqueta);

    // Listar etiquetas por clasificación
    List<Etiqueta> findByClasificacionIdAndActivoTrueOrderByCreatedAtDesc(Long clasificacionId);

    // Listar etiquetas por empresa
    @Query("SELECT e FROM Etiqueta e " +
            "WHERE e.clasificacion.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND e.activo = true " +
            "ORDER BY e.createdAt DESC")
    List<Etiqueta> findByEmpresaId(@Param("empresaId") Long empresaId);

    // Listar etiquetas por estado
    @Query("SELECT e FROM Etiqueta e " +
            "WHERE e.clasificacion.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND e.estadoEtiqueta = :estado " +
            "AND e.activo = true")
    List<Etiqueta> findByEmpresaIdAndEstado(
            @Param("empresaId") Long empresaId,
            @Param("estado") String estado
    );

    // Listar etiquetas por tipo
    @Query("SELECT e FROM Etiqueta e " +
            "WHERE e.clasificacion.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND e.tipoEtiqueta = :tipo " +
            "AND e.activo = true")
    List<Etiqueta> findByEmpresaIdAndTipo(
            @Param("empresaId") Long empresaId,
            @Param("tipo") String tipo
    );

    // Verificar códigos únicos
    boolean existsByCodigoEtiqueta(String codigoEtiqueta);
    boolean existsByCodigoQr(String codigoQr);

    // Validar pertenencia a empresa
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Etiqueta e " +
            "WHERE e.id = :id AND e.clasificacion.recepcion.lote.finca.empresa.id = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}