package com.frutas.trazabilidad.entity;

/**
 * Roles disponibles en el sistema.
 * Implementa control de acceso basado en roles (RBAC).
 */
public enum TipoRol {
    ADMIN,              // Administrador de empresa (acceso completo)
    PRODUCTOR,          // Gestión de fincas y producción
    OPERADOR_PLANTA,    // Recepción y empaque
    OPERADOR_LOGISTICA, // Gestión de envíos y logística
    AUDITOR             // Solo lectura para auditorías
}