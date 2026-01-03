package com.frutas.trazabilidad.module.empaque.service;

import com.frutas.trazabilidad.module.empaque.dto.ControlCalidadRequest;
import com.frutas.trazabilidad.module.empaque.dto.ControlCalidadResponse;
import com.frutas.trazabilidad.module.empaque.entity.Clasificacion;
import com.frutas.trazabilidad.module.empaque.entity.ControlCalidad;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import com.frutas.trazabilidad.module.empaque.mapper.ControlCalidadMapper;
import com.frutas.trazabilidad.module.empaque.repository.ClasificacionRepository;
import com.frutas.trazabilidad.module.empaque.repository.ControlCalidadRepository;
import com.frutas.trazabilidad.module.empaque.repository.PalletRepository;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de controles de calidad.
 */
@Service
@RequiredArgsConstructor
public class ControlCalidadService {

    private final ControlCalidadRepository controlRepository;
    private final ClasificacionRepository clasificacionRepository;
    private final PalletRepository palletRepository;
    private final ControlCalidadMapper mapper;

    @Transactional(readOnly = true)
    public List<ControlCalidadResponse> listarPorEmpresa(Long empresaId) {
        return controlRepository.findByEmpresaId(empresaId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ControlCalidadResponse> listarPorClasificacion(Long clasificacionId, Long empresaId) {
        validarClasificacionPertenencia(clasificacionId, empresaId);
        return controlRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(clasificacionId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ControlCalidadResponse> listarPorPallet(Long palletId) {
        return controlRepository.findByPalletIdAndActivoTrueOrderByFechaControlDesc(palletId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ControlCalidadResponse> listarPorTipo(Long empresaId, String tipo) {
        return controlRepository.findByEmpresaIdAndTipo(empresaId, tipo).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ControlCalidadResponse> listarPorResultado(Long empresaId, String resultado) {
        return controlRepository.findByEmpresaIdAndResultado(empresaId, resultado).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ControlCalidadResponse> listarPorRangoFechas(Long empresaId, LocalDate desde, LocalDate hasta) {
        return controlRepository.findByEmpresaIdAndFechaBetween(empresaId, desde, hasta).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ControlCalidadResponse buscarPorId(Long id, Long empresaId) {
        ControlCalidad control = controlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Control de calidad no encontrado con ID: " + id));

        validarPertenenciaEmpresa(control, empresaId);
        return mapper.toResponse(control);
    }

    @Transactional
    public ControlCalidadResponse crear(ControlCalidadRequest request, Long empresaId) {
        // Validar código único
        if (controlRepository.existsByCodigoControl(request.getCodigoControl())) {
            throw new IllegalArgumentException("Ya existe un control con el código: " + request.getCodigoControl());
        }

        // Validar que al menos uno esté presente (clasificación o pallet)
        if (request.getClasificacionId() == null && request.getPalletId() == null) {
            throw new IllegalArgumentException("Debe especificar clasificación o pallet para el control");
        }

        Clasificacion clasificacion = null;
        Pallet pallet = null;

        if (request.getClasificacionId() != null) {
            clasificacion = clasificacionRepository.findById(request.getClasificacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con ID: " + request.getClasificacionId()));
            validarClasificacionPertenencia(clasificacion.getId(), empresaId);
        }

        if (request.getPalletId() != null) {
            pallet = palletRepository.findById(request.getPalletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pallet no encontrado con ID: " + request.getPalletId()));
        }

        ControlCalidad control = mapper.toEntity(request, clasificacion, pallet);
        ControlCalidad saved = controlRepository.save(control);

        return mapper.toResponse(saved);
    }

    @Transactional
    public ControlCalidadResponse actualizar(Long id, ControlCalidadRequest request, Long empresaId) {
        ControlCalidad control = controlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Control de calidad no encontrado con ID: " + id));

        validarPertenenciaEmpresa(control, empresaId);

        // Validar código único si cambió
        if (!control.getCodigoControl().equals(request.getCodigoControl()) &&
                controlRepository.existsByCodigoControl(request.getCodigoControl())) {
            throw new IllegalArgumentException("Ya existe un control con el código: " + request.getCodigoControl());
        }

        Clasificacion clasificacion = null;
        Pallet pallet = null;

        if (request.getClasificacionId() != null) {
            clasificacion = clasificacionRepository.findById(request.getClasificacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con ID: " + request.getClasificacionId()));
            validarClasificacionPertenencia(clasificacion.getId(), empresaId);
        }

        if (request.getPalletId() != null) {
            pallet = palletRepository.findById(request.getPalletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pallet no encontrado con ID: " + request.getPalletId()));
        }

        mapper.updateEntityFromRequest(control, request, clasificacion, pallet);
        ControlCalidad updated = controlRepository.save(control);

        return mapper.toResponse(updated);
    }

    @Transactional
    public void eliminar(Long id, Long empresaId) {
        ControlCalidad control = controlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Control de calidad no encontrado con ID: " + id));

        validarPertenenciaEmpresa(control, empresaId);

        control.setActivo(false);
        controlRepository.save(control);
    }

    private void validarClasificacionPertenencia(Long clasificacionId, Long empresaId) {
        if (!clasificacionRepository.existsByIdAndEmpresaId(clasificacionId, empresaId)) {
            throw new IllegalArgumentException("La clasificación no pertenece a la empresa del usuario");
        }
    }

    private void validarPertenenciaEmpresa(ControlCalidad control, Long empresaId) {
        if (control.getClasificacion() != null &&
                !control.getClasificacion().getRecepcion().getLote().getFinca().getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El control no pertenece a la empresa del usuario");
        }
    }
}