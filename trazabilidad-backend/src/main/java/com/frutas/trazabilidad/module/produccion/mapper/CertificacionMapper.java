package com.frutas.trazabilidad.module.produccion.mapper;

import com.frutas.trazabilidad.module.produccion.dto.CertificacionRequest;
import com.frutas.trazabilidad.module.produccion.dto.CertificacionResponse;
import com.frutas.trazabilidad.module.produccion.entity.Certificacion;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Mapper para conversión entre Certificacion entity y DTOs.
 */
@Component
public class CertificacionMapper {

    /**
     * Convierte CertificacionRequest a Certificacion entity.
     */
    public Certificacion toEntity(CertificacionRequest request) {
        return Certificacion.builder()
                .tipoCertificacion(request.getTipoCertificacion())
                .entidadEmisora(request.getEntidadEmisora())
                .numeroCertificado(request.getNumeroCertificado())
                .fechaEmision(request.getFechaEmision())
                .fechaVencimiento(request.getFechaVencimiento())
                .urlDocumento(request.getUrlDocumento())
                .observaciones(request.getObservaciones())
                .estado("VIGENTE")
                .build();
    }

    /**
     * Convierte Certificacion entity a CertificacionResponse.
     */
    public CertificacionResponse toResponse(Certificacion certificacion) {
        LocalDate hoy = LocalDate.now();
        long diasParaVencer = ChronoUnit.DAYS.between(hoy, certificacion.getFechaVencimiento());

        return CertificacionResponse.builder()
                .id(certificacion.getId())
                .fincaId(certificacion.getFinca().getId())
                .fincaNombre(certificacion.getFinca().getNombre())
                .tipoCertificacion(certificacion.getTipoCertificacion())
                .entidadEmisora(certificacion.getEntidadEmisora())
                .numeroCertificado(certificacion.getNumeroCertificado())
                .fechaEmision(certificacion.getFechaEmision())
                .fechaVencimiento(certificacion.getFechaVencimiento())
                .estado(certificacion.getEstado())
                .urlDocumento(certificacion.getUrlDocumento())
                .observaciones(certificacion.getObservaciones())
                .activo(certificacion.getActivo())
                .createdAt(certificacion.getCreatedAt())
                .updatedAt(certificacion.getUpdatedAt())
                .vigente(certificacion.isVigente())
                .diasParaVencer(diasParaVencer > 0 ? diasParaVencer : 0L)
                .build();
    }

    /**
     * Actualiza una entidad Certificacion desde un CertificacionRequest.
     */
    public void updateEntityFromRequest(CertificacionRequest request, Certificacion certificacion) {
        certificacion.setTipoCertificacion(request.getTipoCertificacion());
        certificacion.setEntidadEmisora(request.getEntidadEmisora());
        certificacion.setNumeroCertificado(request.getNumeroCertificado());
        certificacion.setFechaEmision(request.getFechaEmision());
        certificacion.setFechaVencimiento(request.getFechaVencimiento());
        certificacion.setUrlDocumento(request.getUrlDocumento());
        certificacion.setObservaciones(request.getObservaciones());

        // Actualizar estado según fecha
        certificacion.actualizarEstado();
    }
}