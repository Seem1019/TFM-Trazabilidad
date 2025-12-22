package com.frutas.trazabilidad.module.produccion.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.module.produccion.dto.FincaRequest;
import com.frutas.trazabilidad.module.produccion.dto.FincaResponse;
import com.frutas.trazabilidad.module.produccion.service.FincaService;
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
 * Controlador REST para gestión de fincas.
 */
@RestController
@RequestMapping("/api/fincas")
@RequiredArgsConstructor
@Tag(name = "Fincas", description = "Gestión de fincas productoras")
public class FincaController {

    private final FincaService fincaService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Listar fincas", description = "Obtiene todas las fincas activas de la empresa")
    public ResponseEntity<ApiResponse<List<FincaResponse>>> listar(@RequestHeader("Authorization") String token) {
        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<FincaResponse> fincas = fincaService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.success(fincas, "Fincas obtenidas exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener finca por ID", description = "Obtiene los detalles de una finca específica")
    public ResponseEntity<ApiResponse<FincaResponse>> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        FincaResponse finca = fincaService.buscarPorId(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(finca, "Finca encontrada"));
    }

    @PostMapping
    @Operation(summary = "Crear finca", description = "Crea una nueva finca en la empresa")
    public ResponseEntity<ApiResponse<FincaResponse>> crear(
            @Valid @RequestBody FincaRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        FincaResponse finca = fincaService.crear(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(finca, "Finca creada exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar finca", description = "Actualiza los datos de una finca existente")
    public ResponseEntity<ApiResponse<FincaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody FincaRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        FincaResponse finca = fincaService.actualizar(id, request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(finca, "Finca actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar finca", description = "Elimina (desactiva) una finca")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        fincaService.eliminar(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Finca eliminada exitosamente"));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar fincas por nombre", description = "Busca fincas que contengan el texto especificado")
    public ResponseEntity<ApiResponse<List<FincaResponse>>> buscarPorNombre(
            @RequestParam String nombre,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<FincaResponse> fincas = fincaService.buscarPorNombre(nombre, empresaId);
        return ResponseEntity.ok(ApiResponse.success(fincas, "Búsqueda completada"));
    }
}