package com.frutas.trazabilidad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para trazabilidad completa INTERNA (con datos sensibles).
 * Solo accesible para usuarios autenticados de la empresa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrazabilidadCompletaDTO {

    // Información de la etiqueta
    private Long etiquetaId;
    private String codigoEtiqueta;
    private String codigoQr;
    private String tipoEtiqueta;
    private String estadoEtiqueta;
    private String urlQr;

    // Información de origen (Finca y Lote)
    private OrigenInfo origen;

    // Información de producción
    private ProduccionInfo produccion;

    // Información de empaque
    private EmpaqueInfo empaque;

    // Información de logística (puede ser null si no está en envío)
    private LogisticaInfo logistica;

    // Certificaciones
    private List<CertificacionCompleta> certificaciones;

    // Auditoría y trazabilidad
    private AuditoriaInfo auditoria;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrigenInfo {
        private Long fincaId;
        private String fincaNombre;
        private String fincaCodigo;
        private String municipio;
        private String departamento;
        private String pais;
        private Double areaTotal;
        private String contactoResponsable;
        private String telefonoContacto;
        private String emailContacto;

        private Long loteId;
        private String codigoLote;
        private String nombreLote;
        private String tipoFruta;
        private String variedad;
        private Double areaHectareas;
        private LocalDate fechaSiembra;
        private String estadoLote;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProduccionInfo {
        private Long cosechaId;
        private String codigoCosecha;
        private LocalDate fechaCosecha;
        private Double cantidadCosechada;
        private String unidadMedida;
        private String estadoFruta;
        private String responsableCosecha;

        private List<ActividadAgronomicaInfo> actividades;
        private Integer totalActividades;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActividadAgronomicaInfo {
        private Long id;
        private String tipoActividad;
        private LocalDate fechaActividad;
        private String descripcion;
        private String responsable;
        private String productosAplicados;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpaqueInfo {
        private Long recepcionId;
        private String codigoRecepcion;
        private LocalDate fechaRecepcion;
        private Double cantidadRecibida;
        private String estadoRecepcion;
        private String responsableRecepcion;

        private Long clasificacionId;
        private String codigoClasificacion;
        private LocalDate fechaClasificacion;
        private String calidad;
        private String calibre;
        private Double cantidadClasificada;
        private String responsableClasificacion;

        private List<ControlCalidadInfo> controlesCalidad;

        private Long palletId;
        private String codigoPallet;
        private String tipoPallet;
        private Integer numeroCajas;
        private Double pesoNeto;
        private Double pesoBruto;
        private String estadoPallet;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ControlCalidadInfo {
        private Long id;
        private LocalDate fechaControl;
        private String tipoControl;
        private String resultado;
        private String observaciones;
        private String inspector;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogisticaInfo {
        private Long envioId;
        private String codigoEnvio;
        private LocalDate fechaCreacion;
        private LocalDate fechaSalidaEstimada;
        private LocalDate fechaSalidaReal;
        private String estadoEnvio;

        private String exportador;
        private String paisDestino;
        private String puertoDestino;
        private String ciudadDestino;
        private String tipoTransporte;
        private String transportista;

        private String codigoContenedor;
        private String tipoContenedor;
        private Double temperaturaContenedor;
        private String numeroBooking;
        private String numeroBL;

        private Integer totalPallets;
        private Integer totalCajas;
        private Double pesoNetoTotal;
        private Double pesoBrutoTotal;

        private String clienteImportador;
        private String incoterm;

        private List<EventoLogisticoInfo> eventos;
        private List<DocumentoInfo> documentos;

        // Datos de cierre (blockchain)
        private Boolean cerrado;
        private String hashCierre;
        private LocalDateTime fechaCierre;
        private String usuarioCierre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventoLogisticoInfo {
        private Long id;
        private String tipoEvento;
        private LocalDateTime fechaEvento;
        private String ubicacion;
        private String ciudad;
        private String pais;
        private String descripcion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentoInfo {
        private Long id;
        private String tipoDocumento;
        private String numeroDocumento;
        private LocalDate fechaEmision;
        private String entidadEmisora;
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificacionCompleta {
        private Long id;
        private String tipoCertificacion;
        private String numeroCertificado;
        private String entidadEmisora;
        private LocalDate fechaEmision;
        private LocalDate fechaVencimiento;
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditoriaInfo {
        private LocalDateTime fechaCreacionEtiqueta;
        private LocalDateTime fechaUltimaActualizacion;
        private String creadoPor;
        private Long empresaId;
        private String empresaNombre;
        private Integer totalEventosAuditoria;
    }
}
