package com.frutas.trazabilidad.module.empaque.mapper;

import com.frutas.trazabilidad.module.empaque.dto.ControlCalidadRequest;
import com.frutas.trazabilidad.module.empaque.dto.ControlCalidadResponse;
import com.frutas.trazabilidad.module.empaque.entity.Clasificacion;
import com.frutas.trazabilidad.module.empaque.entity.ControlCalidad;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import org.springframework.stereotype.Component;

/**
 * Mapper para ControlCalidad.
 */
@Component
public class ControlCalidadMapper {

    public ControlCalidad toEntity(ControlCalidadRequest request, Clasificacion clasificacion, Pallet pallet) {
        return ControlCalidad.builder()
                .clasificacion(clasificacion)
                .pallet(pallet)
                .codigoControl(request.getCodigoControl())
                .fechaControl(request.getFechaControl())
                .tipoControl(request.getTipoControl())
                .parametroEvaluado(request.getParametroEvaluado())
                .valorMedido(request.getValorMedido())
                .valorEsperado(request.getValorEsperado())
                .cumpleEspecificacion(request.getCumpleEspecificacion() != null ? request.getCumpleEspecificacion() : true)
                .resultado(request.getResultado())
                .responsableControl(request.getResponsableControl())
                .laboratorio(request.getLaboratorio())
                .numeroCertificado(request.getNumeroCertificado())
                .accionCorrectiva(request.getAccionCorrectiva())
                .observaciones(request.getObservaciones())
                .activo(true)
                .build();
    }

    public ControlCalidadResponse toResponse(ControlCalidad entity) {
        return ControlCalidadResponse.builder()
                .id(entity.getId())
                .clasificacionId(entity.getClasificacion() != null ? entity.getClasificacion().getId() : null)
                .clasificacionCodigo(entity.getClasificacion() != null ? entity.getClasificacion().getCodigoClasificacion() : null)
                .palletId(entity.getPallet() != null ? entity.getPallet().getId() : null)
                .palletCodigo(entity.getPallet() != null ? entity.getPallet().getCodigoPallet() : null)
                .codigoControl(entity.getCodigoControl())
                .fechaControl(entity.getFechaControl())
                .tipoControl(entity.getTipoControl())
                .parametroEvaluado(entity.getParametroEvaluado())
                .valorMedido(entity.getValorMedido())
                .valorEsperado(entity.getValorEsperado())
                .cumpleEspecificacion(entity.getCumpleEspecificacion())
                .resultado(entity.getResultado())
                .responsableControl(entity.getResponsableControl())
                .laboratorio(entity.getLaboratorio())
                .numeroCertificado(entity.getNumeroCertificado())
                .accionCorrectiva(entity.getAccionCorrectiva())
                .observaciones(entity.getObservaciones())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(ControlCalidad entity, ControlCalidadRequest request,
                                        Clasificacion clasificacion, Pallet pallet) {
        entity.setClasificacion(clasificacion);
        entity.setPallet(pallet);
        entity.setCodigoControl(request.getCodigoControl());
        entity.setFechaControl(request.getFechaControl());
        entity.setTipoControl(request.getTipoControl());
        entity.setParametroEvaluado(request.getParametroEvaluado());
        entity.setValorMedido(request.getValorMedido());
        entity.setValorEsperado(request.getValorEsperado());
        entity.setCumpleEspecificacion(request.getCumpleEspecificacion());
        entity.setResultado(request.getResultado());
        entity.setResponsableControl(request.getResponsableControl());
        entity.setLaboratorio(request.getLaboratorio());
        entity.setNumeroCertificado(request.getNumeroCertificado());
        entity.setAccionCorrectiva(request.getAccionCorrectiva());
        entity.setObservaciones(request.getObservaciones());
    }
}