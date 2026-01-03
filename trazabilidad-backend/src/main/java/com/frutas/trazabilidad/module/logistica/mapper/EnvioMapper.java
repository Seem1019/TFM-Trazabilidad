package com.frutas.trazabilidad.module.logistica.mapper;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.logistica.dto.EnvioRequest;
import com.frutas.trazabilidad.module.logistica.dto.EnvioResponse;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper para convertir entre Envío entity y DTO.
 */
@Component
public class EnvioMapper {

    public Envio toEntity(EnvioRequest request, User usuario) {
        Envio envio = new Envio();
        envio.setCodigoEnvio(request.getCodigoEnvio());
        envio.setUsuario(usuario);
        envio.setFechaCreacion(request.getFechaCreacion());
        envio.setFechaSalidaEstimada(request.getFechaSalidaEstimada());
        envio.setExportador(request.getExportador());
        envio.setPaisDestino(request.getPaisDestino());
        envio.setPuertoDestino(request.getPuertoDestino());
        envio.setCiudadDestino(request.getCiudadDestino());
        envio.setTipoTransporte(request.getTipoTransporte());
        envio.setCodigoContenedor(request.getCodigoContenedor());
        envio.setTipoContenedor(request.getTipoContenedor());
        envio.setTemperaturaContenedor(request.getTemperaturaContenedor());
        envio.setTransportista(request.getTransportista());
        envio.setNumeroBooking(request.getNumeroBooking());
        envio.setNumeroBL(request.getNumeroBL());
        envio.setObservaciones(request.getObservaciones());
        envio.setClienteImportador(request.getClienteImportador());
        envio.setIncoterm(request.getIncoterm());
        envio.setEstado("CREADO");
        envio.setActivo(true);
        return envio;
    }

    public void updateEntity(Envio envio, EnvioRequest request) {
        envio.setCodigoEnvio(request.getCodigoEnvio());
        envio.setFechaCreacion(request.getFechaCreacion());
        envio.setFechaSalidaEstimada(request.getFechaSalidaEstimada());
        envio.setExportador(request.getExportador());
        envio.setPaisDestino(request.getPaisDestino());
        envio.setPuertoDestino(request.getPuertoDestino());
        envio.setCiudadDestino(request.getCiudadDestino());
        envio.setTipoTransporte(request.getTipoTransporte());
        envio.setCodigoContenedor(request.getCodigoContenedor());
        envio.setTipoContenedor(request.getTipoContenedor());
        envio.setTemperaturaContenedor(request.getTemperaturaContenedor());
        envio.setTransportista(request.getTransportista());
        envio.setNumeroBooking(request.getNumeroBooking());
        envio.setNumeroBL(request.getNumeroBL());
        envio.setObservaciones(request.getObservaciones());
        envio.setClienteImportador(request.getClienteImportador());
        envio.setIncoterm(request.getIncoterm());
    }

    public EnvioResponse toResponse(Envio envio) {
        return EnvioResponse.builder()
                .id(envio.getId())
                .codigoEnvio(envio.getCodigoEnvio())
                .usuarioId(envio.getUsuario().getId())
                .usuarioNombre(envio.getUsuario().getNombre())
                .empresaId(envio.getUsuario().getEmpresa().getId())
                .empresaNombre(envio.getUsuario().getEmpresa().getRazonSocial())
                .fechaCreacion(envio.getFechaCreacion())
                .fechaSalidaEstimada(envio.getFechaSalidaEstimada())
                .fechaSalidaReal(envio.getFechaSalidaReal())
                .exportador(envio.getExportador())
                .paisDestino(envio.getPaisDestino())
                .puertoDestino(envio.getPuertoDestino())
                .ciudadDestino(envio.getCiudadDestino())
                .tipoTransporte(envio.getTipoTransporte())
                .codigoContenedor(envio.getCodigoContenedor())
                .tipoContenedor(envio.getTipoContenedor())
                .temperaturaContenedor(envio.getTemperaturaContenedor())
                .transportista(envio.getTransportista())
                .numeroBooking(envio.getNumeroBooking())
                .numeroBL(envio.getNumeroBL())
                .estado(envio.getEstado())
                .pesoNetoTotal(envio.getPesoNetoTotal())
                .pesoBrutoTotal(envio.getPesoBrutoTotal())
                .numeroPallets(envio.getNumeroPallets())
                .numeroCajas(envio.getNumeroCajas())
                .observaciones(envio.getObservaciones())
                .clienteImportador(envio.getClienteImportador())
                .incoterm(envio.getIncoterm())
                .hashCierre(envio.getHashCierre())
                .fechaCierre(envio.getFechaCierre())
                .usuarioCierreNombre(envio.getUsuarioCierre() != null ? envio.getUsuarioCierre().getNombre() : null)
                .activo(envio.getActivo())
                .createdAt(envio.getCreatedAt())
                .updatedAt(envio.getUpdatedAt())
                .numeroEventos(envio.getEventos() != null ? (long) envio.getEventos().size() : 0L)
                .numeroDocumentos(envio.getDocumentos() != null ? (long) envio.getDocumentos().size() : 0L)
                .build();
    }
}