package com.frutas.trazabilidad.module.produccion.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.module.produccion.dto.ActividadAgronomicarRequest;
import com.frutas.trazabilidad.module.produccion.dto.ActividadAgronomicarResponse;
import com.frutas.trazabilidad.module.produccion.service.ActividadAgronomicarService;
import com.frutas.trazabilidad.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de actividades agronómicas.
 */
@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
@Tag(name = "Actividades Agronómicas", description = "Gestión de actividades agronómicas")
public class ActividadAgronomicarController {

    private final ActividadAgronomicarService actividadService;
    private final JwtUtil jwtUtil;

    @GetMapping("/lote/{loteId}")
    @Operation(summary = "Listar actividades por lote", description = "Obtiene todas las actividades de un lote")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<ActividadAgronomicarResponse>>> listarPorLote(
            @PathVariable Long loteId,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<ActividadAgronomicarResponse> actividades = actividadService.listarPorLote(loteId, empresaId);
        return ResponseEntity.ok(ApiResponse.success(actividades, "Actividades obtenidas exitosamente"));
    }

    @GetMapping("/lote/{loteId}/tipo/{tipo}")
    @Operation(summary = "Listar actividades por tipo", description = "Obtiene actividades de un tipo específico")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<ActividadAgronomicarResponse>>> listarPorTipo(
            @PathVariable Long loteId,
            @PathVariable String tipo,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<ActividadAgronomicarResponse> actividades = actividadService.listarPorLoteYTipo(loteId, tipo, empresaId);
        return ResponseEntity.ok(ApiResponse.success(actividades, "Actividades filtradas obtenidas"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener actividad por ID", description = "Obtiene los detalles de una actividad específica")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<ActividadAgronomicarResponse>> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        ActividadAgronomicarResponse actividad = actividadService.buscarPorId(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(actividad, "Actividad encontrada"));
    }

    @PostMapping
    @Operation(summary = "Registrar actividad", description = "Registra una nueva actividad agronómica")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR')")
    public ResponseEntity<ApiResponse<ActividadAgronomicarResponse>> registrar(
            @Valid @RequestBody ActividadAgronomicarRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        ActividadAgronomicarResponse actividad = actividadService.registrar(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(actividad, "Actividad registrada exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar actividad", description = "Actualiza los datos de una actividad existente")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR')")
    public ResponseEntity<ApiResponse<ActividadAgronomicarResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActividadAgronomicarRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        ActividadAgronomicarResponse actividad = actividadService.actualizar(id, request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(actividad, "Actividad actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar actividad", description = "Elimina (desactiva) una actividad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        actividadService.eliminar(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Actividad eliminada exitosamente"));
    }

    @GetMapping("/lote/{loteId}/recientes")
    @Operation(summary = "Actividades recientes", description = "Obtiene las actividades de los últimos 30 días")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<ActividadAgronomicarResponse>>> recientes(
            @PathVariable Long loteId,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<ActividadAgronomicarResponse> actividades = actividadService.listarRecientes(loteId, empresaId);
        return ResponseEntity.ok(ApiResponse.success(actividades, "Actividades recientes obtenidas"));
    }
}