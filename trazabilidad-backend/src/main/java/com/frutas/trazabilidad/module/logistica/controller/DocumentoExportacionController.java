package com.frutas.trazabilidad.module.logistica.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionRequest;
import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionResponse;
import com.frutas.trazabilidad.module.logistica.service.DocumentoExportacionService;
import com.frutas.trazabilidad.security.InputSanitizer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestión de documentos de exportación.
 * Implementa RBAC, sanitización de inputs y aislamiento multitenant.
 */
@RestController
@RequestMapping("/api/documentos-exportacion")
@RequiredArgsConstructor
@Tag(name = "Documentos de Exportación", description = "Gestión de documentos asociados a envíos")
public class DocumentoExportacionController {

    private final DocumentoExportacionService documentoService;
    private final InputSanitizer sanitizer;

    @PostMapping
    @Operation(summary = "Crear documento", description = "Crea un nuevo documento de exportación")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<DocumentoExportacionResponse>> crear(
            @Valid @RequestBody DocumentoExportacionRequest request,
            @AuthenticationPrincipal User user) {

        // Sanitizar campos de texto libre
        sanitizeRequest(request);

        // Validar URL si existe
        if (request.getUrlArchivo() != null && !sanitizer.isUrlSafe(request.getUrlArchivo())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("URL de archivo no válida"));
        }

        DocumentoExportacionResponse response = documentoService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Documento creado exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar documento", description = "Actualiza un documento existente")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<DocumentoExportacionResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoExportacionRequest request,
            @AuthenticationPrincipal User user) {

        // Sanitizar campos de texto libre
        sanitizeRequest(request);

        // Validar URL si existe
        if (request.getUrlArchivo() != null && !sanitizer.isUrlSafe(request.getUrlArchivo())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("URL de archivo no válida"));
        }

        DocumentoExportacionResponse response = documentoService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Documento actualizado exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del documento", description = "Actualiza el estado de un documento")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<DocumentoExportacionResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user) {

        String estado = body.get("estado");
        if (estado == null || estado.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El estado es obligatorio"));
        }

        // Sanitizar estado
        estado = sanitizer.sanitizeStrict(estado);

        DocumentoExportacionResponse response = documentoService.cambiarEstado(id, estado, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Estado actualizado exitosamente"));
    }

    @GetMapping("/envio/{envioId}")
    @Operation(summary = "Listar documentos por envío", description = "Lista todos los documentos de un envío")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<DocumentoExportacionResponse>>> listarPorEnvio(
            @PathVariable Long envioId,
            @AuthenticationPrincipal User user) {

        List<DocumentoExportacionResponse> documentos = documentoService.listarPorEnvio(envioId, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(documentos, "Documentos obtenidos exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento por ID", description = "Obtiene los detalles de un documento")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<DocumentoExportacionResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        DocumentoExportacionResponse documento = documentoService.obtenerPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(documento, "Documento obtenido exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar documento", description = "Elimina (desactiva) un documento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        documentoService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Documento eliminado exitosamente"));
    }

    /**
     * Sanitiza los campos de texto libre del request.
     */
    private void sanitizeRequest(DocumentoExportacionRequest request) {
        if (request.getNumeroDocumento() != null) {
            request.setNumeroDocumento(sanitizer.sanitizeStrict(request.getNumeroDocumento()));
        }
        if (request.getEntidadEmisora() != null) {
            request.setEntidadEmisora(sanitizer.sanitizeStrict(request.getEntidadEmisora()));
        }
        if (request.getFuncionarioEmisor() != null) {
            request.setFuncionarioEmisor(sanitizer.sanitizeStrict(request.getFuncionarioEmisor()));
        }
        if (request.getDescripcion() != null) {
            request.setDescripcion(sanitizer.sanitize(request.getDescripcion()));
        }
        if (request.getUrlArchivo() != null) {
            request.setUrlArchivo(sanitizer.sanitizeUrl(request.getUrlArchivo()));
        }
    }
}
