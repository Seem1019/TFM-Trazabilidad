import { usePermissions, type Module, type Permission } from '@/hooks/usePermissions';
import type { TipoRol } from '@/types';

interface PermissionGateProps {
  children: React.ReactNode;
  /**
   * Módulo a verificar
   */
  module?: Module;
  /**
   * Permiso requerido (create, read, update, delete)
   */
  permission?: Permission;
  /**
   * Roles permitidos (alternativa a module+permission)
   */
  roles?: TipoRol[];
  /**
   * Si es true, muestra el fallback en lugar de ocultar
   */
  fallback?: React.ReactNode;
}

/**
 * Componente que controla la visibilidad de elementos según permisos RBAC.
 *
 * @example
 * // Mostrar solo si puede crear en el módulo 'lotes'
 * <PermissionGate module="lotes" permission="create">
 *   <Button>Crear Lote</Button>
 * </PermissionGate>
 *
 * @example
 * // Mostrar solo para roles específicos
 * <PermissionGate roles={['ADMIN', 'PRODUCTOR']}>
 *   <Button>Acción especial</Button>
 * </PermissionGate>
 *
 * @example
 * // Con fallback
 * <PermissionGate module="usuarios" permission="delete" fallback={<span>Sin permisos</span>}>
 *   <Button variant="destructive">Eliminar</Button>
 * </PermissionGate>
 */
export function PermissionGate({
  children,
  module,
  permission,
  roles,
  fallback = null,
}: PermissionGateProps) {
  const { hasPermission, hasRole, canAccess } = usePermissions();

  // Si se especifican roles, verificar si el usuario tiene alguno
  if (roles && roles.length > 0) {
    if (!hasRole(...roles)) {
      return <>{fallback}</>;
    }
    return <>{children}</>;
  }

  // Si se especifica módulo y permiso, verificar permiso específico
  if (module && permission) {
    if (!hasPermission(module, permission)) {
      return <>{fallback}</>;
    }
    return <>{children}</>;
  }

  // Si solo se especifica módulo, verificar acceso general (lectura)
  if (module) {
    if (!canAccess(module)) {
      return <>{fallback}</>;
    }
    return <>{children}</>;
  }

  // Sin restricciones, mostrar children
  return <>{children}</>;
}

/**
 * HOC para envolver componentes con verificación de permisos
 */
export function withPermission<P extends object>(
  Component: React.ComponentType<P>,
  module: Module,
  permission: Permission
) {
  return function WrappedComponent(props: P) {
    return (
      <PermissionGate module={module} permission={permission}>
        <Component {...props} />
      </PermissionGate>
    );
  };
}

export default PermissionGate;
