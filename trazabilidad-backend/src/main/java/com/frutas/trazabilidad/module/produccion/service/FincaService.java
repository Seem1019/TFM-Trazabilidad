package com.frutas.trazabilidad.module.produccion.service;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.exception.ConflictException;
import com.frutas.trazabilidad.exception.ForbiddenException;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.produccion.dto.FincaRequest;
import com.frutas.trazabilidad.module.produccion.dto.FincaResponse;
import com.frutas.trazabilidad.module.produccion.entity.Finca;
import com.frutas.trazabilidad.module.produccion.mapper.FincaMapper;
import com.frutas.trazabilidad.module.produccion.repository.CertificacionRepository;
import com.frutas.trazabilidad.module.produccion.repository.FincaRepository;
import com.frutas.trazabilidad.module.produccion.repository.LoteRepository;
import com.frutas.trazabilidad.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de fincas.
 * Incluye validaciones de multiempresa y reglas de negocio.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FincaService {

    private final FincaRepository fincaRepository;
    private final EmpresaRepository empresaRepository;
    private final LoteRepository loteRepository;
    private final CertificacionRepository certificacionRepository;
    private final FincaMapper fincaMapper;

    /**
     * Lista todas las fincas activas de una empresa.
     */
    @Transactional(readOnly = true)
    public List<FincaResponse> listarPorEmpresa(Long empresaId) {
        log.debug("Listando fincas para empresa: {}", empresaId);

        List<Finca> fincas = fincaRepository.findByEmpresaIdAndActivoTrue(empresaId);

        return fincas.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca una finca por ID validando pertenencia a la empresa.
     */
    @Transactional(readOnly = true)
    public FincaResponse buscarPorId(Long id, Long empresaId) {
        log.debug("Buscando finca id={} para empresa={}", id, empresaId);

        Finca finca = fincaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", id));

        return convertirAResponse(finca);
    }

    /**
     * Crea una nueva finca.
     */
    @Transactional
    public FincaResponse crear(FincaRequest request, Long empresaId) {
        log.info("Creando finca '{}' para empresa {}", request.getNombre(), empresaId);

        // Validar que la empresa existe
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));

        // Validar que no exista código duplicado
        if (fincaRepository.existsByCodigoFincaAndEmpresaId(request.getCodigoFinca(), empresaId)) {
            throw new ConflictException("Finca", "código", request.getCodigoFinca());
        }

        // Mapear y guardar
        Finca finca = fincaMapper.toEntity(request);
        finca.setEmpresa(empresa);
        finca.setActivo(true);

        finca = fincaRepository.save(finca);

        log.info("Finca creada exitosamente: id={}, código={}", finca.getId(), finca.getCodigoFinca());

        return convertirAResponse(finca);
    }

    /**
     * Actualiza una finca existente.
     */
    @Transactional
    public FincaResponse actualizar(Long id, FincaRequest request, Long empresaId) {
        log.info("Actualizando finca id={} para empresa {}", id, empresaId);

        // Buscar y validar pertenencia
        Finca finca = fincaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", id));

        // Validar código único (excepto la misma finca)
        if (!finca.getCodigoFinca().equals(request.getCodigoFinca()) &&
                fincaRepository.existsByCodigoFincaAndEmpresaId(request.getCodigoFinca(), empresaId)) {
            throw new ConflictException("Finca", "código", request.getCodigoFinca());
        }

        // Actualizar campos
        fincaMapper.updateEntityFromRequest(request, finca);

        finca = fincaRepository.save(finca);

        log.info("Finca actualizada exitosamente: id={}", finca.getId());

        return convertirAResponse(finca);
    }

    /**
     * Elimina (soft delete) una finca.
     */
    @Transactional
    public void eliminar(Long id, Long empresaId) {
        log.warn("Eliminando finca id={} de empresa {}", id, empresaId);

        Finca finca = fincaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", id));

        // Verificar que no tenga lotes activos
        long lotesActivos = loteRepository.countByFincaIdAndActivoTrue(id);
        if (lotesActivos > 0) {
            throw new ConflictException("No se puede eliminar la finca porque tiene " + lotesActivos + " lotes activos");
        }

        finca.setActivo(false);
        fincaRepository.save(finca);

        log.info("Finca eliminada (soft delete): id={}", id);
    }

    /**
     * Busca fincas por nombre parcial.
     */
    @Transactional(readOnly = true)
    public List<FincaResponse> buscarPorNombre(String nombre, Long empresaId) {
        log.debug("Buscando fincas con nombre que contenga '{}' para empresa {}", nombre, empresaId);

        List<Finca> fincas = fincaRepository.findByEmpresaIdAndNombreContaining(empresaId, nombre);

        return fincas.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // --- Métodos privados de ayuda ---

    private FincaResponse convertirAResponse(Finca finca) {
        FincaResponse response = fincaMapper.toResponse(finca);

        // Agregar información adicional
        response.setTotalLotes(loteRepository.countByFincaIdAndActivoTrue(finca.getId()));
        response.setTotalCertificacionesVigentes(certificacionRepository.countCertificacionesVigentes(finca.getId()));

        return response;
    }
}