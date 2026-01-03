package com.frutas.trazabilidad.module.logistica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad que representa un evento logístico en el tracking de un envío.
 * Permite registrar el recorrido del envío desde la planta hasta su destino.
 */
@Entity
@Table(name = "eventos_logisticos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoLogistico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Envío al que pertenece este evento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    /**
     * Código único del evento (opcional).
     * Ejemplo: "EVT-2025-001"
     */
    @Column(length = 50)
    private String codigoEvento;

    /**
     * Tipo de evento logístico.
     * Valores: CARGA, SALIDA_PLANTA, ARRIBO_PUERTO, CONSOLIDACION, DESPACHO, ARRIBO_DESTINO
     */
    @Column(nullable = false, length = 30)
    private String tipoEvento;

    /**
     * Fecha del evento.
     */
    @Column(nullable = false)
    private LocalDate fechaEvento;

    /**
     * Hora del evento.
     */
    @Column(nullable = false)
    private LocalTime horaEvento;

    /**
     * Ubicación donde ocurrió el evento.
     * Ejemplo: "Planta Empaque Frutícola", "Puerto de Buenaventura"
     */
    @Column(nullable = false, length = 200)
    private String ubicacion;

    /**
     * Ciudad/región donde ocurrió el evento.
     */
    @Column(length = 100)
    private String ciudad;

    /**
     * País donde ocurrió el evento.
     */
    @Column(length = 100)
    private String pais;

    /**
     * Coordenadas GPS (latitud).
     */
    private Double latitud;

    /**
     * Coordenadas GPS (longitud).
     */
    private Double longitud;

    /**
     * Responsable del registro del evento.
     * Ejemplo: "Juan Pérez - Coordinador Logística"
     */
    @Column(nullable = false, length = 200)
    private String responsable;

    /**
     * Empresa/organización responsable (transportista, agente aduanal, etc.).
     */
    @Column(length = 200)
    private String organizacion;

    /**
     * Temperatura registrada en el evento (°C).
     * Útil para contenedores refrigerados.
     */
    private Double temperaturaRegistrada;

    /**
     * Humedad registrada (%).
     */
    private Double humedadRegistrada;

    /**
     * Placa del vehículo utilizado (si aplica).
     */
    @Column(length = 20)
    private String vehiculo;

    /**
     * Conductor del vehículo (si aplica).
     */
    @Column(length = 200)
    private String conductor;

    /**
     * Número de precinto/sello de seguridad.
     */
    @Column(length = 100)
    private String numeroPrecinto;

    /**
     * Observaciones del evento.
     */
    @Column(length = 1000)
    private String observaciones;

    /**
     * URL de fotos/evidencia del evento (opcional).
     */
    @Column(length = 500)
    private String urlEvidencia;

    /**
     * Indica si se detectó alguna incidencia.
     */
    @Column(nullable = false)
    private Boolean incidencia = false;

    /**
     * Descripción de la incidencia (si existe).
     */
    @Column(length = 1000)
    private String detalleIncidencia;

    /**
     * Indica si el registro está activo (soft delete).
     */
    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Métodos de utilidad

    /**
     * Retorna la fecha y hora del evento como LocalDateTime.
     */
    public LocalDateTime getFechaHoraEvento() {
        return LocalDateTime.of(fechaEvento, horaEvento);
    }

    /**
     * Verifica si el evento tiene incidencias.
     */
    public boolean tieneIncidencia() {
        return incidencia != null && incidencia;
    }
}