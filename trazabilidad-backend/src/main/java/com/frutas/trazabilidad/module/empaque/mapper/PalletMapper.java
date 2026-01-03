package com.frutas.trazabilidad.module.empaque.mapper;

import com.frutas.trazabilidad.module.empaque.dto.PalletRequest;
import com.frutas.trazabilidad.module.empaque.dto.PalletResponse;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Mapper para Pallet.
 */
@Component
public class PalletMapper {

    public Pallet toEntity(PalletRequest request) {
        return Pallet.builder()
                .codigoPallet(request.getCodigoPallet())
                .fechaPaletizado(request.getFechaPaletizado())
                .tipoPallet(request.getTipoPallet())
                .numeroCajas(request.getNumeroCajas())
                .pesoNetoTotal(request.getPesoNetoTotal())
                .pesoBrutoTotal(request.getPesoBrutoTotal())
                .alturaPallet(request.getAlturaPallet())
                .tipoFruta(request.getTipoFruta())
                .calidad(request.getCalidad())
                .destino(request.getDestino())
                .temperaturaAlmacenamiento(request.getTemperaturaAlmacenamiento())
                .responsablePaletizado(request.getResponsablePaletizado())
                .observaciones(request.getObservaciones())
                .estadoPallet("ARMADO")
                .etiquetas(new ArrayList<>())
                .activo(true)
                .build();
    }

    public PalletResponse toResponse(Pallet entity) {
        PalletResponse response = PalletResponse.builder()
                .id(entity.getId())
                .codigoPallet(entity.getCodigoPallet())
                .fechaPaletizado(entity.getFechaPaletizado())
                .tipoPallet(entity.getTipoPallet())
                .numeroCajas(entity.getNumeroCajas())
                .pesoNetoTotal(entity.getPesoNetoTotal())
                .pesoBrutoTotal(entity.getPesoBrutoTotal())
                .alturaPallet(entity.getAlturaPallet())
                .tipoFruta(entity.getTipoFruta())
                .calidad(entity.getCalidad())
                .destino(entity.getDestino())
                .temperaturaAlmacenamiento(entity.getTemperaturaAlmacenamiento())
                .responsablePaletizado(entity.getResponsablePaletizado())
                .estadoPallet(entity.getEstadoPallet())
                .observaciones(entity.getObservaciones())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .totalEtiquetas(entity.getEtiquetas() != null ? entity.getEtiquetas().size() : 0)
                .build();

        if (entity.getEtiquetas() != null && !entity.getEtiquetas().isEmpty()) {
            response.setEtiquetasCodigos(
                    entity.getEtiquetas().stream()
                            .map(ep -> ep.getEtiqueta().getCodigoEtiqueta())
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    public void updateEntityFromRequest(Pallet entity, PalletRequest request) {
        entity.setCodigoPallet(request.getCodigoPallet());
        entity.setFechaPaletizado(request.getFechaPaletizado());
        entity.setTipoPallet(request.getTipoPallet());
        entity.setNumeroCajas(request.getNumeroCajas());
        entity.setPesoNetoTotal(request.getPesoNetoTotal());
        entity.setPesoBrutoTotal(request.getPesoBrutoTotal());
        entity.setAlturaPallet(request.getAlturaPallet());
        entity.setTipoFruta(request.getTipoFruta());
        entity.setCalidad(request.getCalidad());
        entity.setDestino(request.getDestino());
        entity.setTemperaturaAlmacenamiento(request.getTemperaturaAlmacenamiento());
        entity.setResponsablePaletizado(request.getResponsablePaletizado());
        entity.setObservaciones(request.getObservaciones());
    }
}