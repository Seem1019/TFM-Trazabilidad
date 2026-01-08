package com.frutas.trazabilidad.module.logistica.entity;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una orden de envío/exportación.
 * Agrupa pallets listos para ser exportados y gestiona su documentación y tracking.
 */
@Entity
@Table(name = "envios")
@EntityListeners(com.frutas.trazabilidad.listener.AuditEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código único del envío (generado por la empresa).
     * Ejemplo: "ENV-2025-001", "EXPORT-US-001"
     */
    @Column(nullable = false, unique = true, length = 50)
    private String codigoEnvio;

    /**
     * Usuario que creó la orden de envío.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    /**
     * Fecha en que se creó la orden de envío.
     */
    @Column(nullable = false)
    private LocalDate fechaCreacion;

    /**
     * Fecha estimada de salida.
     */
    private LocalDate fechaSalidaEstimada;

    /**
     * Fecha real de salida del país.
     */
    private LocalDate fechaSalidaReal;

    /**
     * Exportador responsable (puede ser diferente de quien creó el envío).
     */
    @Column(length = 200)
    private String exportador;

    /**
     * País de destino (ej: "Estados Unidos", "España").
     */
    @Column(nullable = false, length = 100)
    private String paisDestino;

    /**
     * Puerto de destino (ej: "Miami", "Rotterdam").
     */
    @Column(length = 100)
    private String puertoDestino;

    /**
     * Ciudad destino final.
     */
    @Column(length = 100)
    private String ciudadDestino;

    /**
     * Tipo de transporte (MARITIMO, AEREO, TERRESTRE).
     */
    @Column(nullable = false, length = 20)
    private String tipoTransporte;

    /**
     * Código del contenedor asignado (ej: "MSCU1234567").
     * Modelo simplificado: 1 envío = 1 contenedor principal.
     */
    @Column(length = 50)
    private String codigoContenedor;

    /**
     * Tipo de contenedor (ej: "Reefer 40ft", "Dry 20ft").
     */
    @Column(length = 50)
    private String tipoContenedor;

    /**
     * Temperatura configurada para el contenedor (°C).
     */
    private Double temperaturaContenedor;

    /**
     * Naviera o aerolínea encargada del transporte.
     */
    @Column(length = 200)
    private String transportista;

    /**
     * Número de booking o reserva.
     */
    @Column(length = 100)
    private String numeroBooking;

    /**
     * Bill of Lading (B/L) o Airway Bill (AWB).
     */
    @Column(length = 100)
    private String numeroBL;

    /**
     * Estado del envío.
     * Valores: CREADO, EN_PREPARACION, CARGADO, EN_TRANSITO, EN_PUERTO, DESPACHADO, CERRADO
     */
    @Column(nullable = false, length = 30)
    private String estado = "CREADO";

    /**
     * Peso neto total del envío (kg).
     */
    private Double pesoNetoTotal;

    /**
     * Peso bruto total del envío (kg).
     */
    private Double pesoBrutoTotal;

    /**
     * Número total de pallets en el envío.
     */
    private Integer numeroPallets;

    /**
     * Número total de cajas en el envío.
     */
    private Integer numeroCajas;

    /**
     * Observaciones generales del envío.
     */
    @Column(length = 1000)
    private String observaciones;

    /**
     * Cliente/importador final.
     */
    @Column(length = 200)
    private String clienteImportador;

    /**
     * Incoterm (ej: FOB, CIF, EXW).
     */
    @Column(length = 10)
    private String incoterm;

    /**
     * Pallets asociados a este envío.
     */
    @OneToMany(mappedBy = "envio", cascade = CascadeType.ALL)
    private List<Pallet> pallets = new ArrayList<>();

    /**
     * Eventos logísticos del envío (tracking).
     */
    @OneToMany(mappedBy = "envio", cascade = CascadeType.ALL)
    private List<EventoLogistico> eventos = new ArrayList<>();

    /**
     * Documentos de exportación asociados.
     */
    @OneToMany(mappedBy = "envio", cascade = CascadeType.ALL)
    private List<DocumentoExportacion> documentos = new ArrayList<>();

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

    /**
     * Hash SHA-256 generado al cerrar el envío (blockchain conceptual).
     */
    @Column(length = 64)
    private String hashCierre;

    /**
     * Fecha y hora del cierre del envío.
     */
    private LocalDateTime fechaCierre;

    /**
     * Usuario que cerró el envío.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierre_id")
    private User usuarioCierre;

    // Métodos de utilidad

    /**
     * Agrega un pallet al envío.
     */
    public void agregarPallet(Pallet pallet) {
        pallets.add(pallet);
        pallet.setEnvio(this);
    }

    /**
     * Agrega un evento logístico al envío.
     */
    public void agregarEvento(EventoLogistico evento) {
        eventos.add(evento);
        evento.setEnvio(this);
    }

    /**
     * Agrega un documento de exportación.
     */
    public void agregarDocumento(DocumentoExportacion documento) {
        documentos.add(documento);
        documento.setEnvio(this);
    }

    /**
     * Verifica si el envío está cerrado.
     */
    public boolean estaCerrado() {
        return "CERRADO".equals(estado);
    }

    /**
     * Verifica si el envío puede ser modificado.
     */
    public boolean esModificable() {
        return !estaCerrado() && activo;
    }
}