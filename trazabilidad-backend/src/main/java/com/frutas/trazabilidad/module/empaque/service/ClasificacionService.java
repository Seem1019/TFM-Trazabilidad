package com.frutas.trazabilidad.module.empaque.service;

import com.frutas.trazabilidad.module.empaque.dto.ClasificacionRequest;
import com.frutas.trazabilidad.module.empaque.dto.ClasificacionResponse;
import com.frutas.trazabilidad.module.empaque.entity.Clasificacion;
import com.frutas.trazabilidad.module.empaque.entity.RecepcionPlanta;
import com.frutas.trazabilidad.module.empaque.mapper.ClasificacionMapper;
import com.frutas.trazabilidad.module.empaque.repository.ClasificacionRepository;
import com.frutas.trazabilidad.module.empaque.repository.RecepcionPlantaRepository;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de clasificaciones.
 */
@Service
@RequiredArgsConstructor
public class ClasificacionService {

    private final ClasificacionRepository clasificacionRepository;
    private final RecepcionPlantaRepository recepcionRepository;
    private final ClasificacionMapper mapper;

    @Transactional(readOnly = true)
    public List<ClasificacionResponse> listarPorEmpresa(Long empresaId) {
        return clasificacionRepository.findByEmpresaId(empresaId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClasificacionResponse> listarPorRecepcion(Long recepcionId, Long empresaId) {
        validarRecepcionPertenencia(recepcionId, empresaId);
        return clasificacionRepository.findByRecepcionIdAndActivoTrueOrderByFechaClasificacionDesc(recepcionId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClasificacionResponse> listarPorCalidad(Long empresaId, String calidad) {
        return clasificacionRepository.findByEmpresaIdAndCalidad(empresaId, calidad).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClasificacionResponse buscarPorId(Long id, Long empresaId) {
        Clasificacion clasificacion = clasificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con ID: " + id));

        validarPertenenciaEmpresa(clasificacion, empresaId);
        return mapper.toResponse(clasificacion);
    }

    @Transactional
    public ClasificacionResponse crear(ClasificacionRequest request, Long empresaId) {
        // Validar código único
        if (clasificacionRepository.existsByCodigoClasificacion(request.getCodigoClasificacion())) {
            throw new IllegalArgumentException("Ya existe una clasificación con el código: " + request.getCodigoClasificacion());
        }

        // Validar recepción
        RecepcionPlanta recepcion = recepcionRepository.findById(request.getRecepcionId())
                .orElseThrow(() -> new ResourceNotFoundException("Recepción no encontrada con ID: " + request.getRecepcionId()));

        validarRecepcionPertenencia(recepcion.getId(), empresaId);

        Clasificacion clasificacion = mapper.toEntity(request, recepcion);
        Clasificacion saved = clasificacionRepository.save(clasificacion);

        return mapper.toResponse(saved);
    }

    @Transactional
    public ClasificacionResponse actualizar(Long id, ClasificacionRequest request, Long empresaId) {
        Clasificacion clasificacion = clasificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con ID: " + id));

        validarPertenenciaEmpresa(clasificacion, empresaId);

        // Validar código único si cambió
        if (!clasificacion.getCodigoClasificacion().equals(request.getCodigoClasificacion()) &&
                clasificacionRepository.existsByCodigoClasificacion(request.getCodigoClasificacion())) {
            throw new IllegalArgumentException("Ya existe una clasificación con el código: " + request.getCodigoClasificacion());
        }

        // Validar recepción
        RecepcionPlanta recepcion = recepcionRepository.findById(request.getRecepcionId())
                .orElseThrow(() -> new ResourceNotFoundException("Recepción no encontrada con ID: " + request.getRecepcionId()));

        validarRecepcionPertenencia(recepcion.getId(), empresaId);

        mapper.updateEntityFromRequest(clasificacion, request, recepcion);
        Clasificacion updated = clasificacionRepository.save(clasificacion);

        return mapper.toResponse(updated);
    }

    @Transactional
    public void eliminar(Long id, Long empresaId) {
        Clasificacion clasificacion = clasificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con ID: " + id));

        validarPertenenciaEmpresa(clasificacion, empresaId);

        clasificacion.setActivo(false);
        clasificacionRepository.save(clasificacion);
    }

    private void validarRecepcionPertenencia(Long recepcionId, Long empresaId) {
        if (!recepcionRepository.existsByRecepcionIdAndEmpresaId(recepcionId, empresaId)) {
            throw new IllegalArgumentException("La recepción no pertenece a la empresa del usuario");
        }
    }

    private void validarPertenenciaEmpresa(Clasificacion clasificacion, Long empresaId) {
        if (!clasificacion.getRecepcion().getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("La clasificación no pertenece a la empresa del usuario");
        }
    }
}