package com.frutas.trazabilidad.module.produccion.mapper;

import com.frutas.trazabilidad.module.produccion.dto.CosechaRequest;
import com.frutas.trazabilidad.module.produccion.dto.CosechaResponse;
import com.frutas.trazabilidad.module.produccion.entity.Cosecha;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversión entre Cosecha entity y DTOs.
 */
@Component
public class CosechaMapper {

    /**
     * Convierte CosechaRequest a Cosecha entity.
     */
    public Cosecha toEntity(CosechaRequest request) {
        return Cosecha.builder()
                .fechaCosecha(request.getFechaCosecha())
                .cantidadCosechada(request.getCantidadCosechada())
                .unidadMedida(request.getUnidadMedida())
                .calidadInicial(request.getCalidadInicial())
                .estadoFruta(request.getEstadoFruta())
                .responsableCosecha(request.getResponsableCosecha())
                .numeroTrabajadores(request.getNumeroTrabajadores())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .temperaturaAmbiente(request.getTemperaturaAmbiente())
                .observaciones(request.getObservaciones())
                .build();
    }

    /**
     * Convierte Cosecha entity a CosechaResponse.
     */
    public CosechaResponse toResponse(Cosecha cosecha) {
        return CosechaResponse.builder()
                .id(cosecha.getId())
                .loteId(cosecha.getLote().getId())
                .loteNombre(cosecha.getLote().getNombre())
                .loteCodigoLote(cosecha.getLote().getCodigoLote())
                .fechaCosecha(cosecha.getFechaCosecha())
                .cantidadCosechada(cosecha.getCantidadCosechada())
                .unidadMedida(cosecha.getUnidadMedida())
                .calidadInicial(cosecha.getCalidadInicial())
                .estadoFruta(cosecha.getEstadoFruta())
                .responsableCosecha(cosecha.getResponsableCosecha())
                .numeroTrabajadores(cosecha.getNumeroTrabajadores())
                .horaInicio(cosecha.getHoraInicio())
                .horaFin(cosecha.getHoraFin())
                .temperaturaAmbiente(cosecha.getTemperaturaAmbiente())
                .observaciones(cosecha.getObservaciones())
                .activo(cosecha.getActivo())
                .createdAt(cosecha.getCreatedAt())
                .updatedAt(cosecha.getUpdatedAt())
                .build();
    }

    /**
     * Actualiza una entidad Cosecha desde un CosechaRequest.
     */
    public void updateEntityFromRequest(CosechaRequest request, Cosecha cosecha) {
        cosecha.setFechaCosecha(request.getFechaCosecha());
        cosecha.setCantidadCosechada(request.getCantidadCosechada());
        cosecha.setUnidadMedida(request.getUnidadMedida());
        cosecha.setCalidadInicial(request.getCalidadInicial());
        cosecha.setEstadoFruta(request.getEstadoFruta());
        cosecha.setResponsableCosecha(request.getResponsableCosecha());
        cosecha.setNumeroTrabajadores(request.getNumeroTrabajadores());
        cosecha.setHoraInicio(request.getHoraInicio());
        cosecha.setHoraFin(request.getHoraFin());
        cosecha.setTemperaturaAmbiente(request.getTemperaturaAmbiente());
        cosecha.setObservaciones(request.getObservaciones());
    }
}