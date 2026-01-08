package com.frutas.trazabilidad.module.produccion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un lote de cultivo dentro de una finca.
 * Un lote es una porción específica de terreno sembrada con un cultivo.
 */
@Entity
@Table(name = "lotes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo_lote", "finca_id"})
})
@EntityListeners(com.frutas.trazabilidad.listener.AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finca_id", nullable = false)
    private Finca finca;

    @Column(name = "codigo_lote", nullable = false, length = 50)
    private String codigoLote;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_fruta", nullable = false, length = 100)
    private String tipoFruta;

    @Column(length = 100)
    private String variedad;

    @Column(name = "area_hectareas")
    private Double areaHectareas;

    @Column(name = "fecha_siembra")
    private LocalDate fechaSiembra;

    @Column(name = "fecha_primera_cosecha_estimada")
    private LocalDate fechaPrimeraCosechaEstimada;

    @Column(name = "densidad_siembra")
    private Integer densidadSiembra; // Plantas por hectárea

    @Column(name = "ubicacion_interna", length = 200)
    private String ubicacionInterna;

    @Column(name = "estado_lote", length = 50)
    @Builder.Default
    private String estadoLote = "ACTIVO"; // ACTIVO, EN_COSECHA, COSECHADO, RETIRADO

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ActividadAgronomica> actividades = new ArrayList<>();

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Cosecha> cosechas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calcula la edad del lote en días desde la siembra.
     */
    public long getEdadEnDias() {
        if (fechaSiembra == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(fechaSiembra, LocalDate.now());
    }

    /**
     * Verifica si el lote está listo para cosechar (estimado).
     */
    public boolean isListoParaCosechar() {
        return fechaPrimeraCosechaEstimada != null &&
                !fechaPrimeraCosechaEstimada.isAfter(LocalDate.now());
    }
}