package com.frutas.trazabilidad.module.produccion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un evento de cosecha de un lote.
 * Registra la cantidad cosechada y calidad inicial.
 */
@Entity
@Table(name = "cosechas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cosecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "fecha_cosecha", nullable = false)
    private LocalDate fechaCosecha;

    @Column(name = "cantidad_cosechada", nullable = false)
    private Double cantidadCosechada;

    @Column(name = "unidad_medida", nullable = false, length = 50)
    @Builder.Default
    private String unidadMedida = "kg"; // kg, toneladas, cajas, etc.

    @Column(name = "calidad_inicial", length = 50)
    private String calidadInicial; // PREMIUM, PRIMERA, SEGUNDA, INDUSTRIAL

    @Column(name = "estado_fruta", length = 100)
    private String estadoFruta; // VERDE, PINTONA, MADURA

    @Column(name = "responsable_cosecha", length = 150)
    private String responsableCosecha;

    @Column(name = "numero_trabajadores")
    private Integer numeroTrabajadores;

    @Column(name = "hora_inicio")
    private String horaInicio;

    @Column(name = "hora_fin")
    private String horaFin;

    @Column(name = "temperatura_ambiente")
    private Double temperaturaAmbiente;

    @Column(columnDefinition = "TEXT")
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

    /**
     * Calcula el rendimiento por hectárea (si el lote tiene área definida).
     */
    public Double getRendimientoPorHectarea() {
        if (lote == null || lote.getAreaHectareas() == null || lote.getAreaHectareas() == 0) {
            return null;
        }
        return cantidadCosechada / lote.getAreaHectareas();
    }

    /**
     * Verifica si la cosecha es reciente (últimas 24 horas).
     */
    public boolean isReciente() {
        return fechaCosecha != null && fechaCosecha.equals(LocalDate.now());
    }
}