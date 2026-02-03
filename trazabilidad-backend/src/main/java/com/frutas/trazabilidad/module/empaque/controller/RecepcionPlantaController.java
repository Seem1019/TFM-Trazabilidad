package com.frutas.trazabilidad.module.empaque.controller;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.empaque.dto.RecepcionPlantaRequest;
import com.frutas.trazabilidad.module.empaque.dto.RecepcionPlantaResponse;
import com.frutas.trazabilidad.module.empaque.service.RecepcionPlantaService;
import com.frutas.trazabilidad.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para gestión de recepciones en planta.
 */
@RestController
@RequestMapping("/api/recepciones")
@RequiredArgsConstructor
public class RecepcionPlantaController {

    private final RecepcionPlantaService recepcionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<RecepcionPlantaResponse>>> listar(
            @AuthenticationPrincipal User user) {
        List<RecepcionPlantaResponse> recepciones = recepcionService.listarPorEmpresa(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(recepciones, "Recepciones obtenidas exitosamente"));
    }

    @GetMapping("/lote/{loteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<RecepcionPlantaResponse>>> listarPorLote(
            @PathVariable Long loteId,
            @AuthenticationPrincipal User user) {
        List<RecepcionPlantaResponse> recepciones = recepcionService.listarPorLote(loteId, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(recepciones, "Recepciones del lote obtenidas"));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<RecepcionPlantaResponse>>> listarPorEstado(
            @PathVariable String estado,
            @AuthenticationPrincipal User user) {
        List<RecepcionPlantaResponse> recepciones = recepcionService.listarPorEstado(user.getEmpresa().getId(), estado);
        return ResponseEntity.ok(ApiResponse.success(recepciones, "Recepciones por estado obtenidas"));
    }

    @GetMapping("/rango-fechas")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<RecepcionPlantaResponse>>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal User user) {
        List<RecepcionPlantaResponse> recepciones = recepcionService.listarPorRangoFechas(
                user.getEmpresa().getId(), desde, hasta);
        return ResponseEntity.ok(ApiResponse.success(recepciones, "Recepciones por rango de fechas obtenidas"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<RecepcionPlantaResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        RecepcionPlantaResponse recepcion = recepcionService.buscarPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(recepcion, "Recepción obtenida exitosamente"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<RecepcionPlantaResponse>> crear(
            @Valid @RequestBody RecepcionPlantaRequest request,
            @AuthenticationPrincipal User user) {
        RecepcionPlantaResponse recepcion = recepcionService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(recepcion, "Recepción creada exitosamente"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<RecepcionPlantaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RecepcionPlantaRequest request,
            @AuthenticationPrincipal User user) {
        RecepcionPlantaResponse recepcion = recepcionService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(recepcion, "Recepción actualizada exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<RecepcionPlantaResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @AuthenticationPrincipal User user) {
        RecepcionPlantaResponse recepcion = recepcionService.cambiarEstado(id, estado, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(recepcion, "Estado actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        recepcionService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Recepción eliminada exitosamente"));
    }
}