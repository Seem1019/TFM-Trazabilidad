package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.dto.TrazabilidadCompletaDTO;
import com.frutas.trazabilidad.dto.TrazabilidadPublicaDTO;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.empaque.entity.*;
import com.frutas.trazabilidad.module.empaque.repository.ControlCalidadRepository;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaRepository;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaPalletRepository;
import com.frutas.trazabilidad.module.logistica.entity.DocumentoExportacion;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.entity.EventoLogistico;
import com.frutas.trazabilidad.module.produccion.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para construir trazabilidad completa del producto.
 * Desde finca hasta destino final.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrazabilidadService {

    private final EtiquetaRepository etiquetaRepository;
    private final ControlCalidadRepository controlCalidadRepository;
    private final EtiquetaPalletRepository etiquetaPalletRepository;

    /**
     * Obtiene la trazabilidad pública completa a partir del código QR.
     * Filtra datos sensibles y construye el árbol completo.
     */
    @Transactional(readOnly = true)
    public TrazabilidadPublicaDTO obtenerTrazabilidadPublica(String codigoQr) {
        log.info("Consultando trazabilidad pública para código QR: {}", codigoQr);

        // Buscar etiqueta por código QR
        Etiqueta etiqueta = etiquetaRepository.findByCodigoQr(codigoQr)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró información de trazabilidad para el código QR proporcionado"));

        // Obtener toda la cadena de trazabilidad
        Clasificacion clasificacion = etiqueta.getClasificacion();
        RecepcionPlanta recepcion = clasificacion.getRecepcion();
        Lote lote = recepcion.getLote();
        Finca finca = lote.getFinca();

        // Construir DTO público
        return TrazabilidadPublicaDTO.builder()
                .codigoEtiqueta(etiqueta.getCodigoEtiqueta())
                .tipoProducto(lote.getTipoFruta())
                .calidad(clasificacion.getCalidad())
                .variedad(lote.getVariedad())
                .origen(construirOrigenInfo(finca, lote))
                .produccion(construirProduccionInfo(lote))
                .empaque(construirEmpaqueInfo(recepcion, clasificacion))
                .logistica(construirLogisticaInfo(etiqueta))
                .certificaciones(construirCertificaciones(finca))
                .build();
    }

    private TrazabilidadPublicaDTO.OrigenInfo construirOrigenInfo(Finca finca, Lote lote) {
        return TrazabilidadPublicaDTO.OrigenInfo.builder()
                .finca(finca.getNombre())
                .municipio(finca.getMunicipio())
                .departamento(finca.getDepartamento())
                .pais(finca.getPais())
                .codigoLote(lote.getCodigoLote())
                .nombreLote(lote.getNombre())
                .areaHectareas(lote.getAreaHectareas())
                .fechaSiembra(lote.getFechaSiembra())
                .build();
    }

    private TrazabilidadPublicaDTO.ProduccionInfo construirProduccionInfo(Lote lote) {
        // Obtener la cosecha más reciente
        Cosecha cosechaReciente = lote.getCosechas().stream()
                .filter(c -> c.getActivo())
                .max((c1, c2) -> c1.getFechaCosecha().compareTo(c2.getFechaCosecha()))
                .orElse(null);

        // Tipos de actividades realizadas (sin detalles sensibles como productos)
        List<String> tiposActividades = lote.getActividades().stream()
                .filter(a -> a.getActivo())
                .map(ActividadAgronomica::getTipoActividad)
                .distinct()
                .collect(Collectors.toList());

        return TrazabilidadPublicaDTO.ProduccionInfo.builder()
                .fechaCosecha(cosechaReciente != null ? cosechaReciente.getFechaCosecha() : null)
                .estadoFruta(cosechaReciente != null ? cosechaReciente.getEstadoFruta() : null)
                .actividadesRegistradas(lote.getActividades().size())
                .tiposActividades(tiposActividades)
                .build();
    }

    private TrazabilidadPublicaDTO.EmpaqueInfo construirEmpaqueInfo(
            RecepcionPlanta recepcion,
            Clasificacion clasificacion) {

        // Buscar controles de calidad por clasificación
        List<ControlCalidad> controles = controlCalidadRepository
                .findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(clasificacion.getId());

        Boolean controlesAprobados = controles.stream()
                .anyMatch(cc -> "APROBADO".equals(cc.getResultado()));

        return TrazabilidadPublicaDTO.EmpaqueInfo.builder()
                .fechaRecepcion(recepcion.getFechaRecepcion())
                .fechaClasificacion(clasificacion.getFechaClasificacion())
                .calidadClasificada(clasificacion.getCalidad())
                .calibre(clasificacion.getCalibre())
                .controlesCalidadAprobados(controlesAprobados)
                .build();
    }

    private TrazabilidadPublicaDTO.LogisticaInfo construirLogisticaInfo(Etiqueta etiqueta) {
        // Buscar si la etiqueta está asignada a un pallet
        var etiquetaPallet = etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(etiqueta.getId());

        if (etiquetaPallet.isEmpty()) {
            // No hay información de logística aún
            return null;
        }

        Pallet pallet = etiquetaPallet.get().getPallet();

        if (pallet.getEnvio() == null) {
            // El pallet aún no está asignado a un envío
            return null;
        }

        var envio = pallet.getEnvio();

        // Eventos logísticos públicos (sin datos sensibles)
        List<TrazabilidadPublicaDTO.EventoLogisticoPublico> eventos = envio.getEventos().stream()
                .filter(e -> e.getActivo())
                .map(this::convertirEventoAPublico)
                .collect(Collectors.toList());

        return TrazabilidadPublicaDTO.LogisticaInfo.builder()
                .estadoEnvio(envio.getEstado())
                .paisDestino(envio.getPaisDestino())
                .puertoDestino(envio.getPuertoDestino())
                .fechaSalidaEstimada(envio.getFechaSalidaEstimada())
                .tipoTransporte(envio.getTipoTransporte())
                .eventos(eventos)
                .build();
    }

    private TrazabilidadPublicaDTO.EventoLogisticoPublico convertirEventoAPublico(EventoLogistico evento) {
        return TrazabilidadPublicaDTO.EventoLogisticoPublico.builder()
                .tipoEvento(evento.getTipoEvento())
                .fechaEvento(evento.getFechaEvento())
                .ubicacion(evento.getUbicacion())
                .ciudad(evento.getCiudad())
                .pais(evento.getPais())
                .build();
    }

    private List<TrazabilidadPublicaDTO.CertificacionPublica> construirCertificaciones(Finca finca) {
        return finca.getCertificaciones().stream()
                .filter(c -> c.getActivo())
                .map(cert -> TrazabilidadPublicaDTO.CertificacionPublica.builder()
                        .tipoCertificacion(cert.getTipoCertificacion())
                        .entidadEmisora(cert.getEntidadEmisora())
                        .estado(cert.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la trazabilidad completa INTERNA con todos los datos (solo usuarios autenticados).
     */
    @Transactional(readOnly = true)
    public TrazabilidadCompletaDTO obtenerTrazabilidadCompleta(Long etiquetaId, Long empresaId) {
        log.info("Consultando trazabilidad completa interna para etiqueta ID: {}", etiquetaId);

        // Buscar etiqueta y validar pertenencia a empresa
        Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con ID: " + etiquetaId));

        // Validar que la etiqueta pertenece a la empresa del usuario
        Long etiquetaEmpresaId = etiqueta.getClasificacion().getRecepcion().getLote().getFinca().getEmpresa().getId();
        if (!etiquetaEmpresaId.equals(empresaId)) {
            throw new IllegalArgumentException("No tiene permisos para consultar esta etiqueta");
        }

        // Obtener toda la cadena de trazabilidad
        Clasificacion clasificacion = etiqueta.getClasificacion();
        RecepcionPlanta recepcion = clasificacion.getRecepcion();
        Lote lote = recepcion.getLote();
        Finca finca = lote.getFinca();

        // Construir DTO completo con todos los datos
        return TrazabilidadCompletaDTO.builder()
                .etiquetaId(etiqueta.getId())
                .codigoEtiqueta(etiqueta.getCodigoEtiqueta())
                .codigoQr(etiqueta.getCodigoQr())
                .tipoEtiqueta(etiqueta.getTipoEtiqueta())
                .estadoEtiqueta(etiqueta.getEstadoEtiqueta())
                .urlQr(etiqueta.getUrlQr())
                .origen(construirOrigenCompleto(finca, lote))
                .produccion(construirProduccionCompleta(lote))
                .empaque(construirEmpaqueCompleto(recepcion, clasificacion))
                .logistica(construirLogisticaCompleta(etiqueta))
                .certificaciones(construirCertificacionesCompletas(finca))
                .auditoria(construirAuditoriaInfo(etiqueta, finca))
                .build();
    }

    private TrazabilidadCompletaDTO.OrigenInfo construirOrigenCompleto(Finca finca, Lote lote) {
        return TrazabilidadCompletaDTO.OrigenInfo.builder()
                .fincaId(finca.getId())
                .fincaNombre(finca.getNombre())
                .fincaCodigo(finca.getCodigoFinca())
                .municipio(finca.getMunicipio())
                .departamento(finca.getDepartamento())
                .pais(finca.getPais())
                .areaTotal(finca.getAreaHectareas())
                .contactoResponsable(finca.getEncargado())
                .telefonoContacto(finca.getTelefono())
                .emailContacto(finca.getEmail())
                .loteId(lote.getId())
                .codigoLote(lote.getCodigoLote())
                .nombreLote(lote.getNombre())
                .tipoFruta(lote.getTipoFruta())
                .variedad(lote.getVariedad())
                .areaHectareas(lote.getAreaHectareas())
                .fechaSiembra(lote.getFechaSiembra())
                .estadoLote(lote.getEstadoLote())
                .build();
    }

    private TrazabilidadCompletaDTO.ProduccionInfo construirProduccionCompleta(Lote lote) {
        // Obtener la cosecha más reciente
        Cosecha cosechaReciente = lote.getCosechas().stream()
                .filter(Cosecha::getActivo)
                .max((c1, c2) -> c1.getFechaCosecha().compareTo(c2.getFechaCosecha()))
                .orElse(null);

        // Actividades agronómicas
        List<TrazabilidadCompletaDTO.ActividadAgronomicaInfo> actividades = lote.getActividades().stream()
                .filter(ActividadAgronomica::getActivo)
                .map(a -> TrazabilidadCompletaDTO.ActividadAgronomicaInfo.builder()
                        .id(a.getId())
                        .tipoActividad(a.getTipoActividad())
                        .fechaActividad(a.getFechaActividad())
                        .descripcion(a.getObservaciones())
                        .responsable(a.getResponsable())
                        .productosAplicados(a.getProductoAplicado())
                        .build())
                .collect(Collectors.toList());

        return TrazabilidadCompletaDTO.ProduccionInfo.builder()
                .cosechaId(cosechaReciente != null ? cosechaReciente.getId() : null)
                .codigoCosecha(cosechaReciente != null ? cosechaReciente.getLote().getCodigoLote() : null)
                .fechaCosecha(cosechaReciente != null ? cosechaReciente.getFechaCosecha() : null)
                .cantidadCosechada(cosechaReciente != null ? cosechaReciente.getCantidadCosechada() : null)
                .unidadMedida(cosechaReciente != null ? cosechaReciente.getUnidadMedida() : null)
                .estadoFruta(cosechaReciente != null ? cosechaReciente.getEstadoFruta() : null)
                .responsableCosecha(cosechaReciente != null ? cosechaReciente.getResponsableCosecha() : null)
                .actividades(actividades)
                .totalActividades(actividades.size())
                .build();
    }

    private TrazabilidadCompletaDTO.EmpaqueInfo construirEmpaqueCompleto(
            RecepcionPlanta recepcion, Clasificacion clasificacion) {

        // Controles de calidad
        List<ControlCalidad> controles = controlCalidadRepository
                .findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(clasificacion.getId());

        List<TrazabilidadCompletaDTO.ControlCalidadInfo> controlesInfo = controles.stream()
                .map(cc -> TrazabilidadCompletaDTO.ControlCalidadInfo.builder()
                        .id(cc.getId())
                        .fechaControl(cc.getFechaControl())
                        .tipoControl(cc.getTipoControl())
                        .resultado(cc.getResultado())
                        .observaciones(cc.getObservaciones())
                        .inspector(cc.getLaboratorio())
                        .build())
                .collect(Collectors.toList());

        // Buscar pallet asociado
        var etiquetaPallet = etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(clasificacion.getId());
        Pallet pallet = etiquetaPallet.map(EtiquetaPallet::getPallet).orElse(null);

        return TrazabilidadCompletaDTO.EmpaqueInfo.builder()
                .recepcionId(recepcion.getId())
                .codigoRecepcion(recepcion.getCodigoRecepcion())
                .fechaRecepcion(recepcion.getFechaRecepcion())
                .cantidadRecibida(recepcion.getCantidadRecibida())
                .estadoRecepcion(recepcion.getEstadoRecepcion())
                .responsableRecepcion(recepcion.getResponsableRecepcion())
                .clasificacionId(clasificacion.getId())
                .codigoClasificacion(clasificacion.getCodigoClasificacion())
                .fechaClasificacion(clasificacion.getFechaClasificacion())
                .calidad(clasificacion.getCalidad())
                .calibre(clasificacion.getCalibre())
                .cantidadClasificada(clasificacion.getCantidadClasificada())
                .responsableClasificacion(clasificacion.getResponsableClasificacion())
                .controlesCalidad(controlesInfo)
                .palletId(pallet != null ? pallet.getId() : null)
                .codigoPallet(pallet != null ? pallet.getCodigoPallet() : null)
                .tipoPallet(pallet != null ? pallet.getTipoPallet() : null)
                .numeroCajas(pallet != null ? pallet.getNumeroCajas() : null)
                .pesoNeto(pallet != null ? pallet.getPesoNetoTotal() : null)
                .pesoBruto(pallet != null ? pallet.getPesoBrutoTotal() : null)
                .estadoPallet(pallet != null ? pallet.getEstadoPallet() : null)
                .build();
    }

    private TrazabilidadCompletaDTO.LogisticaInfo construirLogisticaCompleta(Etiqueta etiqueta) {
        // Buscar si la etiqueta está asignada a un pallet
        var etiquetaPallet = etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(etiqueta.getId());

        if (etiquetaPallet.isEmpty()) {
            return null;
        }

        Pallet pallet = etiquetaPallet.get().getPallet();
        if (pallet.getEnvio() == null) {
            return null;
        }

        Envio envio = pallet.getEnvio();

        // Eventos logísticos
        List<TrazabilidadCompletaDTO.EventoLogisticoInfo> eventos = envio.getEventos().stream()
                .filter(e -> e.getActivo())
                .map(e -> TrazabilidadCompletaDTO.EventoLogisticoInfo.builder()
                        .id(e.getId())
                        .tipoEvento(e.getTipoEvento())
                        .fechaEvento(e.getFechaEvento().atStartOfDay())
                        .ubicacion(e.getUbicacion())
                        .ciudad(e.getCiudad())
                        .pais(e.getPais())
                        .descripcion(e.getObservaciones())
                        .build())
                .collect(Collectors.toList());

        // Documentos
        List<TrazabilidadCompletaDTO.DocumentoInfo> documentos = envio.getDocumentos().stream()
                .filter(d -> d.getActivo())
                .map(d -> TrazabilidadCompletaDTO.DocumentoInfo.builder()
                        .id(d.getId())
                        .tipoDocumento(d.getTipoDocumento())
                        .numeroDocumento(d.getNumeroDocumento())
                        .fechaEmision(d.getFechaEmision())
                        .entidadEmisora(d.getEntidadEmisora())
                        .estado(d.getEstado())
                        .build())
                .collect(Collectors.toList());

        return TrazabilidadCompletaDTO.LogisticaInfo.builder()
                .envioId(envio.getId())
                .codigoEnvio(envio.getCodigoEnvio())
                .fechaCreacion(envio.getFechaCreacion())
                .fechaSalidaEstimada(envio.getFechaSalidaEstimada())
                .fechaSalidaReal(envio.getFechaSalidaReal())
                .estadoEnvio(envio.getEstado())
                .exportador(envio.getExportador())
                .paisDestino(envio.getPaisDestino())
                .puertoDestino(envio.getPuertoDestino())
                .ciudadDestino(envio.getCiudadDestino())
                .tipoTransporte(envio.getTipoTransporte())
                .transportista(envio.getTransportista())
                .codigoContenedor(envio.getCodigoContenedor())
                .tipoContenedor(envio.getTipoContenedor())
                .temperaturaContenedor(envio.getTemperaturaContenedor())
                .numeroBooking(envio.getNumeroBooking())
                .numeroBL(envio.getNumeroBL())
                .totalPallets(envio.getNumeroPallets())
                .totalCajas(envio.getNumeroCajas())
                .pesoNetoTotal(envio.getPesoNetoTotal())
                .pesoBrutoTotal(envio.getPesoBrutoTotal())
                .clienteImportador(envio.getClienteImportador())
                .incoterm(envio.getIncoterm())
                .eventos(eventos)
                .documentos(documentos)
                .cerrado(envio.estaCerrado())
                .hashCierre(envio.getHashCierre())
                .fechaCierre(envio.getFechaCierre())
                .usuarioCierre(envio.getUsuarioCierre() != null ? envio.getUsuarioCierre().getEmail() : null)
                .build();
    }

    private List<TrazabilidadCompletaDTO.CertificacionCompleta> construirCertificacionesCompletas(Finca finca) {
        return finca.getCertificaciones().stream()
                .filter(c -> c.getActivo())
                .map(cert -> TrazabilidadCompletaDTO.CertificacionCompleta.builder()
                        .id(cert.getId())
                        .tipoCertificacion(cert.getTipoCertificacion())
                        .numeroCertificado(cert.getNumeroCertificado())
                        .entidadEmisora(cert.getEntidadEmisora())
                        .fechaEmision(cert.getFechaEmision())
                        .fechaVencimiento(cert.getFechaVencimiento())
                        .estado(cert.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    private TrazabilidadCompletaDTO.AuditoriaInfo construirAuditoriaInfo(Etiqueta etiqueta, Finca finca) {
        return TrazabilidadCompletaDTO.AuditoriaInfo.builder()
                .fechaCreacionEtiqueta(etiqueta.getCreatedAt())
                .fechaUltimaActualizacion(etiqueta.getUpdatedAt())
                .creadoPor(etiqueta.getClasificacion().getRecepcion().getLote().getFinca().getEmpresa().getRazonSocial())
                .empresaId(finca.getEmpresa().getId())
                .empresaNombre(finca.getEmpresa().getRazonSocial())
                .totalEventosAuditoria(0) // Podría calcularse consultando AuditoriaEvento
                .build();
    }
}
