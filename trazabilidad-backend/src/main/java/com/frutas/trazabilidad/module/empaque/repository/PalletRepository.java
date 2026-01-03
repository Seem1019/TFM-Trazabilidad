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

    // Buscar por código
    Optional<Pallet> findByCodigoPallet(String codigoPallet);

    // Listar todos los pallets (sin multiempresa directa, se valida por etiquetas)
    List<Pallet> findByActivoTrueOrderByFechaPaletizadoDesc();

    // Listar por estado
    List<Pallet> findByEstadoPalletAndActivoTrue(String estado);

    // Listar por destino
    List<Pallet> findByDestinoContainingIgnoreCaseAndActivoTrue(String destino);

    // Listar por tipo fruta
    List<Pallet> findByTipoFrutaAndActivoTrue(String tipoFruta);

    // Listar por calidad
    List<Pallet> findByCalidadAndActivoTrue(String calidad);

    // Verificar código único
    boolean existsByCodigoPallet(String codigoPallet);

    // Buscar pallets listos para envío (armados o en cámara)
    @Query("SELECT p FROM Pallet p " +
            "WHERE p.estadoPallet IN ('ARMADO', 'EN_CAMARA') " +
            "AND p.activo = true " +
            "ORDER BY p.fechaPaletizado")
    List<Pallet> findPalletsListosParaEnvio();
}