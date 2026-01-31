package com.frutas.trazabilidad.module.empaque.service;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.module.empaque.dto.PalletRequest;
import com.frutas.trazabilidad.module.empaque.dto.PalletResponse;
import com.frutas.trazabilidad.module.empaque.entity.Etiqueta;
import com.frutas.trazabilidad.module.empaque.entity.EtiquetaPallet;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import com.frutas.trazabilidad.module.empaque.mapper.PalletMapper;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaRepository;
import com.frutas.trazabilidad.module.empaque.repository.PalletRepository;
import com.frutas.trazabilidad.repository.EmpresaRepository;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de pallets con aislamiento multitenant.
 * Todos los métodos requieren empresaId para garantizar seguridad de datos.
 */
@Service
@RequiredArgsConstructor
public class PalletService {

    private final PalletRepository palletRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final EmpresaRepository empresaRepository;
    private final PalletMapper mapper;

    /**
     * Lista todos los pallets activos de una empresa.
     */
    @Transactional(readOnly = true)
    public List<PalletResponse> listarPorEmpresa(Long empresaId) {
        return palletRepository.findByEmpresaIdAndActivoTrueOrderByFechaPaletizadoDesc(empresaId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista pallets por estado dentro de una empresa.
     */
    @Transactional(readOnly = true)
    public List<PalletResponse> listarPorEstado(Long empresaId, String estado) {
        return palletRepository.findByEmpresaIdAndEstadoPalletAndActivoTrue(empresaId, estado).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista pallets por destino dentro de una empresa.
     */
    @Transactional(readOnly = true)
    public List<PalletResponse> listarPorDestino(Long empresaId, String destino) {
        return palletRepository.findByEmpresaIdAndDestinoContainingIgnoreCaseAndActivoTrue(empresaId, destino).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista pallets por tipo de fruta dentro de una empresa.
     */
    @Transactional(readOnly = true)
    public List<PalletResponse> listarPorTipoFruta(Long empresaId, String tipoFruta) {
        return palletRepository.findByEmpresaIdAndTipoFrutaAndActivoTrue(empresaId, tipoFruta).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista pallets listos para envío dentro de una empresa.
     */
    @Transactional(readOnly = true)
    public List<PalletResponse> listarListosParaEnvio(Long empresaId) {
        return palletRepository.findPalletsListosParaEnvio(empresaId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca un pallet por ID, validando pertenencia a la empresa.
     */
    @Transactional(readOnly = true)
    public PalletResponse buscarPorId(Long id, Long empresaId) {
        Pallet pallet = palletRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pallet", id));

        return mapper.toResponse(pallet);
    }

    /**
     * Crea un nuevo pallet asignándolo a la empresa.
     */
    @Transactional
    public PalletResponse crear(PalletRequest request, Long empresaId) {
        // Validar código único dentro de la empresa
        if (palletRepository.existsByCodigoPalletAndEmpresaId(request.getCodigoPallet(), empresaId)) {
            throw new IllegalArgumentException("Ya existe un pallet con el código: " + request.getCodigoPallet());
        }

        // Obtener empresa
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));

        Pallet pallet = mapper.toEntity(request);
        pallet.setEmpresa(empresa);

        // Asociar etiquetas si se proporcionaron
        if (request.getEtiquetasIds() != null && !request.getEtiquetasIds().isEmpty()) {
            for (int i = 0; i < request.getEtiquetasIds().size(); i++) {
                Long etiquetaId = request.getEtiquetasIds().get(i);

                // Buscar etiqueta validando que pertenezca a la misma empresa
                Etiqueta etiqueta = etiquetaRepository.findByIdAndEmpresaId(etiquetaId, empresaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", etiquetaId));

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

    /**
     * Actualiza un pallet existente, validando pertenencia a la empresa.
     */
    @Transactional
    public PalletResponse actualizar(Long id, PalletRequest request, Long empresaId) {
        Pallet pallet = palletRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pallet", id));

        // Validar código único si cambió (dentro de la empresa)
        if (!pallet.getCodigoPallet().equals(request.getCodigoPallet()) &&
                palletRepository.existsByCodigoPalletAndEmpresaId(request.getCodigoPallet(), empresaId)) {
            throw new IllegalArgumentException("Ya existe un pallet con el código: " + request.getCodigoPallet());
        }

        mapper.updateEntityFromRequest(pallet, request);
        Pallet updated = palletRepository.save(pallet);

        return mapper.toResponse(updated);
    }

    /**
     * Elimina (desactiva) un pallet, validando pertenencia a la empresa.
     */
    @Transactional
    public void eliminar(Long id, Long empresaId) {
        Pallet pallet = palletRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pallet", id));

        // Liberar etiquetas asociadas
        for (EtiquetaPallet ep : pallet.getEtiquetas()) {
            ep.getEtiqueta().setEstadoEtiqueta("DISPONIBLE");
        }

        pallet.setActivo(false);
        palletRepository.save(pallet);
    }

    /**
     * Cambia el estado de un pallet, validando pertenencia a la empresa.
     */
    @Transactional
    public PalletResponse cambiarEstado(Long id, String nuevoEstado, Long empresaId) {
        Pallet pallet = palletRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pallet", id));

        pallet.setEstadoPallet(nuevoEstado);
        Pallet updated = palletRepository.save(pallet);

        return mapper.toResponse(updated);
    }
}
