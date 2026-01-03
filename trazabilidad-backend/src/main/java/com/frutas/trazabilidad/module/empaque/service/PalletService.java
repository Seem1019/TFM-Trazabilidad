package com.frutas.trazabilidad.module.empaque.service;

import com.frutas.trazabilidad.module.empaque.dto.PalletRequest;
import com.frutas.trazabilidad.module.empaque.dto.PalletResponse;
import com.frutas.trazabilidad.module.empaque.entity.Etiqueta;
import com.frutas.trazabilidad.module.empaque.entity.EtiquetaPallet;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import com.frutas.trazabilidad.module.empaque.mapper.PalletMapper;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaRepository;
import com.frutas.trazabilidad.module.empaque.repository.PalletRepository;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de pallets.
 */
@Service
@RequiredArgsConstructor
public class PalletService {

    private final PalletRepository palletRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final PalletMapper mapper;

    @Transactional(readOnly = true)
    public List<PalletResponse> listarTodos() {
        return palletRepository.findByActivoTrueOrderByFechaPaletizadoDesc().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PalletResponse> listarPorEstado(String estado) {
        return palletRepository.findByEstadoPalletAndActivoTrue(estado).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PalletResponse> listarPorDestino(String destino) {
        return palletRepository.findByDestinoContainingIgnoreCaseAndActivoTrue(destino).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PalletResponse> listarPorTipoFruta(String tipoFruta) {
        return palletRepository.findByTipoFrutaAndActivoTrue(tipoFruta).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PalletResponse> listarListosParaEnvio() {
        return palletRepository.findPalletsListosParaEnvio().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PalletResponse buscarPorId(Long id) {
        Pallet pallet = palletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pallet no encontrado con ID: " + id));

        return mapper.toResponse(pallet);
    }

    @Transactional
    public PalletResponse crear(PalletRequest request) {
        // Validar código único
        if (palletRepository.existsByCodigoPallet(request.getCodigoPallet())) {
            throw new IllegalArgumentException("Ya existe un pallet con el código: " + request.getCodigoPallet());
        }

        Pallet pallet = mapper.toEntity(request);

        // Asociar etiquetas si se proporcionaron
        if (request.getEtiquetasIds() != null && !request.getEtiquetasIds().isEmpty()) {
            for (int i = 0; i < request.getEtiquetasIds().size(); i++) {
                Long etiquetaId = request.getEtiquetasIds().get(i);
                Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con ID: " + etiquetaId));

                // Validar que la etiqueta esté disponible
                if (!"DISPONIBLE".equals(etiqueta.getEstadoEtiqueta())) {
                    throw new IllegalArgumentException("La etiqueta " + etiqueta.getCodigoEtiqueta() + " no está disponible");
                }

                EtiquetaPallet ep = EtiquetaPallet.builder()
                        .etiqueta(etiqueta)
                        .pallet(pallet)
                        .posicionEnPallet(i + 1)
                        .activo(true)
                        .build();

                pallet.getEtiquetas().add(ep);

                // Cambiar estado de etiqueta
                etiqueta.setEstadoEtiqueta("ASIGNADA_PALLET");
            }
        }

        Pallet saved = palletRepository.save(pallet);
        return mapper.toResponse(saved);
    }

    @Transactional
    public PalletResponse actualizar(Long id, PalletRequest request) {
        Pallet pallet = palletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pallet no encontrado con ID: " + id));

        // Validar código único si cambió
        if (!pallet.getCodigoPallet().equals(request.getCodigoPallet()) &&
                palletRepository.existsByCodigoPallet(request.getCodigoPallet())) {
            throw new IllegalArgumentException("Ya existe un pallet con el código: " + request.getCodigoPallet());
        }

        mapper.updateEntityFromRequest(pallet, request);
        Pallet updated = palletRepository.save(pallet);

        return mapper.toResponse(updated);
    }

    @Transactional
    public void eliminar(Long id) {
        Pallet pallet = palletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pallet no encontrado con ID: " + id));

        // Liberar etiquetas asociadas
        for (EtiquetaPallet ep : pallet.getEtiquetas()) {
            ep.getEtiqueta().setEstadoEtiqueta("DISPONIBLE");
        }

        pallet.setActivo(false);
        palletRepository.save(pallet);
    }

    @Transactional
    public PalletResponse cambiarEstado(Long id, String nuevoEstado) {
        Pallet pallet = palletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pallet no encontrado con ID: " + id));

        pallet.setEstadoPallet(nuevoEstado);
        Pallet updated = palletRepository.save(pallet);

        return mapper.toResponse(updated);
    }
}