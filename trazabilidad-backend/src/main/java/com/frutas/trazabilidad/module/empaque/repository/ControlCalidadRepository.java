package com.frutas.trazabilidad.module.empaque.repository;

import com.frutas.trazabilidad.module.empaque.entity.ControlCalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ControlCalidadRepository extends JpaRepository<ControlCalidad, Long> {

    // Buscar por código
    Optional<ControlCalidad> findByCodigoControl(String codigoControl);

    // Listar controles por clasificación
    List<ControlCalidad> findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(Long clasificacionId);

    // Listar controles por pallet
    List<ControlCalidad> findByPalletIdAndActivoTrueOrderByFechaControlDesc(Long palletId);

    // Listar por empresa (vía clasificación)
    @Query("SELECT c FROM ControlCalidad c " +
            "WHERE c.clasificacion.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND c.activo = true " +
            "ORDER BY c.fechaControl DESC")
    List<ControlCalidad> findByEmpresaId(@Param("empresaId") Long empresaId);

    // Listar por tipo de control
    @Query("SELECT c FROM ControlCalidad c " +
            "WHERE c.clasificacion.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND c.tipoControl = :tipo " +
            "AND c.activo = true")
    List<ControlCalidad> findByEmpresaIdAndTipo(
            @Param("empresaId") Long empresaId,
            @Param("tipo") String tipo
    );

    // Listar por resultado
    @Query("SELECT c FROM ControlCalidad c " +
            "WHERE c.clasificacion.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND c.resultado = :resultado " +
            "AND c.activo = true")
    List<ControlCalidad> findByEmpresaIdAndResultado(
            @Param("empresaId") Long empresaId,
            @Param("resultado") String resultado
    );

    // Controles por rango de fechas
    @Query("SELECT c FROM ControlCalidad c " +
            "WHERE c.clasificacion.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND c.fechaControl BETWEEN :desde AND :hasta " +
            "AND c.activo = true " +
            "ORDER BY c.fechaControl DESC")
    List<ControlCalidad> findByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    // Estadísticas de cumplimiento
    @Query("SELECT c.resultado, COUNT(c) " +
            "FROM ControlCalidad c " +
            "WHERE c.clasificacion.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND c.activo = true " +
            "GROUP BY c.resultado")
    List<Object[]> getEstadisticasResultados(@Param("empresaId") Long empresaId);

    // Verificar código único
    boolean existsByCodigoControl(String codigoControl);

    // Validar pertenencia a empresa
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM ControlCalidad c " +
            "WHERE c.id = :id AND c.clasificacion.recepcion.lote.finca.empresa.id = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}