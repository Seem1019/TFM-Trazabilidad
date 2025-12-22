package com.frutas.trazabilidad.module.produccion.mapper;

import com.frutas.trazabilidad.module.produccion.dto.LoteRequest;
import com.frutas.trazabilidad.module.produccion.dto.LoteResponse;
import com.frutas.trazabilidad.module.produccion.entity.Lote;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversión entre Lote entity y DTOs.
 */
@Component
public class LoteMapper {

    /**
     * Convierte LoteRequest a Lote entity.
     */
    public Lote toEntity(LoteRequest request) {
        return Lote.builder()
                .codigoLote(request.getCodigoLote())
                .nombre(request.getNombre())
                .tipoFruta(request.getTipoFruta())
                .variedad(request.getVariedad())
                .areaHectareas(request.getAreaHectareas())
                .fechaSiembra(request.getFechaSiembra())
                .fechaPrimeraCosechaEstimada(request.getFechaPrimeraCosechaEstimada())
                .densidadSiembra(request.getDensidadSiembra())
                .ubicacionInterna(request.getUbicacionInterna())
                .observaciones(request.getObservaciones())
                .build();
    }

    /**
     * Convierte Lote entity a LoteResponse.
     */
    public LoteResponse toResponse(Lote lote) {
        return LoteResponse.builder()
                .id(lote.getId())
                .fincaId(lote.getFinca().getId())
                .fincaNombre(lote.getFinca().getNombre())
                .codigoLote(lote.getCodigoLote())
                .nombre(lote.getNombre())
                .tipoFruta(lote.getTipoFruta())
                .variedad(lote.getVariedad())
                .areaHectareas(lote.getAreaHectareas())
                .fechaSiembra(lote.getFechaSiembra())
                .fechaPrimeraCosechaEstimada(lote.getFechaPrimeraCosechaEstimada())
                .densidadSiembra(lote.getDensidadSiembra())
                .ubicacionInterna(lote.getUbicacionInterna())
                .estadoLote(lote.getEstadoLote())
                .observaciones(lote.getObservaciones())
                .activo(lote.getActivo())
                .createdAt(lote.getCreatedAt())
                .updatedAt(lote.getUpdatedAt())
                .build();
    }

    /**
     * Actualiza una entidad Lote desde un LoteRequest.
     */
    public void updateEntityFromRequest(LoteRequest request, Lote lote) {
        lote.setCodigoLote(request.getCodigoLote());
        lote.setNombre(request.getNombre());
        lote.setTipoFruta(request.getTipoFruta());
        lote.setVariedad(request.getVariedad());
        lote.setAreaHectareas(request.getAreaHectareas());
        lote.setFechaSiembra(request.getFechaSiembra());
        lote.setFechaPrimeraCosechaEstimada(request.getFechaPrimeraCosechaEstimada());
        lote.setDensidadSiembra(request.getDensidadSiembra());
        lote.setUbicacionInterna(request.getUbicacionInterna());
        lote.setObservaciones(request.getObservaciones());
    }
}