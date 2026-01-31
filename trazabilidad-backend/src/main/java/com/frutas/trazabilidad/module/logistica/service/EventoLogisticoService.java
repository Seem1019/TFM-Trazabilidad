package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.exception.ForbiddenException;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoRequest;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoResponse;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.entity.EventoLogistico;
import com.frutas.trazabilidad.module.logistica.mapper.EventoLogisticoMapper;
import com.frutas.trazabilidad.module.logistica.repository.EnvioRepository;
import com.frutas.trazabilidad.module.logistica.repository.EventoLogisticoRepository;
import com.frutas.trazabilidad.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de eventos logísticos.
 * Implementa aislamiento multitenant y auditoría.
 */
@Service
@RequiredArgsConstructor
public class EventoLogisticoService {

    private final EventoLogisticoRepository eventoRepository;
    private final EnvioRepository envioRepository;
    private final EventoLogisticoMapper eventoMapper;
    private final AuditoriaEventoService auditoriaService;
    private final TenantContext tenantContext;

    @Transactional
    public EventoLogisticoResponse crear(EventoLogisticoRequest request, Long empresaId) {
        User usuario = tenantContext.getCurrentUser();
        validarPertenenciaEmpresa(usuario, empresaId);

        // Validar que el envío pertenece a la empresa
        Envio envio = envioRepository.findByIdAndEmpresaId(request.getEnvioId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Envío", request.getEnvioId()));

        EventoLogistico evento = eventoMapper.toEntity(request, envio);
        evento = eventoRepository.save(evento);

        auditoriaService.registrarCreacion(
                "EVENTO_LOGISTICO",
                evento.getId(),
                evento.getTipoEvento(),
                "Registro de evento " + evento.getTipoEvento() + " en envío " + envio.getCodigoEnvio(),
                usuario
        );

        return eventoMapper.toResponse(evento);
    }

    @Transactional
    public EventoLogisticoResponse actualizar(Long id, EventoLogisticoRequest request, Long empresaId) {
        User usuario = tenantContext.getCurrentUser();
        validarPertenenciaEmpresa(usuario, empresaId);

        EventoLogistico evento = eventoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento logístico", id));

        eventoMapper.updateEntity(evento, request);
        evento = eventoRepository.save(evento);

        auditoriaService.registrarActualizacion(
                "EVENTO_LOGISTICO",
                evento.getId(),
                evento.getTipoEvento(),
                "Actualización de evento logístico",
                null,
                null,
                usuario
        );

        return eventoMapper.toResponse(evento);
    }

    @Transactional(readOnly = true)
    public List<EventoLogisticoResponse> listarPorEnvio(Long envioId, Long empresaId) {
        // Validar que el envío pertenece a la empresa
        if (!envioRepository.existsByIdAndEmpresaId(envioId, empresaId)) {
            throw new ResourceNotFoundException("Envío", envioId);
        }

        return eventoRepository.findByEnvioIdAndActivoTrueOrderByFechaEventoAsc(envioId)
                .stream()
                .map(eventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventoLogisticoResponse obtenerPorId(Long id, Long empresaId) {
        EventoLogistico evento = eventoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento logístico", id));

        return eventoMapper.toResponse(evento);
    }

    @Transactional
    public void eliminar(Long id, Long empresaId) {
        User usuario = tenantContext.getCurrentUser();
        validarPertenenciaEmpresa(usuario, empresaId);

        EventoLogistico evento = eventoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento logístico", id));

        evento.setActivo(false);
        eventoRepository.save(evento);

        auditoriaService.registrarEliminacion(
                "EVENTO_LOGISTICO",
                evento.getId(),
                evento.getTipoEvento(),
                "Eliminación de evento logístico",
                usuario
        );
    }

    private void validarPertenenciaEmpresa(User usuario, Long empresaId) {
        if (!usuario.getEmpresa().getId().equals(empresaId)) {
            throw new ForbiddenException("No tiene permisos para esta operación");
        }
    }
}
