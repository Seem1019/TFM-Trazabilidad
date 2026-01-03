package com.frutas.trazabilidad.module.empaque.service;

import com.frutas.trazabilidad.module.empaque.dto.EtiquetaRequest;
import com.frutas.trazabilidad.module.empaque.dto.EtiquetaResponse;
import com.frutas.trazabilidad.module.empaque.entity.Clasificacion;
import com.frutas.trazabilidad.module.empaque.entity.Etiqueta;
import com.frutas.trazabilidad.module.empaque.mapper.EtiquetaMapper;
import com.frutas.trazabilidad.module.empaque.repository.ClasificacionRepository;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaRepository;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de etiquetas con código QR.
 */
@Service
@RequiredArgsConstructor
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;
    private final ClasificacionRepository clasificacionRepository;
    private final EtiquetaMapper mapper;

    /**
     * URL base para generar enlaces QR públicos.
     */
    @Value("${app.qr-base-url}")
    private String qrBaseUrl;

    @Transactional(readOnly = true)
    public List<EtiquetaResponse> listarPorEmpresa(Long empresaId) {
        return etiquetaRepository.findByEmpresaId(empresaId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EtiquetaResponse> listarPorClasificacion(Long clasificacionId, Long empresaId) {
        validarClasificacionPertenencia(clasificacionId, empresaId);
        return etiquetaRepository.findByClasificacionIdAndActivoTrueOrderByCreatedAtDesc(clasificacionId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EtiquetaResponse> listarPorEstado(Long empresaId, String estado) {
        return etiquetaRepository.findByEmpresaIdAndEstado(empresaId, estado).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EtiquetaResponse> listarPorTipo(Long empresaId, String tipo) {
        return etiquetaRepository.findByEmpresaIdAndTipo(empresaId, tipo).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EtiquetaResponse buscarPorId(Long id, Long empresaId) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con ID: " + id));

        validarPertenenciaEmpresa(etiqueta, empresaId);
        return mapper.toResponse(etiqueta);
    }

    @Transactional(readOnly = true)
    public EtiquetaResponse buscarPorCodigoQr(String codigoQr) {
        Etiqueta etiqueta = etiquetaRepository.findByCodigoQr(codigoQr)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con código QR: " + codigoQr));

        return mapper.toResponse(etiqueta);
    }

    @Transactional
    public EtiquetaResponse crear(EtiquetaRequest request, Long empresaId) {
        // Validar código único
        if (etiquetaRepository.existsByCodigoEtiqueta(request.getCodigoEtiqueta())) {
            throw new IllegalArgumentException("Ya existe una etiqueta con el código: " + request.getCodigoEtiqueta());
        }

        // Validar clasificación
        Clasificacion clasificacion = clasificacionRepository.findById(request.getClasificacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con ID: " + request.getClasificacionId()));

        validarClasificacionPertenencia(clasificacion.getId(), empresaId);

        Etiqueta etiqueta = mapper.toEntity(request, clasificacion);

        // Generar URL del QR (el UUID ya se genera en @PrePersist)
        // El QR apuntará al frontend: http://localhost:5173/public/traza/{uuid}
        etiqueta.generarUrlQr(qrBaseUrl);

        Etiqueta saved = etiquetaRepository.save(etiqueta);

        return mapper.toResponse(saved);
    }

    @Transactional
    public EtiquetaResponse actualizar(Long id, EtiquetaRequest request, Long empresaId) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con ID: " + id));

        validarPertenenciaEmpresa(etiqueta, empresaId);

        // Validar código único si cambió
        if (!etiqueta.getCodigoEtiqueta().equals(request.getCodigoEtiqueta()) &&
                etiquetaRepository.existsByCodigoEtiqueta(request.getCodigoEtiqueta())) {
            throw new IllegalArgumentException("Ya existe una etiqueta con el código: " + request.getCodigoEtiqueta());
        }

        // Validar clasificación
        Clasificacion clasificacion = clasificacionRepository.findById(request.getClasificacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con ID: " + request.getClasificacionId()));

        validarClasificacionPertenencia(clasificacion.getId(), empresaId);

        mapper.updateEntityFromRequest(etiqueta, request, clasificacion);
        Etiqueta updated = etiquetaRepository.save(etiqueta);

        return mapper.toResponse(updated);
    }

    @Transactional
    public void eliminar(Long id, Long empresaId) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con ID: " + id));

        validarPertenenciaEmpresa(etiqueta, empresaId);

        etiqueta.setActivo(false);
        etiquetaRepository.save(etiqueta);
    }

    @Transactional
    public EtiquetaResponse cambiarEstado(Long id, String nuevoEstado, Long empresaId) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con ID: " + id));

        validarPertenenciaEmpresa(etiqueta, empresaId);

        etiqueta.setEstadoEtiqueta(nuevoEstado);
        Etiqueta updated = etiquetaRepository.save(etiqueta);

        return mapper.toResponse(updated);
    }

    private void validarClasificacionPertenencia(Long clasificacionId, Long empresaId) {
        if (!clasificacionRepository.existsByIdAndEmpresaId(clasificacionId, empresaId)) {
            throw new IllegalArgumentException("La clasificación no pertenece a la empresa del usuario");
        }
    }

    private void validarPertenenciaEmpresa(Etiqueta etiqueta, Long empresaId) {
        if (!etiqueta.getClasificacion().getRecepcion().getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("La etiqueta no pertenece a la empresa del usuario");
        }
    }
}