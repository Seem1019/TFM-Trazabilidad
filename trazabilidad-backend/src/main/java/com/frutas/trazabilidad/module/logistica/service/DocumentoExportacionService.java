package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.exception.ForbiddenException;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionRequest;
import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionResponse;
import com.frutas.trazabilidad.module.logistica.entity.DocumentoExportacion;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.mapper.DocumentoExportacionMapper;
import com.frutas.trazabilidad.module.logistica.repository.DocumentoExportacionRepository;
import com.frutas.trazabilidad.module.logistica.repository.EnvioRepository;
import com.frutas.trazabilidad.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de documentos de exportación.
 * Implementa aislamiento multitenant y auditoría.
 */
@Service
@RequiredArgsConstructor
public class DocumentoExportacionService {

    private final DocumentoExportacionRepository documentoRepository;
    private final EnvioRepository envioRepository;
    private final DocumentoExportacionMapper documentoMapper;
    private final AuditoriaEventoService auditoriaService;
    private final TenantContext tenantContext;

    @Transactional
    public DocumentoExportacionResponse crear(DocumentoExportacionRequest request, Long empresaId) {
        User usuario = tenantContext.getCurrentUser();
        validarPertenenciaEmpresa(usuario, empresaId);

        // Validar que el envío pertenece a la empresa
        Envio envio = envioRepository.findByIdAndEmpresaId(request.getEnvioId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Envío", request.getEnvioId()));

        // Validar que no existe documento con el mismo número en la empresa
        if (documentoRepository.existsByNumeroDocumentoAndEmpresaId(request.getNumeroDocumento(), empresaId)) {
            throw new IllegalArgumentException("Ya existe un documento con el número: " + request.getNumeroDocumento());
        }

        DocumentoExportacion documento = documentoMapper.toEntity(request, envio);
        documento = documentoRepository.save(documento);

        auditoriaService.registrarCreacion(
                "DOCUMENTO",
                documento.getId(),
                documento.getNumeroDocumento(),
                "Creación de documento " + documento.getTipoDocumento() + " para envío " + envio.getCodigoEnvio(),
                usuario
        );

        return documentoMapper.toResponse(documento);
    }

    @Transactional
    public DocumentoExportacionResponse actualizar(Long id, DocumentoExportacionRequest request, Long empresaId) {
        User usuario = tenantContext.getCurrentUser();
        validarPertenenciaEmpresa(usuario, empresaId);

        DocumentoExportacion documento = documentoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));

        documentoMapper.updateEntity(documento, request);
        documento = documentoRepository.save(documento);

        auditoriaService.registrarActualizacion(
                "DOCUMENTO",
                documento.getId(),
                documento.getNumeroDocumento(),
                "Actualización de documento",
                null,
                null,
                usuario
        );

        return documentoMapper.toResponse(documento);
    }

    @Transactional
    public DocumentoExportacionResponse cambiarEstado(Long id, String nuevoEstado, Long empresaId) {
        User usuario = tenantContext.getCurrentUser();
        validarPertenenciaEmpresa(usuario, empresaId);

        DocumentoExportacion documento = documentoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));

        String estadoAnterior = documento.getEstado();
        documento.setEstado(nuevoEstado);
        documento = documentoRepository.save(documento);

        auditoriaService.registrarActualizacion(
                "DOCUMENTO",
                documento.getId(),
                documento.getNumeroDocumento(),
                "Cambio de estado: " + estadoAnterior + " → " + nuevoEstado,
                estadoAnterior,
                nuevoEstado,
                usuario
        );

        return documentoMapper.toResponse(documento);
    }

    @Transactional(readOnly = true)
    public List<DocumentoExportacionResponse> listarPorEnvio(Long envioId, Long empresaId) {
        // Validar que el envío pertenece a la empresa
        if (!envioRepository.existsByIdAndEmpresaId(envioId, empresaId)) {
            throw new ResourceNotFoundException("Envío", envioId);
        }

        return documentoRepository.findByEnvioIdAndActivoTrue(envioId)
                .stream()
                .map(documentoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentoExportacionResponse obtenerPorId(Long id, Long empresaId) {
        DocumentoExportacion documento = documentoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));

        return documentoMapper.toResponse(documento);
    }

    @Transactional
    public void eliminar(Long id, Long empresaId) {
        User usuario = tenantContext.getCurrentUser();
        validarPertenenciaEmpresa(usuario, empresaId);

        DocumentoExportacion documento = documentoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));

        documento.setActivo(false);
        documentoRepository.save(documento);

        auditoriaService.registrarEliminacion(
                "DOCUMENTO",
                documento.getId(),
                documento.getNumeroDocumento(),
                "Eliminación de documento",
                usuario
        );
    }

    private void validarPertenenciaEmpresa(User usuario, Long empresaId) {
        if (!usuario.getEmpresa().getId().equals(empresaId)) {
            throw new ForbiddenException("No tiene permisos para esta operación");
        }
    }
}
