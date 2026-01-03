package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionRequest;
import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionResponse;
import com.frutas.trazabilidad.module.logistica.entity.DocumentoExportacion;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.mapper.DocumentoExportacionMapper;
import com.frutas.trazabilidad.module.logistica.repository.DocumentoExportacionRepository;
import com.frutas.trazabilidad.module.logistica.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentoExportacionService {

    private final DocumentoExportacionRepository documentoRepository;
    private final EnvioRepository envioRepository;
    private final UserRepository userRepository;
    private final DocumentoExportacionMapper documentoMapper;
    private final AuditoriaEventoService auditoriaService;

    @Transactional
    public DocumentoExportacionResponse crear(DocumentoExportacionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Envio envio = envioRepository.findById(request.getEnvioId())
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + request.getEnvioId()));

        if (!envioRepository.existsByIdAndEmpresaId(request.getEnvioId(), usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para agregar documentos a este envío");
        }

        if (documentoRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
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
    public DocumentoExportacionResponse actualizar(Long id, DocumentoExportacionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        DocumentoExportacion documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        if (!documentoRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para modificar este documento");
        }

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
    public DocumentoExportacionResponse cambiarEstado(Long id, String nuevoEstado) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        DocumentoExportacion documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        if (!documentoRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para modificar este documento");
        }

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
    public List<DocumentoExportacionResponse> listarPorEnvio(Long envioId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!envioRepository.existsByIdAndEmpresaId(envioId, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para ver documentos de este envío");
        }

        return documentoRepository.findByEnvioId(envioId)
                .stream()
                .map(documentoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentoExportacionResponse obtenerPorId(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        DocumentoExportacion documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        if (!documentoRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para ver este documento");
        }

        return documentoMapper.toResponse(documento);
    }

    @Transactional
    public void eliminar(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        DocumentoExportacion documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        if (!documentoRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para eliminar este documento");
        }

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
}
