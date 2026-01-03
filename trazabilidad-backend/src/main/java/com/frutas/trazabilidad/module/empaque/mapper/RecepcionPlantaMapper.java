package com.frutas.trazabilidad.module.empaque.mapper;

import com.frutas.trazabilidad.module.empaque.dto.RecepcionPlantaRequest;
import com.frutas.trazabilidad.module.empaque.dto.RecepcionPlantaResponse;
import com.frutas.trazabilidad.module.empaque.entity.RecepcionPlanta;
import com.frutas.trazabilidad.module.produccion.entity.Lote;
import org.springframework.stereotype.Component;

/**
 * Mapper para RecepcionPlanta.
 */
@Component
public class RecepcionPlantaMapper {

    public RecepcionPlanta toEntity(RecepcionPlantaRequest request, Lote lote) {
        return RecepcionPlanta.builder()
                .lote(lote)
                .codigoRecepcion(request.getCodigoRecepcion())
                .fechaRecepcion(request.getFechaRecepcion())
                .horaRecepcion(request.getHoraRecepcion())
                .cantidadRecibida(request.getCantidadRecibida())
                .unidadMedida(request.getUnidadMedida())
                .temperaturaFruta(request.getTemperaturaFruta())
                .estadoInicial(request.getEstadoInicial())
                .responsableRecepcion(request.getResponsableRecepcion())
                .vehiculoTransporte(request.getVehiculoTransporte())
                .conductor(request.getConductor())
                .observaciones(request.getObservaciones())
                .estadoRecepcion("RECIBIDA")
                .activo(true)
                .build();
    }

    public RecepcionPlantaResponse toResponse(RecepcionPlanta entity) {
        return RecepcionPlantaResponse.builder()
                .id(entity.getId())
                .loteId(entity.getLote().getId())
                .loteNombre(entity.getLote().getNombre())
                .loteCodigoLote(entity.getLote().getCodigoLote())
                .fincaNombre(entity.getLote().getFinca().getNombre())
                .codigoRecepcion(entity.getCodigoRecepcion())
                .fechaRecepcion(entity.getFechaRecepcion())
                .horaRecepcion(entity.getHoraRecepcion())
                .cantidadRecibida(entity.getCantidadRecibida())
                .unidadMedida(entity.getUnidadMedida())
                .temperaturaFruta(entity.getTemperaturaFruta())
                .estadoInicial(entity.getEstadoInicial())
                .responsableRecepcion(entity.getResponsableRecepcion())
                .vehiculoTransporte(entity.getVehiculoTransporte())
                .conductor(entity.getConductor())
                .observaciones(entity.getObservaciones())
                .estadoRecepcion(entity.getEstadoRecepcion())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(RecepcionPlanta entity, RecepcionPlantaRequest request, Lote lote) {
        entity.setLote(lote);
        entity.setCodigoRecepcion(request.getCodigoRecepcion());
        entity.setFechaRecepcion(request.getFechaRecepcion());
        entity.setHoraRecepcion(request.getHoraRecepcion());
        entity.setCantidadRecibida(request.getCantidadRecibida());
        entity.setUnidadMedida(request.getUnidadMedida());
        entity.setTemperaturaFruta(request.getTemperaturaFruta());
        entity.setEstadoInicial(request.getEstadoInicial());
        entity.setResponsableRecepcion(request.getResponsableRecepcion());
        entity.setVehiculoTransporte(request.getVehiculoTransporte());
        entity.setConductor(request.getConductor());
        entity.setObservaciones(request.getObservaciones());
    }
}