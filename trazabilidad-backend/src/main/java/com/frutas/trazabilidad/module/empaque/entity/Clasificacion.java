package com.frutas.trazabilidad.module.empaque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa el proceso de clasificación de fruta recibida.
 */
@Entity
@Table(name = "clasificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clasificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recepcion_id", nullable = false)
    private RecepcionPlanta recepcion;

    @Column(name = "codigo_clasificacion", nullable = false, unique = true, length = 50)
    private String codigoClasificacion; // Ej: CLAS-2024-001

    @Column(name = "fecha_clasificacion", nullable = false)
    private LocalDate fechaClasificacion;

    @Column(name = "calidad", nullable = false, length = 50)
    private String calidad; // PREMIUM, PRIMERA, SEGUNDA, TERCERA, RECHAZO

    @Column(name = "cantidad_clasificada", nullable = false)
    private Double cantidadClasificada;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "calibre", length = 30)
    private String calibre; // 10, 12, 14, 16, 18, etc (depende del tipo de fruta)

    @Column(name = "porcentaje_merma")
    private Double porcentajeMerma;

    @Column(name = "cantidad_merma")
    private Double cantidadMerma;

    @Column(name = "motivo_merma", columnDefinition = "TEXT")
    private String motivoMerma; // GOLPES, MADUREZ_EXCESIVA, PLAGAS, etc

    @Column(name = "responsable_clasificacion", length = 150)
    private String responsableClasificacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

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