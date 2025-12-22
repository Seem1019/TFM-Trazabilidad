package com.frutas.trazabilidad.module.produccion.service;

import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.produccion.dto.CosechaRequest;
import com.frutas.trazabilidad.module.produccion.dto.CosechaResponse;
import com.frutas.trazabilidad.module.produccion.entity.Cosecha;
import com.frutas.trazabilidad.module.produccion.entity.Lote;
import com.frutas.trazabilidad.module.produccion.mapper.CosechaMapper;
import com.frutas.trazabilidad.module.produccion.repository.CosechaRepository;
import com.frutas.trazabilidad.module.produccion.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de cosechas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CosechaService {

    private final CosechaRepository cosechaRepository;
    private final LoteRepository loteRepository;
    private final CosechaMapper cosechaMapper;

    /**
     * Lista todas las cosechas de un lote.
     */
    @Transactional(readOnly = true)
    public List<CosechaResponse> listarPorLote(Long loteId, Long empresaId) {
        log.debug("Listando cosechas de lote {} para empresa {}", loteId, empresaId);

        // Validar que el lote existe y pertenece a la empresa
        loteRepository.findByIdAndEmpresaId(loteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));

        List<Cosecha> cosechas = cosechaRepository.findByLoteIdAndActivoTrueOrderByFechaCosechaDesc(loteId);

        return cosechas.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista todas las cosechas de una empresa.
     */
    @Transactional(readOnly = true)
    public List<CosechaResponse> listarPorEmpresa(Long empresaId) {
        log.debug("Listando todas las cosechas para empresa {}", empresaId);

        List<Cosecha> cosechas = cosechaRepository.findByEmpresaId(empresaId);

        return cosechas.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca una cosecha por ID.
     */
    @Transactional(readOnly = true)
    public CosechaResponse buscarPorId(Long id, Long empresaId) {
        log.debug("Buscando cosecha id={} para empresa={}", id, empresaId);

        Cosecha cosecha = cosechaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cosecha", id));

        // Validar que pertenece a la empresa
        if (!cosecha.getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Cosecha", id);
        }

        return convertirAResponse(cosecha);
    }

    /**
     * Registra una nueva cosecha.
     */
    @Transactional
    public CosechaResponse registrar(CosechaRequest request, Long empresaId) {
        log.info("Registrando cosecha de {} {} en lote {} para empresa {}",
                request.getCantidadCosechada(), request.getUnidadMedida(),
                request.getLoteId(), empresaId);

        // Validar que el lote existe y pertenece a la empresa
        Lote lote = loteRepository.findByIdAndEmpresaId(request.getLoteId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", request.getLoteId()));

        // Mapear y guardar
        Cosecha cosecha = cosechaMapper.toEntity(request);
        cosecha.setLote(lote);
        cosecha.setActivo(true);

        // Actualizar estado del lote si es primera cosecha
        if (cosechaRepository.countByLoteIdAndActivoTrue(lote.getId()) == 0) {
            lote.setEstadoLote("EN_COSECHA");
            loteRepository.save(lote);
            log.info("Lote {} actualizado a estado EN_COSECHA", lote.getId());
        }

        cosecha = cosechaRepository.save(cosecha);

        log.info("Cosecha registrada exitosamente: id={}, cantidad={} {}",
                cosecha.getId(), cosecha.getCantidadCosechada(), cosecha.getUnidadMedida());

        return convertirAResponse(cosecha);
    }

    /**
     * Actualiza una cosecha existente.
     */
    @Transactional
    public CosechaResponse actualizar(Long id, CosechaRequest request, Long empresaId) {
        log.info("Actualizando cosecha id={} para empresa {}", id, empresaId);

        Cosecha cosecha = cosechaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cosecha", id));

        // Validar que pertenece a la empresa
        if (!cosecha.getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Cosecha", id);
        }

        // Actualizar campos
        cosechaMapper.updateEntityFromRequest(request, cosecha);

        cosecha = cosechaRepository.save(cosecha);

        log.info("Cosecha actualizada exitosamente: id={}", cosecha.getId());

        return convertirAResponse(cosecha);
    }

    /**
     * Elimina (soft delete) una cosecha.
     */
    @Transactional
    public void eliminar(Long id, Long empresaId) {
        log.warn("Eliminando cosecha id={} de empresa {}", id, empresaId);

        Cosecha cosecha = cosechaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cosecha", id));

        // Validar que pertenece a la empresa
        if (!cosecha.getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Cosecha", id);
        }

        cosecha.setActivo(false);
        cosechaRepository.save(cosecha);

        log.info("Cosecha eliminada (soft delete): id={}", id);
    }

    /**
     * Lista cosechas recientes (últimos 30 días).
     */
    @Transactional(readOnly = true)
    public List<CosechaResponse> listarRecientes(Long empresaId) {
        log.debug("Buscando cosechas recientes para empresa {}", empresaId);

        LocalDate fechaDesde = LocalDate.now().minusDays(30);
        List<Cosecha> cosechas = cosechaRepository.findCosechasRecientes(empresaId, fechaDesde);

        return cosechas.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // --- Métodos privados de ayuda ---

    private CosechaResponse convertirAResponse(Cosecha cosecha) {
        CosechaResponse response = cosechaMapper.toResponse(cosecha);

        // Agregar información calculada
        response.setRendimientoPorHectarea(cosecha.getRendimientoPorHectarea());
        response.setReciente(cosecha.isReciente());

        return response;
    }
}