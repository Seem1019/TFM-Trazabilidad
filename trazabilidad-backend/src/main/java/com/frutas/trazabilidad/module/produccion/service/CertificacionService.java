package com.frutas.trazabilidad.module.produccion.service;

import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.produccion.dto.CertificacionRequest;
import com.frutas.trazabilidad.module.produccion.dto.CertificacionResponse;
import com.frutas.trazabilidad.module.produccion.entity.Certificacion;
import com.frutas.trazabilidad.module.produccion.entity.Finca;
import com.frutas.trazabilidad.module.produccion.mapper.CertificacionMapper;
import com.frutas.trazabilidad.module.produccion.repository.CertificacionRepository;
import com.frutas.trazabilidad.module.produccion.repository.FincaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de certificaciones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CertificacionService {

    private final CertificacionRepository certificacionRepository;
    private final FincaRepository fincaRepository;
    private final CertificacionMapper certificacionMapper;

    /**
     * Lista todas las certificaciones de una finca.
     */
    @Transactional(readOnly = true)
    public List<CertificacionResponse> listarPorFinca(Long fincaId, Long empresaId) {
        log.debug("Listando certificaciones de finca {} para empresa {}", fincaId, empresaId);

        // Validar que la finca pertenece a la empresa
        fincaRepository.findByIdAndEmpresaId(fincaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", fincaId));

        List<Certificacion> certificaciones = certificacionRepository.findByFincaIdAndActivoTrue(fincaId);

        return certificaciones.stream()
                .map(certificacionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista certificaciones vigentes de una finca.
     */
    @Transactional(readOnly = true)
    public List<CertificacionResponse> listarVigentesPorFinca(Long fincaId, Long empresaId) {
        log.debug("Listando certificaciones vigentes de finca {} para empresa {}", fincaId, empresaId);

        // Validar que la finca pertenece a la empresa
        fincaRepository.findByIdAndEmpresaId(fincaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", fincaId));

        List<Certificacion> certificaciones = certificacionRepository.findCertificacionesVigentes(fincaId);

        return certificaciones.stream()
                .map(certificacionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca una certificación por ID.
     */
    @Transactional(readOnly = true)
    public CertificacionResponse buscarPorId(Long id, Long empresaId) {
        log.debug("Buscando certificación id={} para empresa={}", id, empresaId);

        Certificacion certificacion = certificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificación", id));

        // Validar que pertenece a la empresa
        if (!certificacion.getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Certificación", id);
        }

        return certificacionMapper.toResponse(certificacion);
    }

    /**
     * Crea una nueva certificación.
     */
    @Transactional
    public CertificacionResponse crear(CertificacionRequest request, Long empresaId) {
        log.info("Creando certificación '{}' para finca {} de empresa {}",
                request.getTipoCertificacion(), request.getFincaId(), empresaId);

        // Validar que la finca existe y pertenece a la empresa
        Finca finca = fincaRepository.findByIdAndEmpresaId(request.getFincaId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", request.getFincaId()));

        // Mapear y guardar
        Certificacion certificacion = certificacionMapper.toEntity(request);
        certificacion.setFinca(finca);
        certificacion.setActivo(true);
        certificacion.actualizarEstado();

        certificacion = certificacionRepository.save(certificacion);

        log.info("Certificación creada exitosamente: id={}, tipo={}",
                certificacion.getId(), certificacion.getTipoCertificacion());

        return certificacionMapper.toResponse(certificacion);
    }

    /**
     * Actualiza una certificación existente.
     */
    @Transactional
    public CertificacionResponse actualizar(Long id, CertificacionRequest request, Long empresaId) {
        log.info("Actualizando certificación id={} para empresa {}", id, empresaId);

        Certificacion certificacion = certificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificación", id));

        // Validar que pertenece a la empresa
        if (!certificacion.getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Certificación", id);
        }

        // Actualizar campos
        certificacionMapper.updateEntityFromRequest(request, certificacion);

        certificacion = certificacionRepository.save(certificacion);

        log.info("Certificación actualizada exitosamente: id={}", certificacion.getId());

        return certificacionMapper.toResponse(certificacion);
    }

    /**
     * Elimina (soft delete) una certificación.
     */
    @Transactional
    public void eliminar(Long id, Long empresaId) {
        log.warn("Eliminando certificación id={} de empresa {}", id, empresaId);

        Certificacion certificacion = certificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificación", id));

        // Validar que pertenece a la empresa
        if (!certificacion.getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Certificación", id);
        }

        certificacion.setActivo(false);
        certificacionRepository.save(certificacion);

        log.info("Certificación eliminada (soft delete): id={}", id);
    }

    /**
     * Lista certificaciones próximas a vencer (30 días).
     */
    @Transactional(readOnly = true)
    public List<CertificacionResponse> listarProximasAVencer(Long empresaId) {
        log.debug("Buscando certificaciones próximas a vencer para empresa {}", empresaId);

        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(30);

        List<Certificacion> certificaciones = certificacionRepository.findProximasAVencer(empresaId, hoy, fechaLimite);

        return certificaciones.stream()
                .map(certificacionMapper::toResponse)
                .collect(Collectors.toList());
    }
}