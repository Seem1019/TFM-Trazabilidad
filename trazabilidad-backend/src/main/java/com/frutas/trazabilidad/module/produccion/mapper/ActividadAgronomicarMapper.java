package com.frutas.trazabilidad.module.produccion.mapper;

import com.frutas.trazabilidad.module.produccion.dto.ActividadAgronomicarRequest;
import com.frutas.trazabilidad.module.produccion.dto.ActividadAgronomicarResponse;
import com.frutas.trazabilidad.module.produccion.entity.ActividadAgronomica;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversión entre ActividadAgronomica entity y DTOs.
 */
@Component
public class ActividadAgronomicarMapper {

    /**
     * Convierte ActividadAgronomicarRequest a ActividadAgronomica entity.
     */
    public ActividadAgronomica toEntity(ActividadAgronomicarRequest request) {
        return ActividadAgronomica.builder()
                .tipoActividad(request.getTipoActividad())
                .fechaActividad(request.getFechaActividad())
                .productoAplicado(request.getProductoAplicado())
                .dosisoCantidad(request.getDosisoCantidad())
                .unidadMedida(request.getUnidadMedida())
                .metodoAplicacion(request.getMetodoAplicacion())
                .responsable(request.getResponsable())
                .numeroRegistroProducto(request.getNumeroRegistroProducto())
                .intervaloSeguridadDias(request.getIntervaloSeguridadDias())
                .observaciones(request.getObservaciones())
                .build();
    }

    /**
     * Convierte ActividadAgronomica entity a ActividadAgronomicarResponse.
     */
    public ActividadAgronomicarResponse toResponse(ActividadAgronomica actividad) {
        return ActividadAgronomicarResponse.builder()
                .id(actividad.getId())
                .loteId(actividad.getLote().getId())
                .loteNombre(actividad.getLote().getNombre())
                .loteCodigoLote(actividad.getLote().getCodigoLote())
                .tipoActividad(actividad.getTipoActividad())
                .fechaActividad(actividad.getFechaActividad())
                .productoAplicado(actividad.getProductoAplicado())
                .dosisoCantidad(actividad.getDosisoCantidad())
                .unidadMedida(actividad.getUnidadMedida())
                .metodoAplicacion(actividad.getMetodoAplicacion())
                .responsable(actividad.getResponsable())
                .numeroRegistroProducto(actividad.getNumeroRegistroProducto())
                .intervaloSeguridadDias(actividad.getIntervaloSeguridadDias())
                .observaciones(actividad.getObservaciones())
                .activo(actividad.getActivo())
                .createdAt(actividad.getCreatedAt())
                .updatedAt(actividad.getUpdatedAt())
                .fechaFinIntervaloSeguridad(actividad.getFechaFinIntervaloSeguridad())
                .intervaloSeguridadCumplido(actividad.isIntervaloSeguridadCumplido())
                .build();
    }

    /**
     * Actualiza una entidad ActividadAgronomica desde un ActividadAgronomicarRequest.
     */
    public void updateEntityFromRequest(ActividadAgronomicarRequest request, ActividadAgronomica actividad) {
        actividad.setTipoActividad(request.getTipoActividad());
        actividad.setFechaActividad(request.getFechaActividad());
        actividad.setProductoAplicado(request.getProductoAplicado());
        actividad.setDosisoCantidad(request.getDosisoCantidad());
        actividad.setUnidadMedida(request.getUnidadMedida());
        actividad.setMetodoAplicacion(request.getMetodoAplicacion());
        actividad.setResponsable(request.getResponsable());
        actividad.setNumeroRegistroProducto(request.getNumeroRegistroProducto());
        actividad.setIntervaloSeguridadDias(request.getIntervaloSeguridadDias());
        actividad.setObservaciones(request.getObservaciones());
    }
}