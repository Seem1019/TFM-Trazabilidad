package com.frutas.trazabilidad.module.produccion.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.module.produccion.dto.LoteRequest;
import com.frutas.trazabilidad.module.produccion.dto.LoteResponse;
import com.frutas.trazabilidad.module.produccion.service.LoteService;
import com.frutas.trazabilidad.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de lotes.
 */
@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@Tag(name = "Lotes", description = "Gestión de lotes de cultivo")
public class LoteController {

    private final LoteService loteService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Listar todos los lotes", description = "Obtiene todos los lotes de la empresa")
    public ResponseEntity<ApiResponse<List<LoteResponse>>> listar(@RequestHeader("Authorization") String token) {
        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<LoteResponse> lotes = loteService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.success(lotes, "Lotes obtenidos exitosamente"));
    }

    @GetMapping("/finca/{fincaId}")
    @Operation(summary = "Listar lotes por finca", description = "Obtiene todos los lotes de una finca específica")
    public ResponseEntity<ApiResponse<List<LoteResponse>>> listarPorFinca(
            @PathVariable Long fincaId,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<LoteResponse> lotes = loteService.listarPorFinca(fincaId, empresaId);
        return ResponseEntity.ok(ApiResponse.success(lotes, "Lotes de la finca obtenidos exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote por ID", description = "Obtiene los detalles de un lote específico")
    public ResponseEntity<ApiResponse<LoteResponse>> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        LoteResponse lote = loteService.buscarPorId(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(lote, "Lote encontrado"));
    }

    @PostMapping
    @Operation(summary = "Crear lote", description = "Crea un nuevo lote de cultivo")
    public ResponseEntity<ApiResponse<LoteResponse>> crear(
            @Valid @RequestBody LoteRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        LoteResponse lote = loteService.crear(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lote, "Lote creado exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar lote", description = "Actualiza los datos de un lote existente")
    public ResponseEntity<ApiResponse<LoteResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LoteRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        LoteResponse lote = loteService.actualizar(id, request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(lote, "Lote actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lote", description = "Elimina (desactiva) un lote")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        loteService.eliminar(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Lote eliminado exitosamente"));
    }

    @GetMapping("/listos-cosechar")
    @Operation(summary = "Lotes listos para cosechar", description = "Obtiene los lotes que están listos para cosechar")
    public ResponseEntity<ApiResponse<List<LoteResponse>>> listosParaCosechar(
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<LoteResponse> lotes = loteService.listarListosParaCosechar(empresaId);
        return ResponseEntity.ok(ApiResponse.success(lotes, "Lotes listos para cosechar obtenidos"));
    }
}