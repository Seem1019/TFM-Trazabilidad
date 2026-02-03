package com.frutas.trazabilidad.module.produccion.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.produccion.dto.FincaRequest;
import com.frutas.trazabilidad.module.produccion.dto.FincaResponse;
import com.frutas.trazabilidad.module.produccion.service.FincaService;
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
 * Controlador REST para gestión de fincas.
 * Implementa RBAC y aislamiento multitenant.
 */
@RestController
@RequestMapping("/api/fincas")
@RequiredArgsConstructor
@Tag(name = "Fincas", description = "Gestión de fincas productoras")
public class FincaController {

    private final FincaService fincaService;

    @GetMapping
    @Operation(summary = "Listar fincas", description = "Obtiene todas las fincas activas de la empresa")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<FincaResponse>>> listar(
            @AuthenticationPrincipal User user) {
        List<FincaResponse> fincas = fincaService.listarPorEmpresa(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(fincas, "Fincas obtenidas exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener finca por ID", description = "Obtiene los detalles de una finca específica")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<FincaResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        FincaResponse finca = fincaService.buscarPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(finca, "Finca encontrada"));
    }

    @PostMapping
    @Operation(summary = "Crear finca", description = "Crea una nueva finca en la empresa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FincaResponse>> crear(
            @Valid @RequestBody FincaRequest request,
            @AuthenticationPrincipal User user) {
        FincaResponse finca = fincaService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(finca, "Finca creada exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar finca", description = "Actualiza los datos de una finca existente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FincaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody FincaRequest request,
            @AuthenticationPrincipal User user) {
        FincaResponse finca = fincaService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(finca, "Finca actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar finca", description = "Elimina (desactiva) una finca")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        fincaService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Finca eliminada exitosamente"));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar fincas por nombre", description = "Busca fincas que contengan el texto especificado")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<FincaResponse>>> buscarPorNombre(
            @RequestParam String nombre,
            @AuthenticationPrincipal User user) {
        List<FincaResponse> fincas = fincaService.buscarPorNombre(nombre, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(fincas, "Búsqueda completada"));
    }
}
