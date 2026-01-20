import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Plus,
  Pencil,
  Trash2,
  MoreHorizontal,
  UserCheck,
  UserX,
  Mail,
  Phone,
  Shield,
  Calendar,
  Clock,
} from 'lucide-react';
import { userService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import type { UserResponse, UserRequest } from '@/types';
import { ROLES_LABELS } from '@/types';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { DataTable, type Column, PageLoader } from '@/components/shared';
import { UserFormDialog } from './components/UserFormDialog';
import { Switch } from '@/components/ui/switch';

export function UsuariosPage() {
  const {
    data: usuarios,
    isLoading,
    error,
    refetch,
  } = useFetch<UserResponse[]>(
    useCallback(() => userService.getAllIncludingInactive(), []),
    []
  );

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserResponse | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedUser(null);
    setIsFormOpen(true);
  };

  const handleEdit = (user: UserResponse) => {
    setSelectedUser(user);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await userService.delete(deleteId);
      toast.success('Usuario eliminado correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar el usuario');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: UserRequest) => {
    try {
      if (selectedUser) {
        await userService.update(selectedUser.id, data);
        toast.success('Usuario actualizado correctamente');
      } else {
        await userService.create(data);
        toast.success('Usuario creado correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error al guardar';
      toast.error(message);
    }
  };

  const handleToggleEstado = async (user: UserResponse) => {
    try {
      await userService.cambiarEstado(user.id, !user.activo);
      toast.success(`Usuario ${user.activo ? 'desactivado' : 'activado'}`);
      refetch();
    } catch {
      toast.error('Error al cambiar estado');
    }
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-';
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return 'Nunca';
    try {
      return format(new Date(dateStr), "dd MMM yyyy 'a las' HH:mm", { locale: es });
    } catch {
      return dateStr;
    }
  };

  const getRolBadgeVariant = (rol: string) => {
    switch (rol) {
      case 'ADMIN':
        return 'default';
      case 'PRODUCTOR':
        return 'success';
      case 'OPERADOR_PLANTA':
        return 'secondary';
      case 'OPERADOR_LOGISTICA':
        return 'outline';
      case 'AUDITOR':
        return 'warning';
      default:
        return 'secondary';
    }
  };

  const columns: Column<UserResponse>[] = [
    {
      key: 'nombre',
      header: 'Usuario',
      render: (user) => (
        <div className="flex flex-col">
          <span className="font-medium">
            {user.nombre} {user.apellido}
          </span>
          <span className="text-sm text-muted-foreground flex items-center gap-1">
            <Mail className="h-3 w-3" />
            {user.email}
          </span>
        </div>
      ),
    },
    {
      key: 'telefono',
      header: 'Teléfono',
      render: (user) =>
        user.telefono ? (
          <span className="flex items-center gap-1 text-sm">
            <Phone className="h-3 w-3 text-muted-foreground" />
            {user.telefono}
          </span>
        ) : (
          <span className="text-muted-foreground">-</span>
        ),
    },
    {
      key: 'rol',
      header: 'Rol',
      render: (user) => (
        <Badge variant={getRolBadgeVariant(user.rol)} className="gap-1">
          <Shield className="h-3 w-3" />
          {ROLES_LABELS[user.rol] || user.rol}
        </Badge>
      ),
    },
    {
      key: 'activo',
      header: 'Estado',
      render: (user) => (
        <div className="flex items-center gap-2">
          <Switch
            checked={user.activo}
            onCheckedChange={() => handleToggleEstado(user)}
            className="data-[state=checked]:bg-green-500"
          />
          <span className={user.activo ? 'text-green-600' : 'text-muted-foreground'}>
            {user.activo ? 'Activo' : 'Inactivo'}
          </span>
        </div>
      ),
    },
    {
      key: 'createdAt',
      header: 'Creado',
      render: (user) => (
        <div className="flex items-center gap-1 text-sm">
          <Calendar className="h-3 w-3 text-muted-foreground" />
          {formatDate(user.createdAt)}
        </div>
      ),
    },
    {
      key: 'ultimoAcceso',
      header: 'Último acceso',
      render: (user) => (
        <div className="flex items-center gap-1 text-sm text-muted-foreground">
          <Clock className="h-3 w-3" />
          {formatDateTime(user.ultimoAcceso)}
        </div>
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (user) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleEdit(user)}>
              <Pencil className="mr-2 h-4 w-4" />
              Editar
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => handleToggleEstado(user)}>
              {user.activo ? (
                <>
                  <UserX className="mr-2 h-4 w-4" />
                  Desactivar
                </>
              ) : (
                <>
                  <UserCheck className="mr-2 h-4 w-4" />
                  Activar
                </>
              )}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={() => setDeleteId(user.id)}
              className="text-destructive"
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Eliminar
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      ),
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

  // Estadísticas
  const activos = usuarios?.filter((u) => u.activo).length || 0;
  const inactivos = usuarios?.filter((u) => !u.activo).length || 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Gestión de Usuarios</h2>
          <p className="text-muted-foreground">
            Administre los usuarios de su empresa
          </p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="mr-2 h-4 w-4" />
          Nuevo Usuario
        </Button>
      </div>

      {/* Estadísticas */}
      {usuarios && usuarios.length > 0 && (
        <div className="flex gap-4">
          <Badge variant="success" className="px-3 py-1">
            <UserCheck className="mr-1 h-3 w-3" />
            {activos} activos
          </Badge>
          <Badge variant="secondary" className="px-3 py-1">
            <UserX className="mr-1 h-3 w-3" />
            {inactivos} inactivos
          </Badge>
        </div>
      )}

      {usuarios && usuarios.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Shield className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay usuarios registrados. Cree un nuevo usuario para comenzar.
          </p>
        </div>
      ) : (
        <DataTable
          data={usuarios || []}
          columns={columns}
          searchKeys={['nombre', 'apellido', 'email', 'rol']}
          searchPlaceholder="Buscar por nombre, email o rol..."
        />
      )}

      <UserFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        user={selectedUser}
        onSubmit={handleFormSubmit}
      />

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar usuario?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción desactivará el usuario y no podrá acceder al sistema.
              El registro se mantendrá para fines de auditoría.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Eliminar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default UsuariosPage;
