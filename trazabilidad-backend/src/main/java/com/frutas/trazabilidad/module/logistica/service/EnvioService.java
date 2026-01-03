package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import com.frutas.trazabilidad.module.empaque.repository.PalletRepository;
import com.frutas.trazabilidad.module.logistica.dto.EnvioRequest;
import com.frutas.trazabilidad.module.logistica.dto.EnvioResponse;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.mapper.EnvioMapper;
import com.frutas.trazabilidad.module.logistica.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service para gestión de envíos/exportaciones.
 */
@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final PalletRepository palletRepository;
    private final UserRepository userRepository;
    private final EnvioMapper envioMapper;
    private final AuditoriaEventoService auditoriaService;

    /**
     * Crear un nuevo envío.
     */
    @Transactional
    public EnvioResponse crear(EnvioRequest request) {
        // Validar que el código no exista
        if (envioRepository.existsByCodigoEnvio(request.getCodigoEnvio())) {
            throw new IllegalArgumentException("Ya existe un envío con el código: " + request.getCodigoEnvio());
        }

        // Obtener usuario autenticado
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear entidad
        Envio envio = envioMapper.toEntity(request, usuario);

        // Asignar pallets si se proporcionan
        if (request.getPalletsIds() != null && !request.getPalletsIds().isEmpty()) {
            asignarPallets(envio, request.getPalletsIds(), usuario.getEmpresa().getId());
        }

        // Calcular totales
        calcularTotales(envio);

        // Guardar
        envio = envioRepository.save(envio);

        // Auditar
        auditoriaService.registrarCreacion(
                "ENVIO",
                envio.getId(),
                envio.getCodigoEnvio(),
                "Creación de envío " + envio.getCodigoEnvio(),
                usuario
        );

        return envioMapper.toResponse(envio);
    }

    /**
     * Actualizar un envío existente.
     */
    @Transactional
    public EnvioResponse actualizar(Long id, EnvioRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + id));

        // Validar que pertenece a la empresa
        if (!envioRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para modificar este envío");
        }

        // Validar que no esté cerrado
        if (envio.estaCerrado()) {
            throw new IllegalStateException("No se puede modificar un envío cerrado");
        }

        // Actualizar datos
        String datosAnteriores = capturarDatosEnvio(envio);
        envioMapper.updateEntity(envio, request);
        calcularTotales(envio);
        envio = envioRepository.save(envio);

        // Auditar
        auditoriaService.registrarActualizacion(
                "ENVIO",
                envio.getId(),
                envio.getCodigoEnvio(),
                "Actualización de envío " + envio.getCodigoEnvio(),
                datosAnteriores,
                capturarDatosEnvio(envio),
                usuario
        );

        return envioMapper.toResponse(envio);
    }

    /**
     * Asignar pallets a un envío.
     */
    @Transactional
    public EnvioResponse asignarPallets(Long envioId, List<Long> palletsIds) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + envioId));

        if (!envioRepository.existsByIdAndEmpresaId(envioId, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para modificar este envío");
        }

        if (envio.estaCerrado()) {
            throw new IllegalStateException("No se puede modificar un envío cerrado");
        }

        asignarPallets(envio, palletsIds, usuario.getEmpresa().getId());
        calcularTotales(envio);
        envio = envioRepository.save(envio);

        auditoriaService.registrarActualizacion(
                "ENVIO",
                envio.getId(),
                envio.getCodigoEnvio(),
                "Asignación de " + palletsIds.size() + " pallets al envío",
                null,
                "Pallets: " + palletsIds,
                usuario
        );

        return envioMapper.toResponse(envio);
    }

    /**
     * Cambiar estado de un envío.
     */
    @Transactional
    public EnvioResponse cambiarEstado(Long id, String nuevoEstado) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + id));

        if (!envioRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para modificar este envío");
        }

        String estadoAnterior = envio.getEstado();
        envio.setEstado(nuevoEstado);

        // Si el estado es EN_TRANSITO, registrar fecha de salida real
        if ("EN_TRANSITO".equals(nuevoEstado) && envio.getFechaSalidaReal() == null) {
            envio.setFechaSalidaReal(LocalDate.now());
        }

        envio = envioRepository.save(envio);

        auditoriaService.registrarActualizacion(
                "ENVIO",
                envio.getId(),
                envio.getCodigoEnvio(),
                "Cambio de estado: " + estadoAnterior + " → " + nuevoEstado,
                estadoAnterior,
                nuevoEstado,
                usuario
        );

        return envioMapper.toResponse(envio);
    }

    /**
     * Cerrar un envío (genera hash SHA-256 blockchain).
     */
    @Transactional
    public EnvioResponse cerrar(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + id));

        if (!envioRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para cerrar este envío");
        }

        if (envio.estaCerrado()) {
            throw new IllegalStateException("El envío ya está cerrado");
        }

        // Validar que tenga pallets asignados
        if (envio.getPallets() == null || envio.getPallets().isEmpty()) {
            throw new IllegalStateException("El envío debe tener al menos un pallet asignado");
        }

        // Generar hash SHA-256
        String datosParaHash = generarDatosParaHash(envio);
        String hash = generarHashSHA256(datosParaHash);

        envio.setEstado("CERRADO");
        envio.setFechaCierre(LocalDateTime.now());
        envio.setUsuarioCierre(usuario);
        envio.setHashCierre(hash);

        envio = envioRepository.save(envio);

        // Registrar en auditoría con criticidad CRITICAL y encadenamiento
        auditoriaService.registrarCierreEnvio(envio, usuario);

        return envioMapper.toResponse(envio);
    }

    /**
     * Listar envíos por empresa.
     */
    @Transactional(readOnly = true)
    public List<EnvioResponse> listarPorEmpresa() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return envioRepository.findByEmpresaId(usuario.getEmpresa().getId())
                .stream()
                .map(envioMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Listar envíos por estado.
     */
    @Transactional(readOnly = true)
    public List<EnvioResponse> listarPorEstado(String estado) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return envioRepository.findByEmpresaIdAndEstado(usuario.getEmpresa().getId(), estado)
                .stream()
                .map(envioMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener envío por ID.
     */
    @Transactional(readOnly = true)
    public EnvioResponse obtenerPorId(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + id));

        if (!envioRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para ver este envío");
        }

        return envioMapper.toResponse(envio);
    }

    /**
     * Eliminar envío (soft delete).
     */
    @Transactional
    public void eliminar(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + id));

        if (!envioRepository.existsByIdAndEmpresaId(id, usuario.getEmpresa().getId())) {
            throw new RuntimeException("No tiene permisos para eliminar este envío");
        }

        if (envio.estaCerrado()) {
            throw new IllegalStateException("No se puede eliminar un envío cerrado");
        }

        envio.setActivo(false);
        envioRepository.save(envio);

        auditoriaService.registrarEliminacion(
                "ENVIO",
                envio.getId(),
                envio.getCodigoEnvio(),
                "Eliminación de envío " + envio.getCodigoEnvio(),
                usuario
        );
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void asignarPallets(Envio envio, List<Long> palletsIds, Long empresaId) {
        for (Long palletId : palletsIds) {
            Pallet pallet = palletRepository.findById(palletId)
                    .orElseThrow(() -> new RuntimeException("Pallet no encontrado con ID: " + palletId));

            // Validar que el pallet esté disponible
            if (!"ARMADO".equals(pallet.getEstadoPallet()) && !"EN_CAMARA".equals(pallet.getEstadoPallet())) {
                throw new IllegalStateException("El pallet " + pallet.getCodigoPallet() + " no está disponible para asignación");
            }

            // Asignar al envío
            pallet.setEnvio(envio);
            pallet.setEstadoPallet("ASIGNADO_ENVIO");
            palletRepository.save(pallet);
        }
    }

    private void calcularTotales(Envio envio) {
        if (envio.getPallets() != null && !envio.getPallets().isEmpty()) {
            double pesoNeto = envio.getPallets().stream()
                    .mapToDouble(p -> p.getPesoNetoTotal() != null ? p.getPesoNetoTotal() : 0.0)
                    .sum();
            double pesoBruto = envio.getPallets().stream()
                    .mapToDouble(p -> p.getPesoBrutoTotal() != null ? p.getPesoBrutoTotal() : 0.0)
                    .sum();
            int totalCajas = envio.getPallets().stream()
                    .mapToInt(p -> p.getNumeroCajas() != null ? p.getNumeroCajas() : 0)
                    .sum();

            envio.setPesoNetoTotal(pesoNeto);
            envio.setPesoBrutoTotal(pesoBruto);
            envio.setNumeroPallets(envio.getPallets().size());
            envio.setNumeroCajas(totalCajas);
        }
    }

    private String generarDatosParaHash(Envio envio) {
        return String.format(
                "ENVIO:%s|EMPRESA:%d|PALLETS:%d|PESO:%f|FECHA:%s|ESTADO:%s",
                envio.getCodigoEnvio(),
                envio.getUsuario().getEmpresa().getId(),
                envio.getNumeroPallets(),
                envio.getPesoNetoTotal(),
                LocalDateTime.now(),
                envio.getEstado()
        );
    }

    private String generarHashSHA256(String datos) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datos.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA-256", e);
        }
    }

    private String capturarDatosEnvio(Envio envio) {
        return String.format(
                "{codigo:'%s',destino:'%s',estado:'%s',pallets:%d}",
                envio.getCodigoEnvio(),
                envio.getPaisDestino(),
                envio.getEstado(),
                envio.getNumeroPallets() != null ? envio.getNumeroPallets() : 0
        );
    }
}