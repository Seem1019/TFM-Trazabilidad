import { useMemo } from 'react';
import { useAuthStore } from '@/store/authStore';
import type { TipoRol } from '@/types';

/**
 * Tipos de operaciones CRUD
 */
export type Permission = 'create' | 'read' | 'update' | 'delete';

/**
 * Módulos del sistema según Anexo J
 */
export type Module =
  | 'dashboard'
  | 'fincas'
  | 'lotes'
  | 'cosechas'
  | 'certificaciones'
  | 'actividades'
  | 'recepciones'
  | 'clasificacion'
  | 'etiquetas'
  | 'pallets'
  | 'control-calidad'
  | 'envios'
  | 'eventos'
  | 'documentos'
  | 'trazabilidad'
  | 'usuarios';

/**
 * Matriz de permisos RBAC según Anexo J del TFM
 *
 * Tabla 109: Módulo Producción
 * Tabla 110: Módulo Empaque
 * Tabla 111: Módulo Logística
 * Tabla 112: Módulo Trazabilidad
 * Tabla 113: Administración
 */
const PERMISSIONS_MATRIX: Record<Module, Record<TipoRol, Permission[]>> = {
  // === PRINCIPAL ===
  dashboard: {
    ADMIN: ['read'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },

  // === PRODUCCIÓN (Tabla 109) ===
  fincas: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },
  lotes: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['create', 'read', 'update'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },
  cosechas: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['create', 'read', 'update'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },
  certificaciones: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['create', 'read', 'update'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },
  actividades: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['create', 'read', 'update'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },

  // === EMPAQUE (Tabla 110) ===
  recepciones: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['create', 'read', 'update', 'delete'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },
  clasificacion: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['create', 'read', 'update'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },
  etiquetas: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['create', 'read', 'update'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },
  pallets: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['create', 'read', 'update'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },
  'control-calidad': {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['create', 'read', 'update'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },

  // === LOGÍSTICA (Tabla 111) ===
  envios: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['create', 'read', 'update'],
    AUDITOR: ['read'],
  },
  eventos: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['create', 'read', 'update'],
    AUDITOR: ['read'],
  },
  documentos: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['create', 'read', 'update'],
    AUDITOR: ['read'],
  },

  // === TRAZABILIDAD (Tabla 112) ===
  trazabilidad: {
    ADMIN: ['read'],
    PRODUCTOR: ['read'],
    OPERADOR_PLANTA: ['read'],
    OPERADOR_LOGISTICA: ['read'],
    AUDITOR: ['read'],
  },

  // === ADMINISTRACIÓN (Tabla 113) ===
  usuarios: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    PRODUCTOR: [],
    OPERADOR_PLANTA: [],
    OPERADOR_LOGISTICA: [],
    AUDITOR: [],
  },
};

/**
 * Roles que pueden acceder a cada módulo (para navegación)
 */
export const MODULE_ACCESS: Record<Module, TipoRol[]> = {
  dashboard: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  fincas: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  lotes: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  cosechas: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  certificaciones: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  actividades: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  recepciones: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  clasificacion: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  etiquetas: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  pallets: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  'control-calidad': ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  envios: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  eventos: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  documentos: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  trazabilidad: ['ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR'],
  usuarios: ['ADMIN'],
};

/**
 * Hook para verificar permisos del usuario actual
 */
export function usePermissions() {
  const { user } = useAuthStore();

  const permissions = useMemo(() => {
    const userRole = user?.rol;

    /**
     * Verifica si el usuario tiene un permiso específico en un módulo
     */
    const hasPermission = (module: Module, permission: Permission): boolean => {
      if (!userRole) return false;
      const modulePermissions = PERMISSIONS_MATRIX[module]?.[userRole];
      return modulePermissions?.includes(permission) ?? false;
    };

    /**
     * Verifica si el usuario puede acceder a un módulo (tiene al menos permiso de lectura)
     */
    const canAccess = (module: Module): boolean => {
      if (!userRole) return false;
      return MODULE_ACCESS[module]?.includes(userRole) ?? false;
    };

    /**
     * Verifica si el usuario puede crear en un módulo
     */
    const canCreate = (module: Module): boolean => hasPermission(module, 'create');

    /**
     * Verifica si el usuario puede leer en un módulo
     */
    const canRead = (module: Module): boolean => hasPermission(module, 'read');

    /**
     * Verifica si el usuario puede actualizar en un módulo
     */
    const canUpdate = (module: Module): boolean => hasPermission(module, 'update');

    /**
     * Verifica si el usuario puede eliminar en un módulo
     */
    const canDelete = (module: Module): boolean => hasPermission(module, 'delete');

    /**
     * Obtiene todos los permisos del usuario para un módulo
     */
    const getModulePermissions = (module: Module): Permission[] => {
      if (!userRole) return [];
      return PERMISSIONS_MATRIX[module]?.[userRole] ?? [];
    };

    /**
     * Verifica si el usuario es administrador
     */
    const isAdmin = (): boolean => userRole === 'ADMIN';

    /**
     * Verifica si el usuario tiene alguno de los roles especificados
     */
    const hasRole = (...roles: TipoRol[]): boolean => {
      if (!userRole) return false;
      return roles.includes(userRole);
    };

    return {
      userRole,
      hasPermission,
      canAccess,
      canCreate,
      canRead,
      canUpdate,
      canDelete,
      getModulePermissions,
      isAdmin,
      hasRole,
    };
  }, [user?.rol]);

  return permissions;
}

/**
 * Obtiene los roles permitidos para una ruta específica
 */
export function getRouteAllowedRoles(path: string): TipoRol[] | undefined {
  // Mapeo de rutas a módulos
  const routeToModule: Record<string, Module> = {
    '/': 'dashboard',
    '/fincas': 'fincas',
    '/lotes': 'lotes',
    '/cosechas': 'cosechas',
    '/certificaciones': 'certificaciones',
    '/actividades': 'actividades',
    '/recepciones': 'recepciones',
    '/clasificacion': 'clasificacion',
    '/etiquetas': 'etiquetas',
    '/pallets': 'pallets',
    '/control-calidad': 'control-calidad',
    '/envios': 'envios',
    '/eventos': 'eventos',
    '/documentos': 'documentos',
    '/trazabilidad': 'trazabilidad',
    '/usuarios': 'usuarios',
  };

  const module = routeToModule[path];
  if (!module) return undefined;

  return MODULE_ACCESS[module];
}

export default usePermissions;
