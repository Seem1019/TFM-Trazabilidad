package com.frutas.trazabilidad.module.empaque.repository;

import com.frutas.trazabilidad.module.empaque.entity.RecepcionPlanta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecepcionPlantaRepository extends JpaRepository<RecepcionPlanta, Long> {

    // Buscar por código único
    Optional<RecepcionPlanta> findByCodigoRecepcion(String codigoRecepcion);

    // Listar recepciones por lote
    List<RecepcionPlanta> findByLoteIdAndActivoTrueOrderByFechaRecepcionDesc(Long loteId);

    // Listar recepciones por empresa (vía lote)
    @Query("SELECT r FROM RecepcionPlanta r " +
            "WHERE r.lote.finca.empresa.id = :empresaId " +
            "AND r.activo = true " +
            "ORDER BY r.fechaRecepcion DESC")
    List<RecepcionPlanta> findByEmpresaId(@Param("empresaId") Long empresaId);

    // Listar recepciones por estado
    @Query("SELECT r FROM RecepcionPlanta r " +
            "WHERE r.lote.finca.empresa.id = :empresaId " +
            "AND r.estadoRecepcion = :estado " +
            "AND r.activo = true")
    List<RecepcionPlanta> findByEmpresaIdAndEstado(
            @Param("empresaId") Long empresaId,
            @Param("estado") String estado
    );

    // Recepciones por rango de fechas
    @Query("SELECT r FROM RecepcionPlanta r " +
            "WHERE r.lote.finca.empresa.id = :empresaId " +
            "AND r.fechaRecepcion BETWEEN :desde AND :hasta " +
            "AND r.activo = true " +
            "ORDER BY r.fechaRecepcion DESC")
    List<RecepcionPlanta> findByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    // Verificar si código ya existe
    boolean existsByCodigoRecepcion(String codigoRecepcion);

    // Validar pertenencia a empresa
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM RecepcionPlanta r " +
            "WHERE r.id = :id AND r.lote.finca.empresa.id = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    // Validar recepción pertenencia a empresa
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM RecepcionPlanta r " +
            "WHERE r.id = :recepcionId AND r.lote.finca.empresa.id = :empresaId")
    boolean existsByRecepcionIdAndEmpresaId(@Param("recepcionId") Long recepcionId, @Param("empresaId") Long empresaId);
}