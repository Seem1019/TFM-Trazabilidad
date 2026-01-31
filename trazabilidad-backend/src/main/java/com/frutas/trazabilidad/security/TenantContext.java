package com.frutas.trazabilidad.security;

import com.frutas.trazabilidad.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Componente para acceder de forma segura al contexto del tenant (empresa) actual.
 * Proporciona métodos helper para obtener información del usuario autenticado
 * y su empresa de forma centralizada.
 */
@Component
public class TenantContext {

    /**
     * Obtiene el usuario autenticado actual.
     *
     * @return User autenticado o null si no hay autenticación
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }

        return null;
    }

    /**
     * Obtiene el ID de la empresa del usuario autenticado.
     *
     * @return ID de la empresa o null si no hay autenticación
     */
    public Long getCurrentEmpresaId() {
        User user = getCurrentUser();
        return user != null ? user.getEmpresa().getId() : null;
    }

    /**
     * Obtiene el ID del usuario autenticado.
     *
     * @return ID del usuario o null si no hay autenticación
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Verifica si el usuario autenticado tiene un rol específico.
     *
     * @param role Nombre del rol (sin prefijo ROLE_)
     * @return true si tiene el rol
     */
    public boolean hasRole(String role) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }
        return user.getRol().name().equals(role);
    }

    /**
     * Verifica si el usuario autenticado tiene alguno de los roles especificados.
     *
     * @param roles Nombres de roles (sin prefijo ROLE_)
     * @return true si tiene al menos uno de los roles
     */
    public boolean hasAnyRole(String... roles) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }
        String userRole = user.getRol().name();
        for (String role : roles) {
            if (userRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si hay un usuario autenticado.
     *
     * @return true si hay autenticación válida
     */
    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    /**
     * Obtiene el email del usuario autenticado.
     *
     * @return Email del usuario o null
     */
    public String getCurrentUserEmail() {
        User user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
}
