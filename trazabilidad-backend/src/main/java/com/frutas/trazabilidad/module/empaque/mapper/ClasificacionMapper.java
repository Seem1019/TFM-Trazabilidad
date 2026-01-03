package com.frutas.trazabilidad.module.empaque.mapper;

import com.frutas.trazabilidad.module.empaque.dto.ClasificacionRequest;
import com.frutas.trazabilidad.module.empaque.dto.ClasificacionResponse;
import com.frutas.trazabilidad.module.empaque.entity.Clasificacion;
import com.frutas.trazabilidad.module.empaque.entity.RecepcionPlanta;
import org.springframework.stereotype.Component;

/**
 * Mapper para Clasificacion.
 */
@Component
public class ClasificacionMapper {

    public Clasificacion toEntity(ClasificacionRequest request, RecepcionPlanta recepcion) {
        return Clasificacion.builder()
                .recepcion(recepcion)
                .codigoClasificacion(request.getCodigoClasificacion())
                .fechaClasificacion(request.getFechaClasificacion())
                .calidad(request.getCalidad())
                .cantidadClasificada(request.getCantidadClasificada())
                .unidadMedida(request.getUnidadMedida())
                .calibre(request.getCalibre())
                .porcentajeMerma(request.getPorcentajeMerma())
                .cantidadMerma(request.getCantidadMerma())
                .motivoMerma(request.getMotivoMerma())
                .responsableClasificacion(request.getResponsableClasificacion())
                .observaciones(request.getObservaciones())
                .activo(true)
                .build();
    }

    public ClasificacionResponse toResponse(Clasificacion entity) {
        return ClasificacionResponse.builder()
                .id(entity.getId())
                .recepcionId(entity.getRecepcion().getId())
                .recepcionCodigo(entity.getRecepcion().getCodigoRecepcion())
                .loteNombre(entity.getRecepcion().getLote().getNombre())
                .codigoClasificacion(entity.getCodigoClasificacion())
                .fechaClasificacion(entity.getFechaClasificacion())
                .calidad(entity.getCalidad())
                .cantidadClasificada(entity.getCantidadClasificada())
                .unidadMedida(entity.getUnidadMedida())
                .calibre(entity.getCalibre())
                .porcentajeMerma(entity.getPorcentajeMerma())
                .cantidadMerma(entity.getCantidadMerma())
                .motivoMerma(entity.getMotivoMerma())
                .responsableClasificacion(entity.getResponsableClasificacion())
                .observaciones(entity.getObservaciones())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(Clasificacion entity, ClasificacionRequest request, RecepcionPlanta recepcion) {
        entity.setRecepcion(recepcion);
        entity.setCodigoClasificacion(request.getCodigoClasificacion());
        entity.setFechaClasificacion(request.getFechaClasificacion());
        entity.setCalidad(request.getCalidad());
        entity.setCantidadClasificada(request.getCantidadClasificada());
        entity.setUnidadMedida(request.getUnidadMedida());
        entity.setCalibre(request.getCalibre());
        entity.setPorcentajeMerma(request.getPorcentajeMerma());
        entity.setCantidadMerma(request.getCantidadMerma());
        entity.setMotivoMerma(request.getMotivoMerma());
        entity.setResponsableClasificacion(request.getResponsableClasificacion());
        entity.setObservaciones(request.getObservaciones());
    }
}