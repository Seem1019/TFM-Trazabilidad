package com.frutas.trazabilidad.module.empaque.entity;

import com.frutas.trazabilidad.module.produccion.entity.Lote;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la recepción de un lote en planta de empaque.
 */
@Entity
@Table(name = "recepciones_planta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecepcionPlanta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "codigo_recepcion", nullable = false, unique = true, length = 50)
    private String codigoRecepcion; // Ej: REC-2024-001

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDate fechaRecepcion;

    @Column(name = "hora_recepcion", length = 10)
    private String horaRecepcion; // HH:mm formato

    @Column(name = "cantidad_recibida", nullable = false)
    private Double cantidadRecibida;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida; // kg, toneladas, cajas

    @Column(name = "temperatura_fruta")
    private Double temperaturaFruta; // Temperatura al momento de recepción

    @Column(name = "estado_inicial", length = 50)
    private String estadoInicial; // VERDE, PINTONA, MADURA, SOBREMADURA

    @Column(name = "responsable_recepcion", length = 150)
    private String responsableRecepcion;

    @Column(name = "vehiculo_transporte", length = 50)
    private String vehiculoTransporte;

    @Column(name = "conductor", length = 100)
    private String conductor;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "estado_recepcion", nullable = false, length = 30)
    @Builder.Default
    private String estadoRecepcion = "RECIBIDA"; // RECIBIDA, EN_CLASIFICACION, PROCESADA

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