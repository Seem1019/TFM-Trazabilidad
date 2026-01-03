package com.frutas.trazabilidad.module.logistica.mapper;

import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoRequest;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoResponse;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.entity.EventoLogistico;
import org.springframework.stereotype.Component;

@Component
public class EventoLogisticoMapper {

    public EventoLogistico toEntity(EventoLogisticoRequest request, Envio envio) {
        EventoLogistico evento = new EventoLogistico();
        evento.setEnvio(envio);
        evento.setCodigoEvento(request.getCodigoEvento());
        evento.setTipoEvento(request.getTipoEvento());
        evento.setFechaEvento(request.getFechaEvento());
        evento.setHoraEvento(request.getHoraEvento());
        evento.setUbicacion(request.getUbicacion());
        evento.setCiudad(request.getCiudad());
        evento.setPais(request.getPais());
        evento.setLatitud(request.getLatitud());
        evento.setLongitud(request.getLongitud());
        evento.setResponsable(request.getResponsable());
        evento.setOrganizacion(request.getOrganizacion());
        evento.setTemperaturaRegistrada(request.getTemperaturaRegistrada());
        evento.setHumedadRegistrada(request.getHumedadRegistrada());
        evento.setVehiculo(request.getVehiculo());
        evento.setConductor(request.getConductor());
        evento.setNumeroPrecinto(request.getNumeroPrecinto());
        evento.setObservaciones(request.getObservaciones());
        evento.setUrlEvidencia(request.getUrlEvidencia());
        evento.setIncidencia(request.getIncidencia());
        evento.setDetalleIncidencia(request.getDetalleIncidencia());
        evento.setActivo(true);
        return evento;
    }

    public void updateEntity(EventoLogistico evento, EventoLogisticoRequest request) {
        evento.setCodigoEvento(request.getCodigoEvento());
        evento.setTipoEvento(request.getTipoEvento());
        evento.setFechaEvento(request.getFechaEvento());
        evento.setHoraEvento(request.getHoraEvento());
        evento.setUbicacion(request.getUbicacion());
        evento.setCiudad(request.getCiudad());
        evento.setPais(request.getPais());
        evento.setLatitud(request.getLatitud());
        evento.setLongitud(request.getLongitud());
        evento.setResponsable(request.getResponsable());
        evento.setOrganizacion(request.getOrganizacion());
        evento.setTemperaturaRegistrada(request.getTemperaturaRegistrada());
        evento.setHumedadRegistrada(request.getHumedadRegistrada());
        evento.setVehiculo(request.getVehiculo());
        evento.setConductor(request.getConductor());
        evento.setNumeroPrecinto(request.getNumeroPrecinto());
        evento.setObservaciones(request.getObservaciones());
        evento.setUrlEvidencia(request.getUrlEvidencia());
        evento.setIncidencia(request.getIncidencia());
        evento.setDetalleIncidencia(request.getDetalleIncidencia());
    }

    public EventoLogisticoResponse toResponse(EventoLogistico evento) {
        return EventoLogisticoResponse.builder()
                .id(evento.getId())
                .envioId(evento.getEnvio().getId())
                .codigoEnvio(evento.getEnvio().getCodigoEnvio())
                .codigoEvento(evento.getCodigoEvento())
                .tipoEvento(evento.getTipoEvento())
                .fechaEvento(evento.getFechaEvento())
                .horaEvento(evento.getHoraEvento())
                .fechaHoraEvento(evento.getFechaHoraEvento())
                .ubicacion(evento.getUbicacion())
                .ciudad(evento.getCiudad())
                .pais(evento.getPais())
                .latitud(evento.getLatitud())
                .longitud(evento.getLongitud())
                .responsable(evento.getResponsable())
                .organizacion(evento.getOrganizacion())
                .temperaturaRegistrada(evento.getTemperaturaRegistrada())
                .humedadRegistrada(evento.getHumedadRegistrada())
                .vehiculo(evento.getVehiculo())
                .conductor(evento.getConductor())
                .numeroPrecinto(evento.getNumeroPrecinto())
                .observaciones(evento.getObservaciones())
                .urlEvidencia(evento.getUrlEvidencia())
                .incidencia(evento.getIncidencia())
                .detalleIncidencia(evento.getDetalleIncidencia())
                .activo(evento.getActivo())
                .createdAt(evento.getCreatedAt())
                .build();
    }
}