package com.frutas.trazabilidad.module.produccion.repository;

import com.frutas.trazabilidad.module.produccion.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Lote.
 */
@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    /**
     * Busca todos los lotes activos de una finca.
     */
    List<Lote> findByFincaIdAndActivoTrue(Long fincaId);

    /**
     * Busca un lote por ID verificando que pertenezca a la empresa.
     */
    @Query("SELECT l FROM Lote l WHERE l.id = :id AND l.finca.empresa.id = :empresaId")
    Optional<Lote> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Verifica si existe un lote con el código dado en la finca.
     */
    boolean existsByCodigoLoteAndFincaId(String codigoLote, Long fincaId);

    /**
     * Busca lotes por tipo de fruta en una finca.
     */
    List<Lote> findByFincaIdAndTipoFrutaAndActivoTrue(Long fincaId, String tipoFruta);

    /**
     * Busca todos los lotes activos de una empresa.
     */
    @Query("SELECT l FROM Lote l WHERE l.finca.empresa.id = :empresaId AND l.activo = true")
    List<Lote> findAllByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Busca lotes por estado en una finca.
     */
    List<Lote> findByFincaIdAndEstadoLoteAndActivoTrue(Long fincaId, String estadoLote);

    /**
     * Busca lotes listos para cosechar (fecha estimada <= hoy).
     */
    @Query("SELECT l FROM Lote l WHERE l.finca.empresa.id = :empresaId " +
            "AND l.activo = true AND l.estadoLote = 'ACTIVO' " +
            "AND l.fechaPrimeraCosechaEstimada <= CURRENT_DATE")
    List<Lote> findListosParaCosechar(@Param("empresaId") Long empresaId);

    /**
     * Cuenta lotes activos de una finca.
     */
    long countByFincaIdAndActivoTrue(Long fincaId);

    /**
     * Busca lotes por tipo de fruta en toda la empresa.
     */
    @Query("SELECT l FROM Lote l WHERE l.finca.empresa.id = :empresaId " +
            "AND l.tipoFruta = :tipoFruta AND l.activo = true")
    List<Lote> findByEmpresaIdAndTipoFruta(@Param("empresaId") Long empresaId,
                                           @Param("tipoFruta") String tipoFruta);

    /**
     * Verifica si existe un lote con el ID dado que pertenece a la empresa.
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
            "FROM Lote l WHERE l.id = :loteId AND l.finca.empresa.id = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    /**
     * Actualiza el estado del lote directamente con JPQL.
     * Evita ConcurrentModificationException al no pasar por el entity graph
     * cuando hay entidades relacionadas con CascadeType.ALL pendientes de flush.
     */
    @Modifying
    @Query("UPDATE Lote l SET l.estadoLote = :estado, l.updatedAt = CURRENT_TIMESTAMP WHERE l.id = :id")
    void updateEstadoLote(@Param("id") Long id, @Param("estado") String estado);
}