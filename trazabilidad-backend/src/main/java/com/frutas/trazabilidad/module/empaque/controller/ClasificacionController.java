package com.frutas.trazabilidad.module.empaque.controller;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.empaque.dto.ClasificacionRequest;
import com.frutas.trazabilidad.module.empaque.dto.ClasificacionResponse;
import com.frutas.trazabilidad.module.empaque.service.ClasificacionService;
import com.frutas.trazabilidad.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de clasificaciones.
 * RBAC: ADMIN=CRUD, PRODUCTOR=R, OPERADOR_PLANTA=CRU, LOGISTICA=R, AUDITOR=R
 */
@RestController
@RequestMapping("/api/clasificaciones")
@RequiredArgsConstructor
public class ClasificacionController {

    private final ClasificacionService clasificacionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<ClasificacionResponse>>> listar(
            @AuthenticationPrincipal User user) {
        List<ClasificacionResponse> clasificaciones = clasificacionService.listarPorEmpresa(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(clasificaciones, "Clasificaciones obtenidas exitosamente"));
    }

    @GetMapping("/recepcion/{recepcionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<ClasificacionResponse>>> listarPorRecepcion(
            @PathVariable Long recepcionId,
            @AuthenticationPrincipal User user) {
        List<ClasificacionResponse> clasificaciones = clasificacionService.listarPorRecepcion(recepcionId, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(clasificaciones, "Clasificaciones de recepción obtenidas"));
    }

    @GetMapping("/calidad/{calidad}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<ClasificacionResponse>>> listarPorCalidad(
            @PathVariable String calidad,
            @AuthenticationPrincipal User user) {
        List<ClasificacionResponse> clasificaciones = clasificacionService.listarPorCalidad(user.getEmpresa().getId(), calidad);
        return ResponseEntity.ok(ApiResponse.success(clasificaciones, "Clasificaciones por calidad obtenidas"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<ClasificacionResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ClasificacionResponse clasificacion = clasificacionService.buscarPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(clasificacion, "Clasificación obtenida exitosamente"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<ClasificacionResponse>> crear(
            @Valid @RequestBody ClasificacionRequest request,
            @AuthenticationPrincipal User user) {
        ClasificacionResponse clasificacion = clasificacionService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(clasificacion, "Clasificación creada exitosamente"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<ClasificacionResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClasificacionRequest request,
            @AuthenticationPrincipal User user) {
        ClasificacionResponse clasificacion = clasificacionService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(clasificacion, "Clasificación actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        clasificacionService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Clasificación eliminada exitosamente"));
    }
}
