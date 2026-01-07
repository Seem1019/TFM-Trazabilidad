package com.frutas.trazabilidad.module.empaque.repository;

import com.frutas.trazabilidad.module.empaque.entity.EtiquetaPallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EtiquetaPalletRepository extends JpaRepository<EtiquetaPallet, Long> {

    /**
     * Busca la relación etiqueta-pallet por ID de etiqueta.
     */
    Optional<EtiquetaPallet> findByEtiquetaIdAndActivoTrue(Long etiquetaId);

    /**
     * Verifica si una etiqueta ya está asignada a un pallet.
     */
    boolean existsByEtiquetaIdAndActivoTrue(Long etiquetaId);
}
