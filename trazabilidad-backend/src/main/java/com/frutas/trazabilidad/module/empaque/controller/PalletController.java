package com.frutas.trazabilidad.module.empaque.controller;

import com.frutas.trazabilidad.module.empaque.dto.PalletRequest;
import com.frutas.trazabilidad.module.empaque.dto.PalletResponse;
import com.frutas.trazabilidad.module.empaque.service.PalletService;
import com.frutas.trazabilidad.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de pallets.
 */
@RestController
@RequestMapping("/api/pallets")
@RequiredArgsConstructor
public class PalletController {

    private final PalletService palletService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listar() {
        List<PalletResponse> pallets = palletService.listarTodos();
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets obtenidos exitosamente"));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listarPorEstado(
            @PathVariable String estado) {
        List<PalletResponse> pallets = palletService.listarPorEstado(estado);
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets por estado obtenidos"));
    }

    @GetMapping("/destino")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listarPorDestino(
            @RequestParam String destino) {
        List<PalletResponse> pallets = palletService.listarPorDestino(destino);
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets por destino obtenidos"));
    }

    @GetMapping("/tipo-fruta/{tipoFruta}")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listarPorTipoFruta(
            @PathVariable String tipoFruta) {
        List<PalletResponse> pallets = palletService.listarPorTipoFruta(tipoFruta);
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets por tipo de fruta obtenidos"));
    }

    @GetMapping("/listos-envio")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listarListosParaEnvio() {
        List<PalletResponse> pallets = palletService.listarListosParaEnvio();
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets listos para envío obtenidos"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PalletResponse>> obtenerPorId(
            @PathVariable Long id) {
        PalletResponse pallet = palletService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(pallet, "Pallet obtenido exitosamente"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PalletResponse>> crear(
            @Valid @RequestBody PalletRequest request) {
        PalletResponse pallet = palletService.crear(request);
        return ResponseEntity.ok(ApiResponse.success(pallet, "Pallet creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PalletResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PalletRequest request) {
        PalletResponse pallet = palletService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(pallet, "Pallet actualizado exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<PalletResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        PalletResponse pallet = palletService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success(pallet, "Estado actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id) {
        palletService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Pallet eliminado exitosamente"));
    }
}