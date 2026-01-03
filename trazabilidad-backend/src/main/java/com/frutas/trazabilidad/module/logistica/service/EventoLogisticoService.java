package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoRequest;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoResponse;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.entity.EventoLogistico;
import com.frutas.trazabilidad.module.logistica.mapper.EventoLogisticoMapper;
import com.frutas.trazabilidad.module.logistica.repository.EnvioRepository;
import com.frutas.trazabilidad.module.logistica.repository.EventoLogisticoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoLogisticoService {

    private final EventoLogisticoRepository eventoRepository;
    private final EnvioRepository envioRepository;
    private final UserRepository userRepository;
    private final EventoLogisticoMapper eventoMapper;
    private final AuditoriaEventoService auditoriaService;

    @Transactional
    public EventoLogisticoResponse crear(EventoLogisticoRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Envio envio = envioRepository.findById(request.getEnvioId())
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + request.getEnvioId()));

        if (!envioRepository.existsByIdAndEmpresaId(request.getEnvioId(), usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para registrar eventos en este envío");
        }

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
    public EventoLogisticoResponse actualizar(Long id, EventoLogisticoRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        EventoLogistico evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento logístico no encontrado con ID: " + id));

        if (!eventoRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para modificar este evento");
        }

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
    public List<EventoLogisticoResponse> listarPorEnvio(Long envioId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!envioRepository.existsByIdAndEmpresaId(envioId, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para ver eventos de este envío");
        }

        return eventoRepository.findByEnvioIdOrderByFechaAsc(envioId)
                .stream()
                .map(eventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventoLogisticoResponse obtenerPorId(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        EventoLogistico evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento logístico no encontrado con ID: " + id));

        if (!eventoRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para ver este evento");
        }

        return eventoMapper.toResponse(evento);
    }

    @Transactional
    public void eliminar(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        EventoLogistico evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento logístico no encontrado con ID: " + id));

        if (!eventoRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para eliminar este evento");
        }

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
}