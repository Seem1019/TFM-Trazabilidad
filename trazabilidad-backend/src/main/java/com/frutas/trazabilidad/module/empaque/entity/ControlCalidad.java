package com.frutas.trazabilidad.module.empaque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un control de calidad realizado en planta.
 */
@Entity
@Table(name = "controles_calidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlCalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clasificacion_id")
    private Clasificacion clasificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pallet_id")
    private Pallet pallet;

    @Column(name = "codigo_control", nullable = false, unique = true, length = 50)
    private String codigoControl; // Ej: CC-2024-001

    @Column(name = "fecha_control", nullable = false)
    private LocalDate fechaControl;

    @Column(name = "tipo_control", nullable = false, length = 100)
    private String tipoControl; // VISUAL, LABORATORIO, SANITARIO, EMPAQUE

    @Column(name = "parametro_evaluado", length = 200)
    private String parametroEvaluado; // BRIX, ACIDEZ, FIRMEZA, PESO, etc

    @Column(name = "valor_medido", length = 100)
    private String valorMedido;

    @Column(name = "valor_esperado", length = 100)
    private String valorEsperado;

    @Column(name = "cumple_especificacion", nullable = false)
    @Builder.Default
    private Boolean cumpleEspecificacion = true;

    @Column(name = "resultado", nullable = false, length = 30)
    private String resultado; // APROBADO, RECHAZADO, CONDICIONAL

    @Column(name = "responsable_control", nullable = false, length = 150)
    private String responsableControl;

    @Column(name = "laboratorio", length = 200)
    private String laboratorio; // Si es control de laboratorio externo

    @Column(name = "numero_certificado", length = 100)
    private String numeroCertificado; // Si se emite certificado

    @Column(name = "accion_correctiva", columnDefinition = "TEXT")
    private String accionCorrectiva; // Qué se hizo si no cumplió

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