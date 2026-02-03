import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { Plus, Pencil, Trash2, MapPin, MoreHorizontal } from 'lucide-react';
import { fincaService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import { usePermissions } from '@/hooks/usePermissions';
import type { Finca, FincaRequest } from '@/types';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
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
import { DataTable, type Column, PageLoader, PermissionGate } from '@/components/shared';
import { FincaFormDialog } from './components/FincaFormDialog';

export function FincasPage() {
  const { data: fincas, isLoading, error, refetch } = useFetch<Finca[]>(
    useCallback(() => fincaService.getAll(), []),
    []
  );
  const { canUpdate, canDelete } = usePermissions();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedFinca, setSelectedFinca] = useState<Finca | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedFinca(null);
    setIsFormOpen(true);
  };

  const handleEdit = (finca: Finca) => {
    setSelectedFinca(finca);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await fincaService.delete(deleteId);
      toast.success('Finca eliminada correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar la finca');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: FincaRequest) => {
    try {
      if (selectedFinca) {
        await fincaService.update(selectedFinca.id, data);
        toast.success('Finca actualizada correctamente');
      } else {
        await fincaService.create(data);
        toast.success('Finca creada correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedFinca ? 'Error al actualizar la finca' : 'Error al crear la finca');
    }
  };

  const columns: Column<Finca>[] = [
    {
      key: 'codigoFinca',
      header: 'Código',
      render: (finca) => (
        <span className="font-medium">{finca.codigoFinca}</span>
      ),
    },
    {
      key: 'nombre',
      header: 'Nombre',
      render: (finca) => (
        <div className="flex items-center gap-2">
          <MapPin className="h-4 w-4 text-primary" />
          <span>{finca.nombre}</span>
        </div>
      ),
    },
    {
      key: 'ubicacion',
      header: 'Ubicación',
      render: (finca) => (
        <span className="text-muted-foreground">
          {[finca.municipio, finca.departamento].filter(Boolean).join(', ') || '-'}
        </span>
      ),
    },
    {
      key: 'areaHectareas',
      header: 'Área (Ha)',
      render: (finca) => finca.areaHectareas?.toFixed(2) || '-',
    },
    {
      key: 'totalLotes',
      header: 'Lotes',
      render: (finca) => (
        <Badge variant="secondary">{finca.totalLotes}</Badge>
      ),
    },
    {
      key: 'totalCertificacionesVigentes',
      header: 'Certificaciones',
      render: (finca) => (
        <Badge variant={finca.totalCertificacionesVigentes > 0 ? 'success' : 'outline'}>
          {finca.totalCertificacionesVigentes}
        </Badge>
      ),
    },
    {
      key: 'activo',
      header: 'Estado',
      render: (finca) => (
        <Badge variant={finca.activo ? 'success' : 'secondary'}>
          {finca.activo ? 'Activo' : 'Inactivo'}
        </Badge>
      ),
    },
    // Solo mostrar columna de acciones si el usuario tiene permisos
    ...(canUpdate('fincas') || canDelete('fincas') ? [{
      key: 'actions' as keyof Finca,
      header: '',
      className: 'w-[50px]',
      render: (finca: Finca) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {canUpdate('fincas') && (
              <DropdownMenuItem onClick={() => handleEdit(finca)}>
                <Pencil className="mr-2 h-4 w-4" />
                Editar
              </DropdownMenuItem>
            )}
            {canDelete('fincas') && (
              <DropdownMenuItem
                onClick={() => setDeleteId(finca.id)}
                className="text-destructive"
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Eliminar
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    }] : []),
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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Fincas</h2>
          <p className="text-muted-foreground">
            Gestione las fincas productoras de su empresa
          </p>
        </div>
        <PermissionGate module="fincas" permission="create">
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Nueva Finca
          </Button>
        </PermissionGate>
      </div>

      <DataTable
        data={fincas || []}
        columns={columns}
        searchKeys={['codigoFinca', 'nombre', 'municipio', 'departamento']}
        searchPlaceholder="Buscar por código, nombre o ubicación..."
      />

      {/* Form Dialog */}
      <FincaFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        finca={selectedFinca}
        onSubmit={handleFormSubmit}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar finca?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminarán todos los datos
              asociados a esta finca.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              Eliminar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default FincasPage;
