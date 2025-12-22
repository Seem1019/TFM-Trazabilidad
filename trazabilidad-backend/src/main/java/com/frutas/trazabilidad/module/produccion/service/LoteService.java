package com.frutas.trazabilidad.module.produccion.service;

import com.frutas.trazabilidad.exception.ConflictException;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.produccion.dto.LoteRequest;
import com.frutas.trazabilidad.module.produccion.dto.LoteResponse;
import com.frutas.trazabilidad.module.produccion.entity.Finca;
import com.frutas.trazabilidad.module.produccion.entity.Lote;
import com.frutas.trazabilidad.module.produccion.mapper.LoteMapper;
import com.frutas.trazabilidad.module.produccion.repository.ActividadAgronomicarepository;
import com.frutas.trazabilidad.module.produccion.repository.CosechaRepository;
import com.frutas.trazabilidad.module.produccion.repository.FincaRepository;
import com.frutas.trazabilidad.module.produccion.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de lotes de cultivo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoteService {

    private final LoteRepository loteRepository;
    private final FincaRepository fincaRepository;
    private final CosechaRepository cosechaRepository;
    private final ActividadAgronomicarepository actividadRepository;
    private final LoteMapper loteMapper;

    /**
     * Lista todos los lotes de una finca.
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> listarPorFinca(Long fincaId, Long empresaId) {
        log.debug("Listando lotes de finca {} para empresa {}", fincaId, empresaId);

        // Validar que la finca pertenece a la empresa
        fincaRepository.findByIdAndEmpresaId(fincaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", fincaId));

        List<Lote> lotes = loteRepository.findByFincaIdAndActivoTrue(fincaId);

        return lotes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos los lotes de una empresa.
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> listarPorEmpresa(Long empresaId) {
        log.debug("Listando todos los lotes para empresa {}", empresaId);

        List<Lote> lotes = loteRepository.findAllByEmpresaId(empresaId);

        return lotes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca un lote por ID validando pertenencia a la empresa.
     */
    @Transactional(readOnly = true)
    public LoteResponse buscarPorId(Long id, Long empresaId) {
        log.debug("Buscando lote id={} para empresa={}", id, empresaId);

        Lote lote = loteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", id));

        return convertirAResponse(lote);
    }

    /**
     * Crea un nuevo lote.
     */
    @Transactional
    public LoteResponse crear(LoteRequest request, Long empresaId) {
        log.info("Creando lote '{}' en finca {} para empresa {}",
                request.getNombre(), request.getFincaId(), empresaId);

        // Validar que la finca existe y pertenece a la empresa
        Finca finca = fincaRepository.findByIdAndEmpresaId(request.getFincaId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", request.getFincaId()));

        // Validar código único en la finca
        if (loteRepository.existsByCodigoLoteAndFincaId(request.getCodigoLote(), request.getFincaId())) {
            throw new ConflictException("Lote", "código", request.getCodigoLote());
        }

        // Mapear y guardar
        Lote lote = loteMapper.toEntity(request);
        lote.setFinca(finca);
        lote.setActivo(true);
        lote.setEstadoLote("ACTIVO");

        lote = loteRepository.save(lote);

        log.info("Lote creado exitosamente: id={}, código={}", lote.getId(), lote.getCodigoLote());

        return convertirAResponse(lote);
    }

    /**
     * Actualiza un lote existente.
     */
    @Transactional
    public LoteResponse actualizar(Long id, LoteRequest request, Long empresaId) {
        log.info("Actualizando lote id={} para empresa {}", id, empresaId);

        // Buscar y validar pertenencia
        Lote lote = loteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", id));

        // Validar código único (excepto el mismo lote)
        if (!lote.getCodigoLote().equals(request.getCodigoLote()) &&
                loteRepository.existsByCodigoLoteAndFincaId(request.getCodigoLote(), lote.getFinca().getId())) {
            throw new ConflictException("Lote", "código", request.getCodigoLote());
        }

        // Actualizar campos
        loteMapper.updateEntityFromRequest(request, lote);

        lote = loteRepository.save(lote);

        log.info("Lote actualizado exitosamente: id={}", lote.getId());

        return convertirAResponse(lote);
    }

    /**
     * Elimina (soft delete) un lote.
     */
    @Transactional
    public void eliminar(Long id, Long empresaId) {
        log.warn("Eliminando lote id={} de empresa {}", id, empresaId);

        Lote lote = loteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", id));

        lote.setActivo(false);
        lote.setEstadoLote("RETIRADO");
        loteRepository.save(lote);

        log.info("Lote eliminado (soft delete): id={}", id);
    }

    /**
     * Busca lotes listos para cosechar en una empresa.
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> listarListosParaCosechar(Long empresaId) {
        log.debug("Buscando lotes listos para cosechar en empresa {}", empresaId);

        List<Lote> lotes = loteRepository.findListosParaCosechar(empresaId);

        return lotes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // --- Métodos privados de ayuda ---

    private LoteResponse convertirAResponse(Lote lote) {
        LoteResponse response = loteMapper.toResponse(lote);

        // Agregar información calculada
        response.setEdadEnDias(lote.getEdadEnDias());
        response.setListoParaCosechar(lote.isListoParaCosechar());
        response.setTotalCosechado(cosechaRepository.sumCantidadCosechadaByLoteId(lote.getId()));
        response.setTotalActividades(actividadRepository.countByLoteIdAndTipoActividadAndActivoTrue(
                lote.getId(), "TODOS"));

        return response;
    }
}