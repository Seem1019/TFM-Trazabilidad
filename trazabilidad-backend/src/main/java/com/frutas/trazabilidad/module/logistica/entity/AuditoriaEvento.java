package com.frutas.trazabilidad.module.logistica.entity;

import com.frutas.trazabilidad.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que registra eventos de auditoría del sistema.
 * Almacena un log inmutable de operaciones críticas para trazabilidad y cumplimiento.
 * Implementa "blockchain conceptual" mediante hash SHA-256.
 */
@Entity
@Table(name = "auditoria_eventos", indexes = {
        @Index(name = "idx_auditoria_entidad", columnList = "tipoEntidad,entidadId"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario_id"),
        @Index(name = "idx_auditoria_fecha", columnList = "fechaEvento")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que ejecutó la operación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    /**
     * Tipo de entidad afectada.
     * Valores: FINCA, LOTE, COSECHA, RECEPCION, CLASIFICACION, ETIQUETA,
     *          PALLET, ENVIO, EVENTO_LOGISTICO, DOCUMENTO
     */
    @Column(nullable = false, length = 50)
    private String tipoEntidad;

    /**
     * ID de la entidad afectada.
     */
    @Column(nullable = false)
    private Long entidadId;

    /**
     * Código de la entidad afectada (para referencia legible).
     * Ejemplo: "LOTE-AGU-001", "ENV-2025-001"
     */
    @Column(length = 100)
    private String codigoEntidad;

    /**
     * Tipo de operación realizada.
     * Valores: CREATE, UPDATE, DELETE, CLOSE, APPROVE, REJECT, ASSIGN
     */
    @Column(nullable = false, length = 20)
    private String tipoOperacion;

    /**
     * Descripción legible de la operación.
     * Ejemplo: "Cierre de envío ENV-2025-001", "Asignación de pallet PLT-001 a envío"
     */
    @Column(nullable = false, length = 500)
    private String descripcionOperacion;

    /**
     * Datos anteriores de la entidad (JSON).
     * Permite reconstruir el estado antes de la modificación.
     */
    @Column(columnDefinition = "TEXT")
    private String datosAnteriores;

    /**
     * Datos nuevos de la entidad (JSON).
     * Permite ver el estado después de la modificación.
     */
    @Column(columnDefinition = "TEXT")
    private String datosNuevos;

    /**
     * Campos modificados (lista separada por comas).
     * Ejemplo: "estado,fechaCierre,hashCierre"
     */
    @Column(length = 500)
    private String camposModificados;

    /**
     * Hash SHA-256 del evento de auditoría.
     * Se calcula con: hash_anterior + usuario + timestamp + entidad + operacion + datos
     * Esto crea una cadena de bloques conceptual donde cada registro depende del anterior.
     */
    @Column(nullable = false, length = 64)
    private String hashEvento;

    /**
     * Hash del evento de auditoría anterior (para crear la cadena).
     */
    @Column(length = 64)
    private String hashAnterior;

    /**
     * IP desde donde se realizó la operación.
     */
    @Column(length = 50)
    private String ipOrigen;

    /**
     * User Agent del navegador/cliente.
     */
    @Column(length = 500)
    private String userAgent;

    /**
     * Empresa a la que pertenece el usuario (multiempresa).
     */
    @Column(nullable = false)
    private Long empresaId;

    /**
     * Nombre de la empresa (desnormalizado para consultas rápidas).
     */
    @Column(length = 200)
    private String empresaNombre;

    /**
     * Módulo del sistema donde se ejecutó la operación.
     * Valores: PRODUCCION, EMPAQUE, LOGISTICA, ADMINISTRACION
     */
    @Column(nullable = false, length = 30)
    private String modulo;

    /**
     * Nivel de criticidad del evento.
     * Valores: INFO, WARNING, CRITICAL
     */
    @Column(nullable = false, length = 10)
    private String nivelCriticidad = "INFO";

    /**
     * Indica si este registro es parte de la cadena blockchain.
     * Solo eventos críticos (cierres, aprobaciones) se encadenan.
     */
    @Column(nullable = false)
    private Boolean enCadena = false;

    /**
     * Fecha y hora del evento (inmutable).
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEvento;

    // Métodos de utilidad

    /**
     * Verifica la integridad del hash comparándolo con el calculado.
     */
    public boolean verificarIntegridad(String hashCalculado) {
        return this.hashEvento != null && this.hashEvento.equals(hashCalculado);
    }

    /**
     * Verifica si este evento está encadenado con el anterior.
     */
    public boolean estaEncadenado() {
        return enCadena != null && enCadena;
    }

    /**
     * Verifica si es un evento crítico.
     */
    public boolean esCritico() {
        return "CRITICAL".equals(nivelCriticidad);
    }
}