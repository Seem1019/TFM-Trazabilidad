package com.frutas.trazabilidad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para consulta pública de trazabilidad mediante QR.
 * Contiene información básica sin datos sensibles para consumidores finales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrazabilidadPublicaDTO {

    // Información de la etiqueta
    private String codigoEtiqueta;
    private String tipoProducto;
    private String calidad;
    private String variedad;

    // Origen (Finca y Lote)
    private OrigenInfo origen;

    // Producción
    private ProduccionInfo produccion;

    // Empaque
    private EmpaqueInfo empaque;

    // Logística (si está disponible)
    private LogisticaInfo logistica;

    // Certificaciones de la finca
    private List<CertificacionPublica> certificaciones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrigenInfo {
        private String finca;
        private String municipio;
        private String departamento;
        private String pais;
        private String codigoLote;
        private String nombreLote;
        private Double areaHectareas;
        private LocalDate fechaSiembra;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProduccionInfo {
        private LocalDate fechaCosecha;
        private String estadoFruta;
        private Integer actividadesRegistradas; // Número de actividades sin detalles sensibles
        private List<String> tiposActividades; // Solo tipos: FERTILIZACIÓN, RIEGO, etc.
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpaqueInfo {
        private LocalDate fechaRecepcion;
        private LocalDate fechaClasificacion;
        private String calidadClasificada;
        private String calibre;
        private Boolean controlesCalidadAprobados;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogisticaInfo {
        private String estadoEnvio;
        private String paisDestino;
        private String puertoDestino;
        private LocalDate fechaSalidaEstimada;
        private String tipoTransporte;
        private List<EventoLogisticoPublico> eventos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventoLogisticoPublico {
        private String tipoEvento;
        private LocalDate fechaEvento;
        private String ubicacion;
        private String ciudad;
        private String pais;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificacionPublica {
        private String tipoCertificacion;
        private String entidadEmisora;
        private String estado; // VIGENTE, VENCIDA
    }
}
