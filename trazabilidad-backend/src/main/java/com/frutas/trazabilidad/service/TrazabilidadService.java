package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.dto.TrazabilidadPublicaDTO;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.empaque.entity.*;
import com.frutas.trazabilidad.module.empaque.repository.ControlCalidadRepository;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaRepository;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaPalletRepository;
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
}
