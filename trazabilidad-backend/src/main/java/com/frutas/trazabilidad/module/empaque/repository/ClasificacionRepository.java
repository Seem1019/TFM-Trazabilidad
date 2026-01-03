package com.frutas.trazabilidad.module.empaque.repository;

import com.frutas.trazabilidad.module.empaque.entity.Clasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Long> {

    // Buscar por código único
    Optional<Clasificacion> findByCodigoClasificacion(String codigoClasificacion);

    // Listar clasificaciones por recepción
    List<Clasificacion> findByRecepcionIdAndActivoTrueOrderByFechaClasificacionDesc(Long recepcionId);

    // Listar clasificaciones por empresa
    @Query("SELECT c FROM Clasificacion c " +
            "WHERE c.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND c.activo = true " +
            "ORDER BY c.fechaClasificacion DESC")
    List<Clasificacion> findByEmpresaId(@Param("empresaId") Long empresaId);

    // Listar por calidad específica
    @Query("SELECT c FROM Clasificacion c " +
            "WHERE c.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND c.calidad = :calidad " +
            "AND c.activo = true")
    List<Clasificacion> findByEmpresaIdAndCalidad(
            @Param("empresaId") Long empresaId,
            @Param("calidad") String calidad
    );

    // Estadísticas de calidad por empresa
    @Query("SELECT c.calidad, SUM(c.cantidadClasificada) " +
            "FROM Clasificacion c " +
            "WHERE c.recepcion.lote.finca.empresa.id = :empresaId " +
            "AND c.activo = true " +
            "GROUP BY c.calidad")
    List<Object[]> getEstadisticasCalidadByEmpresa(@Param("empresaId") Long empresaId);

    // Verificar código único
    boolean existsByCodigoClasificacion(String codigoClasificacion);

    // Validar pertenencia a empresa
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Clasificacion c " +
            "WHERE c.id = :id AND c.recepcion.lote.finca.empresa.id = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}