package com.frutas.trazabilidad.module.empaque.entity;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un pallet (agrupación de cajas/etiquetas).
 * Tiene relación directa con Empresa para facilitar el filtrado multitenant.
 */
@Entity
@Table(name = "pallets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo_pallet", "empresa_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_pallet", nullable = false, length = 50)
    private String codigoPallet; // Ej: PLT-2024-00001

    /**
     * Empresa a la que pertenece el pallet (para aislamiento multitenant).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "fecha_paletizado", nullable = false)
    private LocalDate fechaPaletizado;

    @Column(name = "tipo_pallet", length = 50)
    private String tipoPallet; // EPAL, AMERICANO, PERSONALIZADO

    @Column(name = "numero_cajas", nullable = false)
    private Integer numeroCajas;

    @Column(name = "peso_neto_total")
    private Double pesoNetoTotal;

    @Column(name = "peso_bruto_total")
    private Double pesoBrutoTotal;

    @Column(name = "altura_pallet")
    private Double alturaPallet; // en cm

    @Column(name = "tipo_fruta", length = 100)
    private String tipoFruta;

    @Column(name = "calidad", length = 50)
    private String calidad;

    @Column(name = "destino", length = 200)
    private String destino; // País o cliente destino

    @Column(name = "temperatura_almacenamiento")
    private Double temperaturaAlmacenamiento;

    @Column(name = "responsable_paletizado", length = 150)
    private String responsablePaletizado;

    @Column(name = "estado_pallet", nullable = false, length = 30)
    @Builder.Default
    private String estadoPallet = "ARMADO"; // ARMADO, EN_CAMARA, ASIGNADO_ENVIO, DESPACHADO

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Relación con etiquetas (un pallet puede tener una etiqueta maestra)
    @OneToMany(mappedBy = "pallet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<EtiquetaPallet> etiquetas = new ArrayList<>();

    /**
     * Envío al que está asignado este pallet (opcional hasta que se crea el envío).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id")
    private Envio envio;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}