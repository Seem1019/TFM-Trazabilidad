package com.frutas.trazabilidad.module.produccion.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.produccion.dto.LoteRequest;
import com.frutas.trazabilidad.module.produccion.dto.LoteResponse;
import com.frutas.trazabilidad.module.produccion.service.LoteService;
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
 * Controlador REST para gestión de lotes.
 * Implementa RBAC y aislamiento multitenant.
 */
@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@Tag(name = "Lotes", description = "Gestión de lotes de cultivo")
public class LoteController {

    private final LoteService loteService;

    @GetMapping
    @Operation(summary = "Listar todos los lotes", description = "Obtiene todos los lotes de la empresa")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<LoteResponse>>> listar(
            @AuthenticationPrincipal User user) {
        List<LoteResponse> lotes = loteService.listarPorEmpresa(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(lotes, "Lotes obtenidos exitosamente"));
    }

    @GetMapping("/finca/{fincaId}")
    @Operation(summary = "Listar lotes por finca", description = "Obtiene todos los lotes de una finca específica")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<LoteResponse>>> listarPorFinca(
            @PathVariable Long fincaId,
            @AuthenticationPrincipal User user) {
        List<LoteResponse> lotes = loteService.listarPorFinca(fincaId, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(lotes, "Lotes de la finca obtenidos exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote por ID", description = "Obtiene los detalles de un lote específico")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<LoteResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        LoteResponse lote = loteService.buscarPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(lote, "Lote encontrado"));
    }

    @PostMapping
    @Operation(summary = "Crear lote", description = "Crea un nuevo lote de cultivo")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR')")
    public ResponseEntity<ApiResponse<LoteResponse>> crear(
            @Valid @RequestBody LoteRequest request,
            @AuthenticationPrincipal User user) {
        LoteResponse lote = loteService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lote, "Lote creado exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar lote", description = "Actualiza los datos de un lote existente")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR')")
    public ResponseEntity<ApiResponse<LoteResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LoteRequest request,
            @AuthenticationPrincipal User user) {
        LoteResponse lote = loteService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(lote, "Lote actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lote", description = "Elimina (desactiva) un lote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        loteService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Lote eliminado exitosamente"));
    }

    @GetMapping("/listos-cosechar")
    @Operation(summary = "Lotes listos para cosechar", description = "Obtiene los lotes que están listos para cosechar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<List<LoteResponse>>> listosParaCosechar(
            @AuthenticationPrincipal User user) {
        List<LoteResponse> lotes = loteService.listarListosParaCosechar(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(lotes, "Lotes listos para cosechar obtenidos"));
    }
}
