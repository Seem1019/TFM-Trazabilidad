package com.frutas.trazabilidad.module.empaque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad intermedia que relaciona etiquetas con pallets.
 * Permite trazabilidad detallada de qué etiquetas están en cada pallet.
 */
@Entity
@Table(name = "etiquetas_pallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtiquetaPallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etiqueta_id", nullable = false)
    private Etiqueta etiqueta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pallet_id", nullable = false)
    private Pallet pallet;

    @Column(name = "posicion_en_pallet")
    private Integer posicionEnPallet; // Número de caja dentro del pallet

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaAsignacion = LocalDateTime.now();
    }
}