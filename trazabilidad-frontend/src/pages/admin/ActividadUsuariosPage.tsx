import { useCallback } from 'react';
import { format, formatDistanceToNow } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Activity,
  Shield,
  Clock,
  AlertTriangle,
  Wifi,
  WifiOff,
  Lock,
  UserCheck,
  UserX,
} from 'lucide-react';
import { auditoriaService } from '@/services/auditoriaService';
import type { UserActivity } from '@/services/auditoriaService';
import { useFetch } from '@/hooks/useFetch';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { DataTable, type Column, PageLoader } from '@/components/shared';
import { ROLES_LABELS } from '@/types';

export function ActividadUsuariosPage() {
  const {
    data: actividad,
    isLoading,
    error,
    refetch,
  } = useFetch<UserActivity[]>(
    useCallback(() => auditoriaService.obtenerActividadUsuarios(), []),
    []
  );

  const formatDateTime = (dateStr?: string | null) => {
    if (!dateStr) return 'Nunca';
    try {
      return format(new Date(dateStr), "dd MMM yyyy 'a las' HH:mm", { locale: es });
    } catch {
      return dateStr;
    }
  };

  const formatTimeAgo = (dateStr?: string | null) => {
    if (!dateStr) return null;
    try {
      return formatDistanceToNow(new Date(dateStr), { addSuffix: true, locale: es });
    } catch {
      return null;
    }
  };

  const getRolBadgeVariant = (rol: string) => {
    switch (rol) {
      case 'ADMIN': return 'default';
      case 'PRODUCTOR': return 'success';
      case 'OPERADOR_PLANTA': return 'secondary';
      case 'OPERADOR_LOGISTICA': return 'outline';
      case 'AUDITOR': return 'warning';
      default: return 'secondary';
    }
  };

  const columns: Column<UserActivity>[] = [
    {
      key: 'nombre',
      header: 'Usuario',
      render: (u) => (
        <div className="flex flex-col">
          <span className="font-medium">{u.nombre} {u.apellido}</span>
          <span className="text-sm text-muted-foreground">{u.email}</span>
        </div>
      ),
    },
    {
      key: 'rol',
      header: 'Rol',
      render: (u) => (
        <Badge variant={getRolBadgeVariant(u.rol)} className="gap-1">
          <Shield className="h-3 w-3" />
          {ROLES_LABELS[u.rol as keyof typeof ROLES_LABELS] || u.rol}
        </Badge>
      ),
    },
    {
      key: 'estado',
      header: 'Estado',
      render: (u) => {
        if (u.bloqueado) {
          return (
            <Badge variant="destructive" className="gap-1">
              <Lock className="h-3 w-3" />
              Bloqueado
            </Badge>
          );
        }
        return u.activo ? (
          <Badge variant="success" className="gap-1">
            <UserCheck className="h-3 w-3" />
            Activo
          </Badge>
        ) : (
          <Badge variant="secondary" className="gap-1">
            <UserX className="h-3 w-3" />
            Inactivo
          </Badge>
        );
      },
    },
    {
      key: 'ultimoAcceso',
      header: 'Último acceso',
      render: (u) => {
        const timeAgo = formatTimeAgo(u.ultimoAcceso);
        return (
          <div className="flex flex-col">
            <div className="flex items-center gap-1 text-sm">
              <Clock className="h-3 w-3 text-muted-foreground" />
              {formatDateTime(u.ultimoAcceso)}
            </div>
            {timeAgo && (
              <span className="text-xs text-muted-foreground">{timeAgo}</span>
            )}
          </div>
        );
      },
    },
    {
      key: 'sesionesActivas',
      header: 'Sesiones',
      render: (u) => (
        <div className="flex items-center gap-1">
          {u.sesionesActivas > 0 ? (
            <Wifi className="h-4 w-4 text-green-500" />
          ) : (
            <WifiOff className="h-4 w-4 text-muted-foreground" />
          )}
          <span className="text-sm font-medium">{u.sesionesActivas}</span>
        </div>
      ),
    },
    {
      key: 'intentosFallidos',
      header: 'Intentos fallidos',
      render: (u) => {
        if (!u.intentosFallidos || u.intentosFallidos === 0) {
          return <span className="text-sm text-muted-foreground">0</span>;
        }
        return (
          <div className="flex items-center gap-1">
            <AlertTriangle className={`h-4 w-4 ${u.intentosFallidos >= 3 ? 'text-red-500' : 'text-yellow-500'}`} />
            <span className="text-sm font-medium">{u.intentosFallidos}</span>
            {u.bloqueadoHasta && (
              <span className="text-xs text-muted-foreground">
                (hasta {formatDateTime(u.bloqueadoHasta)})
              </span>
            )}
          </div>
        );
      },
    },
  ];

  if (isLoading) {
    return <PageLoader />;
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px]">
        <p className="text-destructive mb-4">{error}</p>
        <Button onClick={refetch}>Reintentar</Button>
      </div>
    );
  }

  const activos = actividad?.filter((u) => u.activo && !u.bloqueado).length || 0;
  const bloqueados = actividad?.filter((u) => u.bloqueado).length || 0;
  const conSesion = actividad?.filter((u) => u.sesionesActivas > 0).length || 0;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Actividad de Usuarios</h2>
        <p className="text-muted-foreground">
          Monitoreo de sesiones, accesos e intentos de login
        </p>
      </div>

      {actividad && actividad.length > 0 && (
        <div className="flex gap-4 flex-wrap">
          <Badge variant="success" className="px-3 py-1">
            <UserCheck className="mr-1 h-3 w-3" />
            {activos} activos
          </Badge>
          <Badge variant="outline" className="px-3 py-1">
            <Wifi className="mr-1 h-3 w-3" />
            {conSesion} con sesión
          </Badge>
          {bloqueados > 0 && (
            <Badge variant="destructive" className="px-3 py-1">
              <Lock className="mr-1 h-3 w-3" />
              {bloqueados} bloqueados
            </Badge>
          )}
        </div>
      )}

      {actividad && actividad.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Activity className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay datos de actividad de usuarios.
          </p>
        </div>
      ) : (
        <DataTable
          data={actividad || []}
          columns={columns}
          searchKeys={['nombre', 'apellido', 'email', 'rol'] as never[]}
          searchPlaceholder="Buscar por nombre, email o rol..."
          pageSize={15}
        />
      )}
    </div>
  );
}

export default ActividadUsuariosPage;
