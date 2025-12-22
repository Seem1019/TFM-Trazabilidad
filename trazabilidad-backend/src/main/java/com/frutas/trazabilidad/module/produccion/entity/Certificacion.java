package com.frutas.trazabilidad.module.produccion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa certificaciones de una finca.
 * Ejemplos: GlobalG.A.P., Orgánico, Rainforest Alliance, etc.
 */
@Entity
@Table(name = "certificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finca_id", nullable = false)
    private Finca finca;

    @Column(name = "tipo_certificacion", nullable = false, length = 100)
    private String tipoCertificacion;

    @Column(name = "entidad_emisora", length = 200)
    private String entidadEmisora;

    @Column(name = "numero_certificado", length = 100)
    private String numeroCertificado;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "estado", length = 50)
    @Builder.Default
    private String estado = "VIGENTE"; // VIGENTE, VENCIDA, SUSPENDIDA

    @Column(name = "url_documento", length = 500)
    private String urlDocumento;

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
        actualizarEstado();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        actualizarEstado();
    }

    /**
     * Actualiza automáticamente el estado según la fecha de vencimiento.
     */
    public void actualizarEstado() {
        if (fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now())) {
            this.estado = "VENCIDA";
        }
    }

    /**
     * Verifica si la certificación está vigente.
     */
    public boolean isVigente() {
        return "VIGENTE".equals(estado) &&
                (fechaVencimiento == null || fechaVencimiento.isAfter(LocalDate.now()));
    }
}