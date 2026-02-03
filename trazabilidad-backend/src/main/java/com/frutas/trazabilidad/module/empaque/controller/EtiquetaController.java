package com.frutas.trazabilidad.module.empaque.controller;

import com.frutas.trazabilidad.dto.TrazabilidadCompletaDTO;
import com.frutas.trazabilidad.dto.TrazabilidadPublicaDTO;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.empaque.dto.EtiquetaRequest;
import com.frutas.trazabilidad.module.empaque.dto.EtiquetaResponse;
import com.frutas.trazabilidad.module.empaque.service.EtiquetaService;
import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.service.TrazabilidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de etiquetas con QR.
 * RBAC: ADMIN=CRUD, PRODUCTOR=R, OPERADOR_PLANTA=CRU, LOGISTICA=R, AUDITOR=R
 */
@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
public class EtiquetaController {

    private final EtiquetaService etiquetaService;
    private final TrazabilidadService trazabilidadService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listar(
            @AuthenticationPrincipal User user) {
        List<EtiquetaResponse> etiquetas = etiquetaService.listarPorEmpresa(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(etiquetas, "Etiquetas obtenidas exitosamente"));
    }

    @GetMapping("/clasificacion/{clasificacionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listarPorClasificacion(
            @PathVariable Long clasificacionId,
            @AuthenticationPrincipal User user) {
        List<EtiquetaResponse> etiquetas = etiquetaService.listarPorClasificacion(clasificacionId, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(etiquetas, "Etiquetas de clasificación obtenidas"));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listarPorEstado(
            @PathVariable String estado,
            @AuthenticationPrincipal User user) {
        List<EtiquetaResponse> etiquetas = etiquetaService.listarPorEstado(user.getEmpresa().getId(), estado);
        return ResponseEntity.ok(ApiResponse.success(etiquetas, "Etiquetas por estado obtenidas"));
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listarPorTipo(
            @PathVariable String tipo,
            @AuthenticationPrincipal User user) {
        List<EtiquetaResponse> etiquetas = etiquetaService.listarPorTipo(user.getEmpresa().getId(), tipo);
        return ResponseEntity.ok(ApiResponse.success(etiquetas, "Etiquetas por tipo obtenidas"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<EtiquetaResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        EtiquetaResponse etiqueta = etiquetaService.buscarPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(etiqueta, "Etiqueta obtenida exitosamente"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<EtiquetaResponse>> crear(
            @Valid @RequestBody EtiquetaRequest request,
            @AuthenticationPrincipal User user) {
        EtiquetaResponse etiqueta = etiquetaService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(etiqueta, "Etiqueta creada exitosamente"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<EtiquetaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EtiquetaRequest request,
            @AuthenticationPrincipal User user) {
        EtiquetaResponse etiqueta = etiquetaService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(etiqueta, "Etiqueta actualizada exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<EtiquetaResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @AuthenticationPrincipal User user) {
        EtiquetaResponse etiqueta = etiquetaService.cambiarEstado(id, estado, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(etiqueta, "Estado actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        etiquetaService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Etiqueta eliminada exitosamente"));
    }

    /**
     * Endpoint público para consulta de trazabilidad completa por QR.
     * Accesible sin autenticación para consumidores finales.
     * Retorna información completa del recorrido del producto sin datos sensibles.
     */
    @GetMapping("/public/qr/{codigoQr}")
    public ResponseEntity<ApiResponse<TrazabilidadPublicaDTO>> consultarPorQr(
            @PathVariable String codigoQr) {
        TrazabilidadPublicaDTO trazabilidad = trazabilidadService.obtenerTrazabilidadPublica(codigoQr);
        return ResponseEntity.ok(ApiResponse.success(trazabilidad, "Trazabilidad completa obtenida exitosamente"));
    }

    /**
     * Endpoint interno para consulta de trazabilidad completa con todos los datos.
     * Solo accesible para usuarios autenticados de la empresa.
     * Retorna información completa incluyendo datos sensibles.
     */
    @GetMapping("/{id}/trazabilidad")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<TrazabilidadCompletaDTO>> consultarTrazabilidadCompleta(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        TrazabilidadCompletaDTO trazabilidad = trazabilidadService.obtenerTrazabilidadCompleta(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(trazabilidad, "Trazabilidad completa interna obtenida exitosamente"));
    }
}
