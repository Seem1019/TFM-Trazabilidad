package com.frutas.trazabilidad.module.produccion.service;

import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.produccion.dto.ActividadAgronomicarRequest;
import com.frutas.trazabilidad.module.produccion.dto.ActividadAgronomicarResponse;
import com.frutas.trazabilidad.module.produccion.entity.ActividadAgronomica;
import com.frutas.trazabilidad.module.produccion.entity.Lote;
import com.frutas.trazabilidad.module.produccion.mapper.ActividadAgronomicarMapper;
import com.frutas.trazabilidad.module.produccion.repository.ActividadAgronomicarepository;
import com.frutas.trazabilidad.module.produccion.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de actividades agronómicas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActividadAgronomicarService {

    private final ActividadAgronomicarepository actividadRepository;
    private final LoteRepository loteRepository;
    private final ActividadAgronomicarMapper actividadMapper;

    /**
     * Lista todas las actividades de un lote.
     */
    @Transactional(readOnly = true)
    public List<ActividadAgronomicarResponse> listarPorLote(Long loteId, Long empresaId) {
        log.debug("Listando actividades de lote {} para empresa {}", loteId, empresaId);

        // Validar que el lote pertenece a la empresa
        loteRepository.findByIdAndEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));

        List<ActividadAgronomica> actividades = actividadRepository
                .findByLoteIdAndActivoTrueOrderByFechaActividadDesc(loteId);

        return actividades.stream()
                .map(actividadMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista actividades por tipo.
     */
    @Transactional(readOnly = true)
    public List<ActividadAgronomicarResponse> listarPorLoteYTipo(Long loteId, String tipo, Long empresaId) {
        log.debug("Listando actividades tipo '{}' de lote {} para empresa {}", tipo, loteId, empresaId);

        // Validar que el lote pertenece a la empresa
        loteRepository.findByIdAndEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));

        List<ActividadAgronomica> actividades = actividadRepository
                .findByLoteIdAndTipoActividadAndActivoTrue(loteId, tipo);

        return actividades.stream()
                .map(actividadMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca una actividad por ID.
     */
    @Transactional(readOnly = true)
    public ActividadAgronomicarResponse buscarPorId(Long id, Long empresaId) {
        log.debug("Buscando actividad id={} para empresa={}", id, empresaId);

        ActividadAgronomica actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad Agronómica", id));

        // Validar que pertenece a la empresa
        if (!actividad.getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Actividad Agronómica", id);
        }

        return actividadMapper.toResponse(actividad);
    }

    /**
     * Registra una nueva actividad agronómica.
     */
    @Transactional
    public ActividadAgronomicarResponse registrar(ActividadAgronomicarRequest request, Long empresaId) {
        log.info("Registrando actividad '{}' en lote {} para empresa {}",
                request.getTipoActividad(), request.getLoteId(), empresaId);

        // Validar que el lote existe y pertenece a la empresa
        Lote lote = loteRepository.findByIdAndEmpresaId(request.getLoteId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", request.getLoteId()));

        // Mapear y guardar
        ActividadAgronomica actividad = actividadMapper.toEntity(request);
        actividad.setLote(lote);
        actividad.setActivo(true);

        actividad = actividadRepository.save(actividad);

        log.info("Actividad registrada exitosamente: id={}, tipo={}",
                actividad.getId(), actividad.getTipoActividad());

        return actividadMapper.toResponse(actividad);
    }

    /**
     * Actualiza una actividad existente.
     */
    @Transactional
    public ActividadAgronomicarResponse actualizar(Long id, ActividadAgronomicarRequest request, Long empresaId) {
        log.info("Actualizando actividad id={} para empresa {}", id, empresaId);

        ActividadAgronomica actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad Agronómica", id));

        // Validar que pertenece a la empresa
        if (!actividad.getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Actividad Agronómica", id);
        }

        // Actualizar campos
        actividadMapper.updateEntityFromRequest(request, actividad);

        actividad = actividadRepository.save(actividad);

        log.info("Actividad actualizada exitosamente: id={}", actividad.getId());

        return actividadMapper.toResponse(actividad);
    }

    /**
     * Elimina (soft delete) una actividad.
     */
    @Transactional
    public void eliminar(Long id, Long empresaId) {
        log.warn("Eliminando actividad id={} de empresa {}", id, empresaId);

        ActividadAgronomica actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad Agronómica", id));

        // Validar que pertenece a la empresa
        if (!actividad.getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Actividad Agronómica", id);
        }

        actividad.setActivo(false);
        actividadRepository.save(actividad);

        log.info("Actividad eliminada (soft delete): id={}", id);
    }

    /**
     * Lista actividades recientes (últimos 30 días).
     */
    @Transactional(readOnly = true)
    public List<ActividadAgronomicarResponse> listarRecientes(Long loteId, Long empresaId) {
        log.debug("Buscando actividades recientes del lote {} para empresa {}", loteId, empresaId);

        // Validar que el lote pertenece a la empresa
        loteRepository.findByIdAndEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));

        LocalDate fechaDesde = LocalDate.now().minusDays(30);
        List<ActividadAgronomica> actividades = actividadRepository.findActividadesRecientes(loteId, fechaDesde);

        return actividades.stream()
                .map(actividadMapper::toResponse)
                .collect(Collectors.toList());
    }
}