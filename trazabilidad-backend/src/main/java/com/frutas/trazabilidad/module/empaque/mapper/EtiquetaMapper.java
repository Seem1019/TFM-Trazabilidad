package com.frutas.trazabilidad.module.empaque.mapper;

import com.frutas.trazabilidad.module.empaque.dto.EtiquetaRequest;
import com.frutas.trazabilidad.module.empaque.dto.EtiquetaResponse;
import com.frutas.trazabilidad.module.empaque.entity.Clasificacion;
import com.frutas.trazabilidad.module.empaque.entity.Etiqueta;
import org.springframework.stereotype.Component;

/**
 * Mapper para Etiqueta.
 */
@Component
public class EtiquetaMapper {

    public Etiqueta toEntity(EtiquetaRequest request, Clasificacion clasificacion) {
        return Etiqueta.builder()
                .clasificacion(clasificacion)
                .codigoEtiqueta(request.getCodigoEtiqueta())
                .tipoEtiqueta(request.getTipoEtiqueta())
                .cantidadContenida(request.getCantidadContenida())
                .unidadMedida(request.getUnidadMedida())
                .pesoNeto(request.getPesoNeto())
                .pesoBruto(request.getPesoBruto())
                .numeroCajas(request.getNumeroCajas())
                .observaciones(request.getObservaciones())
                .estadoEtiqueta("DISPONIBLE")
                .activo(true)
                .build();
    }

    public EtiquetaResponse toResponse(Etiqueta entity) {
        return EtiquetaResponse.builder()
                .id(entity.getId())
                .clasificacionId(entity.getClasificacion().getId())
                .clasificacionCodigo(entity.getClasificacion().getCodigoClasificacion())
                .calidad(entity.getClasificacion().getCalidad())
                .codigoEtiqueta(entity.getCodigoEtiqueta())
                .codigoQr(entity.getCodigoQr())
                .tipoEtiqueta(entity.getTipoEtiqueta())
                .cantidadContenida(entity.getCantidadContenida())
                .unidadMedida(entity.getUnidadMedida())
                .pesoNeto(entity.getPesoNeto())
                .pesoBruto(entity.getPesoBruto())
                .numeroCajas(entity.getNumeroCajas())
                .estadoEtiqueta(entity.getEstadoEtiqueta())
                .urlQr(entity.getUrlQr())
                .observaciones(entity.getObservaciones())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .loteOrigen(entity.getClasificacion().getRecepcion().getLote().getCodigoLote())
                .fincaOrigen(entity.getClasificacion().getRecepcion().getLote().getFinca().getNombre())
                .build();
    }

    public void updateEntityFromRequest(Etiqueta entity, EtiquetaRequest request, Clasificacion clasificacion) {
        entity.setClasificacion(clasificacion);
        entity.setCodigoEtiqueta(request.getCodigoEtiqueta());
        entity.setTipoEtiqueta(request.getTipoEtiqueta());
        entity.setCantidadContenida(request.getCantidadContenida());
        entity.setUnidadMedida(request.getUnidadMedida());
        entity.setPesoNeto(request.getPesoNeto());
        entity.setPesoBruto(request.getPesoBruto());
        entity.setNumeroCajas(request.getNumeroCajas());
        entity.setObservaciones(request.getObservaciones());
    }
}