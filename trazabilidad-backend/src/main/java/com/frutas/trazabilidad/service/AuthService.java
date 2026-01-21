package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.dto.LoginRequest;
import com.frutas.trazabilidad.dto.LoginResponse;
import com.frutas.trazabilidad.dto.PasswordResetConfirmRequest;
import com.frutas.trazabilidad.dto.PasswordResetRequest;
import com.frutas.trazabilidad.entity.PasswordResetToken;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.exception.ForbiddenException;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.exception.UnauthorizedException;
import com.frutas.trazabilidad.repository.PasswordResetTokenRepository;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.debug("Intento de login para email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        // Verificar bloqueo temporal por intentos fallidos
        if (user.estaBloqueadoTemporalmente()) {
            log.warn("Cuenta bloqueada temporalmente para usuario: {}", request.getEmail());
            throw new ForbiddenException("Cuenta bloqueada temporalmente por múltiples intentos fallidos. Intente más tarde.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Contraseña incorrecta para usuario: {}", request.getEmail());
            user.registrarIntentoFallido();
            userRepository.save(user);

            int intentosRestantes = 5 - (user.getIntentosFallidos() != null ? user.getIntentosFallidos() : 0);
            if (intentosRestantes > 0) {
                throw new UnauthorizedException("Credenciales inválidas. Intentos restantes: " + intentosRestantes);
            } else {
                throw new ForbiddenException("Cuenta bloqueada temporalmente por múltiples intentos fallidos. Intente más tarde.");
            }
        }

        if (!user.getActivo()) {
            log.warn("Intento de login con usuario inactivo: {}", request.getEmail());
            throw new ForbiddenException("Usuario inactivo. Contacte al administrador");
        }

        // Login exitoso: reiniciar intentos fallidos
        user.reiniciarIntentosFallidos();
        user.setUltimoAcceso(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        log.info("Login exitoso para usuario: {} - Empresa: {}", user.getEmail(), user.getEmpresa().getRazonSocial());

        return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nombre(user.getNombre())
                        .apellido(user.getApellido())
                        .rol(user.getRol())
                        .empresaId(user.getEmpresa().getId())
                        .empresaNombre(user.getEmpresa().getRazonSocial())
                        .build())
                .build();
    }

    /**
     * Genera un token de recuperación de contraseña y lo envía por email.
     * En producción, aquí se enviaría un email con el token.
     */
    @Transactional
    public String requestPasswordReset(PasswordResetRequest request) {
        log.info("Solicitud de recuperación de contraseña para email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un usuario con el email proporcionado"));

        if (!user.getActivo()) {
            throw new ForbiddenException("Usuario inactivo. Contacte al administrador");
        }

        // Eliminar tokens anteriores del usuario
        resetTokenRepository.deleteByUserId(user.getId());

        // Generar token único
        String token = UUID.randomUUID().toString();

        // Crear y guardar token (válido por 1 hora)
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        resetTokenRepository.save(resetToken);

        log.info("Token de recuperación generado para usuario: {}", user.getEmail());

        // TODO: En producción, enviar email con el token
        // emailService.sendPasswordResetEmail(user.getEmail(), token);

        return token; // En desarrollo retornamos el token directamente
    }

    /**
     * Confirma el cambio de contraseña usando el token.
     */
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        log.info("Confirmando cambio de contraseña con token");

        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Token de recuperación inválido"));

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException("Este token ya fue utilizado");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Este token ha expirado");
        }

        // Actualizar contraseña
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Marcar token como usado
        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        log.info("Contraseña actualizada exitosamente para usuario: {}", user.getEmail());
    }
}