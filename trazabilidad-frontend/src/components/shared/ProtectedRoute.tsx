import { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { usePermissions, type Module } from '@/hooks/usePermissions';
import type { TipoRol } from '@/types';

interface ProtectedRouteProps {
  children: React.ReactNode;
  /**
   * Roles permitidos (forma tradicional)
   */
  allowedRoles?: TipoRol[];
  /**
   * Módulo requerido (usa el sistema de permisos RBAC)
   */
  module?: Module;
}

/**
 * Mapeo de rutas a módulos para verificación automática
 */
const ROUTE_TO_MODULE: Record<string, Module> = {
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

/**
 * Obtiene el módulo correspondiente a una ruta
 */
function getModuleFromPath(pathname: string): Module | undefined {
  // Buscar coincidencia exacta primero
  if (ROUTE_TO_MODULE[pathname]) {
    return ROUTE_TO_MODULE[pathname];
  }

  // Buscar coincidencia por prefijo (para rutas como /fincas/123)
  for (const [route, module] of Object.entries(ROUTE_TO_MODULE)) {
    if (route !== '/' && pathname.startsWith(route)) {
      return module;
    }
  }

  return undefined;
}

export function ProtectedRoute({ children, allowedRoles, module }: ProtectedRouteProps) {
  const { isAuthenticated, user, sessionExpired, checkAuth } = useAuthStore();
  const { canAccess } = usePermissions();
  const location = useLocation();

  // Verificar autenticación cuando el componente se monta o la ruta cambia
  useEffect(() => {
    checkAuth();
  }, [location.pathname, checkAuth]);

  // Si la sesión expiró o no está autenticado, redirigir al login
  if (!isAuthenticated || sessionExpired) {
    // Redirigir al login guardando la ubicación actual
    return <Navigate to="/login" state={{ from: location, sessionExpired }} replace />;
  }

  // Verificar permisos por roles específicos (forma tradicional)
  if (allowedRoles && user && !allowedRoles.includes(user.rol)) {
    // Usuario autenticado pero sin permisos
    return <Navigate to="/" replace />;
  }

  // Verificar permisos por módulo (si se especifica)
  if (module && !canAccess(module)) {
    return <Navigate to="/" replace />;
  }

  // Verificación automática basada en la ruta actual
  const currentModule = getModuleFromPath(location.pathname);
  if (currentModule && !canAccess(currentModule)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}

export default ProtectedRoute;
