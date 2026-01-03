package com.frutas.trazabilidad.module.logistica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un documento de exportación asociado a un envío.
 * Gestiona packing lists, certificados fitosanitarios, facturas comerciales, etc.
 */
@Entity
@Table(name = "documentos_exportacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoExportacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Envío al que pertenece este documento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    /**
     * Tipo de documento.
     * Valores: PACKING_LIST, CERTIFICADO_FITOSANITARIO, FACTURA_COMERCIAL,
     *          BL, CERTIFICADO_ORIGEN, LISTA_EMPAQUE, OTRO
     */
    @Column(nullable = false, length = 50)
    private String tipoDocumento;

    /**
     * Número del documento.
     * Ejemplo: "PL-2025-001", "CFE-123456"
     */
    @Column(nullable = false, length = 100)
    private String numeroDocumento;

    /**
     * Fecha de emisión del documento.
     */
    @Column(nullable = false)
    private LocalDate fechaEmision;

    /**
     * Fecha de vencimiento del documento (si aplica).
     * Ejemplo: certificados fitosanitarios tienen vigencia limitada.
     */
    private LocalDate fechaVencimiento;

    /**
     * Entidad emisora del documento.
     * Ejemplo: "ICA", "Aduana Nacional", "Exportadora Frutas Colombia"
     */
    @Column(length = 200)
    private String entidadEmisora;

    /**
     * Nombre del funcionario que emitió/firmó el documento.
     */
    @Column(length = 200)
    private String funcionarioEmisor;

    /**
     * URL del archivo digital del documento (PDF, imagen, etc.).
     * Podría apuntar a S3, Google Drive, etc.
     */
    @Column(length = 500)
    private String urlArchivo;

    /**
     * Tipo MIME del archivo (ej: "application/pdf", "image/jpeg").
     */
    @Column(length = 50)
    private String tipoArchivo;

    /**
     * Tamaño del archivo en bytes.
     */
    private Long tamanoArchivo;

    /**
     * Hash SHA-256 del archivo (para verificar integridad).
     */
    @Column(length = 64)
    private String hashArchivo;

    /**
     * Estado del documento.
     * Valores: GENERADO, FIRMADO, ENVIADO, APROBADO, RECHAZADO
     */
    @Column(nullable = false, length = 20)
    private String estado = "GENERADO";

    /**
     * Descripción o notas adicionales del documento.
     */
    @Column(length = 1000)
    private String descripcion;

    /**
     * Valor declarado en el documento (para facturas comerciales).
     */
    private Double valorDeclarado;

    /**
     * Moneda del valor declarado (USD, EUR, COP).
     */
    @Column(length = 10)
    private String moneda;

    /**
     * Indica si el documento es obligatorio para el envío.
     */
    @Column(nullable = false)
    private Boolean obligatorio = false;

    /**
     * Indica si el registro está activo (soft delete).
     */
    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Métodos de utilidad

    /**
     * Verifica si el documento está vencido.
     */
    public boolean estaVencido() {
        if (fechaVencimiento == null) {
            return false;
        }
        return LocalDate.now().isAfter(fechaVencimiento);
    }

    /**
     * Verifica si el documento está aprobado.
     */
    public boolean estaAprobado() {
        return "APROBADO".equals(estado);
    }

    /**
     * Verifica si el documento tiene archivo adjunto.
     */
    public boolean tieneArchivo() {
        return urlArchivo != null && !urlArchivo.isEmpty();
    }
}