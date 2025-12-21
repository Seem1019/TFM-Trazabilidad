package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.dto.LoginRequest;
import com.frutas.trazabilidad.dto.LoginResponse;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.exception.ForbiddenException;
import com.frutas.trazabilidad.exception.UnauthorizedException;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.debug("Intento de login para email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Contraseña incorrecta para usuario: {}", request.getEmail());
            throw new UnauthorizedException("Credenciales inválidas");
        }

        if (!user.getActivo()) {
            log.warn("Intento de login con usuario inactivo: {}", request.getEmail());
            throw new ForbiddenException("Usuario inactivo. Contacte al administrador");
        }

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
}