package com.frutas.trazabilidad.module.produccion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa actividades agronómicas realizadas en un lote.
 * Ejemplos: fertilización, fumigación, riego, poda, etc.
 */
@Entity
@Table(name = "actividades_agronomicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadAgronomica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "tipo_actividad", nullable = false, length = 100)
    private String tipoActividad; // FERTILIZACION, FUMIGACION, RIEGO, PODA, etc.

    @Column(name = "fecha_actividad", nullable = false)
    private LocalDate fechaActividad;

    @Column(name = "producto_aplicado", length = 200)
    private String productoAplicado;

    @Column(name = "dosis_cantidad", length = 100)
    private String dosisoCantidad;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida; // kg, litros, gramos, etc.

    @Column(name = "metodo_aplicacion", length = 100)
    private String metodoAplicacion;

    @Column(name = "responsable", length = 150)
    private String responsable;

    @Column(name = "numero_registro_producto", length = 100)
    private String numeroRegistroProducto; // Registro ICA u otro

    @Column(name = "intervalo_seguridad_dias")
    private Integer intervaloSeguridadDias;

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
     * Calcula la fecha en la que se cumple el intervalo de seguridad.
     */
    public LocalDate getFechaFinIntervaloSeguridad() {
        if (fechaActividad == null || intervaloSeguridadDias == null) {
            return null;
        }
        return fechaActividad.plusDays(intervaloSeguridadDias);
    }

    /**
     * Verifica si ya se cumplió el intervalo de seguridad.
     */
    public boolean isIntervaloSeguridadCumplido() {
        LocalDate fechaFin = getFechaFinIntervaloSeguridad();
        return fechaFin == null || !fechaFin.isAfter(LocalDate.now());
    }
}