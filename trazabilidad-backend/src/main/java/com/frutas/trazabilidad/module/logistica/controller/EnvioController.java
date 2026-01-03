package com.frutas.trazabilidad.module.logistica.controller;

import com.frutas.trazabilidad.module.logistica.dto.EnvioRequest;
import com.frutas.trazabilidad.module.logistica.dto.EnvioResponse;
import com.frutas.trazabilidad.module.logistica.service.EnvioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioService envioService;

    @PostMapping
    public ResponseEntity<EnvioResponse> crear(@Valid @RequestBody EnvioRequest request) {
        EnvioResponse response = envioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnvioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EnvioRequest request) {
        EnvioResponse response = envioService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pallets")
    public ResponseEntity<EnvioResponse> asignarPallets(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> palletsIds = body.get("palletsIds");
        EnvioResponse response = envioService.asignarPallets(id, palletsIds);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String estado = body.get("estado");
        EnvioResponse response = envioService.cambiarEstado(id, estado);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<EnvioResponse> cerrar(@PathVariable Long id) {
        EnvioResponse response = envioService.cerrar(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EnvioResponse>> listar() {
        List<EnvioResponse> envios = envioService.listarPorEmpresa();
        return ResponseEntity.ok(envios);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<EnvioResponse>> listarPorEstado(@PathVariable String estado) {
        List<EnvioResponse> envios = envioService.listarPorEstado(estado);
        return ResponseEntity.ok(envios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponse> obtenerPorId(@PathVariable Long id) {
        EnvioResponse envio = envioService.obtenerPorId(id);
        return ResponseEntity.ok(envio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        envioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}