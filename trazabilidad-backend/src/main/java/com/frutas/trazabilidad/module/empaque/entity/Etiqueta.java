package com.frutas.trazabilidad.module.empaque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una etiqueta de trazabilidad con código QR.
 * Cada etiqueta tiene un código UUID único para consulta pública.
 */
@Entity
@Table(name = "etiquetas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clasificacion_id", nullable = false)
    private Clasificacion clasificacion;

    @Column(name = "codigo_etiqueta", nullable = false, unique = true, length = 50)
    private String codigoEtiqueta; // Ej: ETQ-2024-00001

    @Column(name = "codigo_qr", nullable = false, unique = true, length = 36)
    private String codigoQr; // UUID para trazabilidad pública

    @Column(name = "tipo_etiqueta", nullable = false, length = 30)
    private String tipoEtiqueta; // CAJA, PALLET

    @Column(name = "cantidad_contenida")
    private Double cantidadContenida; // Cantidad de fruta en esta etiqueta

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @Column(name = "peso_neto")
    private Double pesoNeto;

    @Column(name = "peso_bruto")
    private Double pesoBruto;

    @Column(name = "numero_cajas")
    private Integer numeroCajas; // Si es pallet, cuántas cajas tiene

    @Column(name = "estado_etiqueta", nullable = false, length = 30)
    @Builder.Default
    private String estadoEtiqueta = "DISPONIBLE"; // DISPONIBLE, ASIGNADA_PALLET, ENVIADA

    @Column(name = "url_qr", length = 500)
    private String urlQr; // URL pública para consultar trazabilidad

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

        // Generar código QR único si no existe
        if (codigoQr == null) {
            codigoQr = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Genera la URL pública para consultar la trazabilidad.
     */
    public void generarUrlQr(String baseUrl) {
        this.urlQr = baseUrl + "/public/traza/" + this.codigoQr;
    }
}