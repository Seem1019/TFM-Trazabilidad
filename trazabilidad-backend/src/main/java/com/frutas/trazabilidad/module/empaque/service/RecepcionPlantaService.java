package com.frutas.trazabilidad.module.empaque.service;

import com.frutas.trazabilidad.module.empaque.dto.RecepcionPlantaRequest;
import com.frutas.trazabilidad.module.empaque.dto.RecepcionPlantaResponse;
import com.frutas.trazabilidad.module.empaque.entity.RecepcionPlanta;
import com.frutas.trazabilidad.module.empaque.mapper.RecepcionPlantaMapper;
import com.frutas.trazabilidad.module.empaque.repository.RecepcionPlantaRepository;
import com.frutas.trazabilidad.module.produccion.entity.Lote;
import com.frutas.trazabilidad.module.produccion.repository.LoteRepository;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de recepciones en planta.
 */
@Service
@RequiredArgsConstructor
public class RecepcionPlantaService {

    private final RecepcionPlantaRepository recepcionRepository;
    private final LoteRepository loteRepository;
    private final RecepcionPlantaMapper mapper;

    @Transactional(readOnly = true)
    public List<RecepcionPlantaResponse> listarPorEmpresa(Long empresaId) {
        return recepcionRepository.findByEmpresaId(empresaId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecepcionPlantaResponse> listarPorLote(Long loteId, Long empresaId) {
        validarLotePertenencia(loteId, empresaId);
        return recepcionRepository.findByLoteIdAndActivoTrueOrderByFechaRecepcionDesc(loteId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecepcionPlantaResponse> listarPorEstado(Long empresaId, String estado) {
        return recepcionRepository.findByEmpresaIdAndEstado(empresaId, estado).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecepcionPlantaResponse> listarPorRangoFechas(Long empresaId, LocalDate desde, LocalDate hasta) {
        return recepcionRepository.findByEmpresaIdAndFechaBetween(empresaId, desde, hasta).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecepcionPlantaResponse buscarPorId(Long id, Long empresaId) {
        RecepcionPlanta recepcion = recepcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recepción no encontrada con ID: " + id));

        validarPertenenciaEmpresa(recepcion, empresaId);
        return mapper.toResponse(recepcion);
    }

    @Transactional
    public RecepcionPlantaResponse crear(RecepcionPlantaRequest request, Long empresaId) {
        // Validar código único
        if (recepcionRepository.existsByCodigoRecepcion(request.getCodigoRecepcion())) {
            throw new IllegalArgumentException("Ya existe una recepción con el código: " + request.getCodigoRecepcion());
        }

        // Validar lote
        Lote lote = loteRepository.findById(request.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + request.getLoteId()));

        validarLotePertenencia(lote.getId(), empresaId);

        RecepcionPlanta recepcion = mapper.toEntity(request, lote);
        RecepcionPlanta saved = recepcionRepository.save(recepcion);

        return mapper.toResponse(saved);
    }

    @Transactional
    public RecepcionPlantaResponse actualizar(Long id, RecepcionPlantaRequest request, Long empresaId) {
        RecepcionPlanta recepcion = recepcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recepción no encontrada con ID: " + id));

        validarPertenenciaEmpresa(recepcion, empresaId);

        // Validar código único si cambió
        if (!recepcion.getCodigoRecepcion().equals(request.getCodigoRecepcion()) &&
                recepcionRepository.existsByCodigoRecepcion(request.getCodigoRecepcion())) {
            throw new IllegalArgumentException("Ya existe una recepción con el código: " + request.getCodigoRecepcion());
        }

        // Validar lote
        Lote lote = loteRepository.findById(request.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + request.getLoteId()));

        validarLotePertenencia(lote.getId(), empresaId);

        mapper.updateEntityFromRequest(recepcion, request, lote);
        RecepcionPlanta updated = recepcionRepository.save(recepcion);

        return mapper.toResponse(updated);
    }

    @Transactional
    public void eliminar(Long id, Long empresaId) {
        RecepcionPlanta recepcion = recepcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recepción no encontrada con ID: " + id));

        validarPertenenciaEmpresa(recepcion, empresaId);

        recepcion.setActivo(false);
        recepcionRepository.save(recepcion);
    }

    @Transactional
    public RecepcionPlantaResponse cambiarEstado(Long id, String nuevoEstado, Long empresaId) {
        RecepcionPlanta recepcion = recepcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recepción no encontrada con ID: " + id));

        validarPertenenciaEmpresa(recepcion, empresaId);

        recepcion.setEstadoRecepcion(nuevoEstado);
        RecepcionPlanta updated = recepcionRepository.save(recepcion);

        return mapper.toResponse(updated);
    }

    private void validarLotePertenencia(Long loteId, Long empresaId) {
        if (!loteRepository.existsByIdAndEmpresaId(loteId, empresaId)) {
            throw new IllegalArgumentException("El lote no pertenece a la empresa del usuario");
        }
    }

    private void validarPertenenciaEmpresa(RecepcionPlanta recepcion, Long empresaId) {
        if (!recepcion.getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("La recepción no pertenece a la empresa del usuario");
        }
    }
}