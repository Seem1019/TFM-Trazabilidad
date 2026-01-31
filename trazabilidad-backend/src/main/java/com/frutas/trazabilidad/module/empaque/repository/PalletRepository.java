package com.frutas.trazabilidad.module.empaque.repository;

import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PalletRepository extends JpaRepository<Pallet, Long> {

    // ==================== MÉTODOS CON FILTRO MULTITENANT ====================

    /**
     * Lista todos los pallets activos de una empresa.
     */
    List<Pallet> findByEmpresaIdAndActivoTrueOrderByFechaPaletizadoDesc(Long empresaId);

    /**
     * Busca un pallet por ID verificando que pertenezca a la empresa.
     */
    Optional<Pallet> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Busca un pallet por código dentro de una empresa.
     */
    Optional<Pallet> findByCodigoPalletAndEmpresaId(String codigoPallet, Long empresaId);

    /**
     * Lista pallets por estado dentro de una empresa.
     */
    List<Pallet> findByEmpresaIdAndEstadoPalletAndActivoTrue(Long empresaId, String estado);

    /**
     * Lista pallets por destino dentro de una empresa.
     */
    List<Pallet> findByEmpresaIdAndDestinoContainingIgnoreCaseAndActivoTrue(Long empresaId, String destino);

    /**
     * Lista pallets por tipo de fruta dentro de una empresa.
     */
    List<Pallet> findByEmpresaIdAndTipoFrutaAndActivoTrue(Long empresaId, String tipoFruta);

    /**
     * Lista pallets por calidad dentro de una empresa.
     */
    List<Pallet> findByEmpresaIdAndCalidadAndActivoTrue(Long empresaId, String calidad);

    /**
     * Verifica si existe un pallet con el código dado en la empresa.
     */
    boolean existsByCodigoPalletAndEmpresaId(String codigoPallet, Long empresaId);

    /**
     * Busca pallets listos para envío (armados o en cámara) dentro de una empresa.
     */
    @Query("SELECT p FROM Pallet p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.estadoPallet IN ('ARMADO', 'EN_CAMARA') " +
            "AND p.activo = true " +
            "ORDER BY p.fechaPaletizado")
    List<Pallet> findPalletsListosParaEnvio(@Param("empresaId") Long empresaId);

    /**
     * Cuenta pallets activos por empresa.
     */
    long countByEmpresaIdAndActivoTrue(Long empresaId);

    // ==================== MÉTODOS LEGACY (SIN FILTRO - DEPRECADOS) ====================

    /**
     * @deprecated Use findByCodigoPalletAndEmpresaId instead
     */
    @Deprecated
    Optional<Pallet> findByCodigoPallet(String codigoPallet);

    /**
     * @deprecated Use findByEmpresaIdAndActivoTrueOrderByFechaPaletizadoDesc instead
     */
    @Deprecated
    List<Pallet> findByActivoTrueOrderByFechaPaletizadoDesc();

    /**
     * @deprecated Use findByEmpresaIdAndEstadoPalletAndActivoTrue instead
     */
    @Deprecated
    List<Pallet> findByEstadoPalletAndActivoTrue(String estado);

    /**
     * @deprecated Use findByEmpresaIdAndDestinoContainingIgnoreCaseAndActivoTrue instead
     */
    @Deprecated
    List<Pallet> findByDestinoContainingIgnoreCaseAndActivoTrue(String destino);

    /**
     * @deprecated Use findByEmpresaIdAndTipoFrutaAndActivoTrue instead
     */
    @Deprecated
    List<Pallet> findByTipoFrutaAndActivoTrue(String tipoFruta);

    /**
     * @deprecated Use findByEmpresaIdAndCalidadAndActivoTrue instead
     */
    @Deprecated
    List<Pallet> findByCalidadAndActivoTrue(String calidad);

    /**
     * @deprecated Use existsByCodigoPalletAndEmpresaId instead
     */
    @Deprecated
    boolean existsByCodigoPallet(String codigoPallet);

    /**
     * @deprecated Use findPalletsListosParaEnvio(empresaId) instead
     */
    @Deprecated
    @Query("SELECT p FROM Pallet p " +
            "WHERE p.estadoPallet IN ('ARMADO', 'EN_CAMARA') " +
            "AND p.activo = true " +
            "ORDER BY p.fechaPaletizado")
    List<Pallet> findPalletsListosParaEnvioSinFiltro();
}
