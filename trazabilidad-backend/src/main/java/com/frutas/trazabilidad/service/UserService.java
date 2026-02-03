package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.dto.UserRequest;
import com.frutas.trazabilidad.dto.UserResponse;
import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.mapper.UserMapper;
import com.frutas.trazabilidad.module.logistica.service.AuditoriaEventoService;
import com.frutas.trazabilidad.repository.EmpresaRepository;
import com.frutas.trazabilidad.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de usuarios del sistema.
 * Implementa aislamiento multiempresa: cada admin solo puede gestionar usuarios de su empresa.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaEventoService auditoriaService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<UserResponse> listarPorEmpresa(Long empresaId) {
        log.info("Listando usuarios de empresa ID: {}", empresaId);
        return userRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listarTodosPorEmpresa(Long empresaId) {
        log.info("Listando todos los usuarios (activos e inactivos) de empresa ID: {}", empresaId);
        return userRepository.findByEmpresaId(empresaId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse buscarPorId(Long id, Long empresaId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        validarPertenenciaEmpresa(user, empresaId);
        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse crear(UserRequest request, Long empresaId) {
        log.info("Creando nuevo usuario: {} para empresa ID: {}", request.getEmail(), empresaId);

        // Validar que no exista el email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail());
        }

        // Validar que se proporcionó password
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear un usuario");
        }

        // Obtener empresa
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + empresaId));

        // Hash de la contraseña
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Crear usuario
        User user = mapper.toEntity(request, empresa, passwordHash);
        User saved = userRepository.save(user);

        log.info("Usuario creado exitosamente con ID: {}", saved.getId());

        // Publicar evento para auditoría después de la transacción
        eventPublisher.publishEvent(new UserAuditEvent(this, "CREATE", saved.getId(), saved.getEmail(), obtenerUsuarioActualId()));

        return mapper.toResponse(saved);
    }

    @Transactional
    public UserResponse actualizar(Long id, UserRequest request, Long empresaId) {
        log.info("Actualizando usuario ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        validarPertenenciaEmpresa(user, empresaId);

        // Validar email único si cambió
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail());
        }

        // Hash de la contraseña solo si se proporcionó una nueva
        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordHash = passwordEncoder.encode(request.getPassword());
        }

        mapper.updateEntityFromRequest(user, request, passwordHash);
        User updated = userRepository.save(user);

        log.info("Usuario actualizado exitosamente: {}", updated.getId());

        // Publicar evento para auditoría después de la transacción
        eventPublisher.publishEvent(new UserAuditEvent(this, "UPDATE", updated.getId(), updated.getEmail(), obtenerUsuarioActualId()));

        return mapper.toResponse(updated);
    }

    @Transactional
    public void eliminar(Long id, Long empresaId) {
        log.info("Eliminando usuario ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        validarPertenenciaEmpresa(user, empresaId);

        // Soft delete
        user.setActivo(false);
        userRepository.save(user);

        log.info("Usuario eliminado (desactivado) exitosamente: {}", id);

        // Publicar evento para auditoría después de la transacción
        eventPublisher.publishEvent(new UserAuditEvent(this, "DELETE", user.getId(), user.getEmail(), obtenerUsuarioActualId()));
    }

    @Transactional
    public UserResponse cambiarEstado(Long id, Boolean nuevoEstado, Long empresaId) {
        log.info("Cambiando estado de usuario ID: {} a {}", id, nuevoEstado);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        validarPertenenciaEmpresa(user, empresaId);

        user.setActivo(nuevoEstado);
        User updated = userRepository.save(user);

        log.info("Estado de usuario cambiado exitosamente");

        // Publicar evento para auditoría después de la transacción
        eventPublisher.publishEvent(new UserAuditEvent(this, "UPDATE", updated.getId(), updated.getEmail(), obtenerUsuarioActualId()));

        return mapper.toResponse(updated);
    }

    private void validarPertenenciaEmpresa(User user, Long empresaId) {
        if (!user.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El usuario no pertenece a la empresa del administrador");
        }
    }

    private Long obtenerUsuarioActualId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                return userRepository.findByEmail(email).map(User::getId).orElse(null);
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener ID del usuario actual: {}", e.getMessage());
        }
        return null;
    }

    // ========== EVENTO DE AUDITORÍA DE USUARIOS ==========

    /**
     * Evento interno para auditoría de usuarios.
     * Se procesa después de que la transacción se complete exitosamente.
     */
    public record UserAuditEvent(Object source, String operacion, Long userId, String userEmail, Long ejecutorId) {
    }
}
