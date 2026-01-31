package com.frutas.trazabilidad.module.logistica.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoRequest;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoResponse;
import com.frutas.trazabilidad.module.logistica.service.EventoLogisticoService;
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

/**
 * Controller REST para gestión de eventos logísticos.
 * Implementa RBAC, sanitización de inputs y aislamiento multitenant.
 */
@RestController
@RequestMapping("/api/eventos-logisticos")
@RequiredArgsConstructor
@Tag(name = "Eventos Logísticos", description = "Gestión de eventos de seguimiento de envíos")
public class EventoLogisticoController {

    private final EventoLogisticoService eventoService;
    private final InputSanitizer sanitizer;

    @PostMapping
    @Operation(summary = "Crear evento", description = "Registra un nuevo evento logístico")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<EventoLogisticoResponse>> crear(
            @Valid @RequestBody EventoLogisticoRequest request,
            @AuthenticationPrincipal User user) {

        // Sanitizar campos de texto libre
        sanitizeRequest(request);

        // Validar URL de evidencia si existe
        if (request.getUrlEvidencia() != null && !sanitizer.isUrlSafe(request.getUrlEvidencia())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("URL de evidencia no válida"));
        }

        EventoLogisticoResponse response = eventoService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Evento registrado exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar evento", description = "Actualiza un evento existente")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<EventoLogisticoResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoLogisticoRequest request,
            @AuthenticationPrincipal User user) {

        // Sanitizar campos de texto libre
        sanitizeRequest(request);

        // Validar URL de evidencia si existe
        if (request.getUrlEvidencia() != null && !sanitizer.isUrlSafe(request.getUrlEvidencia())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("URL de evidencia no válida"));
        }

        EventoLogisticoResponse response = eventoService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Evento actualizado exitosamente"));
    }

    @GetMapping("/envio/{envioId}")
    @Operation(summary = "Listar eventos por envío", description = "Lista todos los eventos de un envío")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<EventoLogisticoResponse>>> listarPorEnvio(
            @PathVariable Long envioId,
            @AuthenticationPrincipal User user) {

        List<EventoLogisticoResponse> eventos = eventoService.listarPorEnvio(envioId, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(eventos, "Eventos obtenidos exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID", description = "Obtiene los detalles de un evento")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<EventoLogisticoResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        EventoLogisticoResponse evento = eventoService.obtenerPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(evento, "Evento obtenido exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar evento", description = "Elimina (desactiva) un evento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        eventoService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Evento eliminado exitosamente"));
    }

    /**
     * Sanitiza los campos de texto libre del request.
     */
    private void sanitizeRequest(EventoLogisticoRequest request) {
        if (request.getCodigoEvento() != null) {
            request.setCodigoEvento(sanitizer.sanitizeStrict(request.getCodigoEvento()));
        }
        if (request.getUbicacion() != null) {
            request.setUbicacion(sanitizer.sanitizeStrict(request.getUbicacion()));
        }
        if (request.getCiudad() != null) {
            request.setCiudad(sanitizer.sanitizeStrict(request.getCiudad()));
        }
        if (request.getPais() != null) {
            request.setPais(sanitizer.sanitizeStrict(request.getPais()));
        }
        if (request.getResponsable() != null) {
            request.setResponsable(sanitizer.sanitizeStrict(request.getResponsable()));
        }
        if (request.getOrganizacion() != null) {
            request.setOrganizacion(sanitizer.sanitizeStrict(request.getOrganizacion()));
        }
        if (request.getVehiculo() != null) {
            request.setVehiculo(sanitizer.sanitizeStrict(request.getVehiculo()));
        }
        if (request.getConductor() != null) {
            request.setConductor(sanitizer.sanitizeStrict(request.getConductor()));
        }
        if (request.getNumeroPrecinto() != null) {
            request.setNumeroPrecinto(sanitizer.sanitizeStrict(request.getNumeroPrecinto()));
        }
        if (request.getObservaciones() != null) {
            request.setObservaciones(sanitizer.sanitize(request.getObservaciones()));
        }
        if (request.getDetalleIncidencia() != null) {
            request.setDetalleIncidencia(sanitizer.sanitize(request.getDetalleIncidencia()));
        }
        if (request.getUrlEvidencia() != null) {
            request.setUrlEvidencia(sanitizer.sanitizeUrl(request.getUrlEvidencia()));
        }
    }
}
