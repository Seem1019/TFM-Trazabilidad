package com.frutas.trazabilidad.module.logistica.mapper;

import com.frutas.trazabilidad.module.logistica.dto.AuditoriaEventoResponse;
import com.frutas.trazabilidad.module.logistica.entity.AuditoriaEvento;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaEventoMapper {

    public AuditoriaEventoResponse toResponse(AuditoriaEvento evento) {
        return AuditoriaEventoResponse.builder()
                .id(evento.getId())
                .usuarioId(evento.getUsuario().getId())
                .usuarioNombre(evento.getUsuario().getNombre())
                .usuarioEmail(evento.getUsuario().getEmail())
                .tipoEntidad(evento.getTipoEntidad())
                .entidadId(evento.getEntidadId())
                .codigoEntidad(evento.getCodigoEntidad())
                .tipoOperacion(evento.getTipoOperacion())
                .descripcionOperacion(evento.getDescripcionOperacion())
                .datosAnteriores(evento.getDatosAnteriores())
                .datosNuevos(evento.getDatosNuevos())
                .camposModificados(evento.getCamposModificados())
                .hashEvento(evento.getHashEvento())
                .hashAnterior(evento.getHashAnterior())
                .enCadena(evento.getEnCadena())
                .integridadVerificada(null) // Se calcula en el service
                .ipOrigen(evento.getIpOrigen())
                .userAgent(evento.getUserAgent())
                .empresaId(evento.getEmpresaId())
                .empresaNombre(evento.getEmpresaNombre())
                .modulo(evento.getModulo())
                .nivelCriticidad(evento.getNivelCriticidad())
                .esCritico(evento.esCritico())
                .fechaEvento(evento.getFechaEvento())
                .build();
    }
}