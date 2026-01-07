package com.frutas.trazabilidad.mapper;

import com.frutas.trazabilidad.dto.UserRequest;
import com.frutas.trazabilidad.dto.UserResponse;
import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre User entity y DTOs.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .telefono(user.getTelefono())
                .rol(user.getRol())
                .activo(user.getActivo())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .ultimoAcceso(user.getUltimoAcceso())
                .empresaId(user.getEmpresa().getId())
                .empresaNombre(user.getEmpresa().getNombreComercial())
                .build();
    }

    public User toEntity(UserRequest request, Empresa empresa, String passwordHash) {
        return User.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .rol(request.getRol())
                .activo(request.getActivo())
                .empresa(empresa)
                .build();
    }

    public void updateEntityFromRequest(User user, UserRequest request, String passwordHash) {
        user.setEmail(request.getEmail());

        // Solo actualizar password si se proporcionó uno nuevo
        if (passwordHash != null) {
            user.setPasswordHash(passwordHash);
        }

        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setTelefono(request.getTelefono());
        user.setRol(request.getRol());
        user.setActivo(request.getActivo());
    }
}
