import { useState, useCallback, useEffect } from 'react';
import { toast } from 'sonner';
import { Plus, Pencil, Trash2, Sprout, MoreHorizontal, Calendar, Layers } from 'lucide-react';
import { actividadService, loteService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import { usePermissions } from '@/hooks/usePermissions';
import type { ActividadAgronomica, ActividadAgronomicarRequest, Lote } from '@/types';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
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
import { ActividadFormDialog } from './components/ActividadFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function ActividadesPage() {
  const [selectedLoteId, setSelectedLoteId] = useState<string>('');
  const [actividades, setActividades] = useState<ActividadAgronomica[]>([]);
  const [isLoadingActividades, setIsLoadingActividades] = useState(false);

  const { data: lotes, isLoading: isLoadingLotes, error: errorLotes } = useFetch<Lote[]>(
    useCallback(() => loteService.getAll(), []),
    []
  );

  const { canUpdate, canDelete } = usePermissions();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedActividad, setSelectedActividad] = useState<ActividadAgronomica | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  // Cargar actividades cuando se selecciona un lote
  useEffect(() => {
    const loadActividades = async () => {
      if (!selectedLoteId) {
        setActividades([]);
        return;
      }

      setIsLoadingActividades(true);
      try {
        const data = await actividadService.getByLote(Number(selectedLoteId));
        setActividades(data);
      } catch {
        toast.error('Error al cargar actividades');
        setActividades([]);
      } finally {
        setIsLoadingActividades(false);
      }
    };

    loadActividades();
  }, [selectedLoteId]);

  const refetch = async () => {
    if (selectedLoteId) {
      setIsLoadingActividades(true);
      try {
        const data = await actividadService.getByLote(Number(selectedLoteId));
        setActividades(data);
      } catch {
        toast.error('Error al cargar actividades');
      } finally {
        setIsLoadingActividades(false);
      }
    }
  };

  const handleCreate = () => {
    setSelectedActividad(null);
    setIsFormOpen(true);
  };

  const handleEdit = (actividad: ActividadAgronomica) => {
    setSelectedActividad(actividad);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await actividadService.delete(deleteId);
      toast.success('Actividad eliminada correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar la actividad');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: ActividadAgronomicarRequest) => {
    try {
      if (selectedActividad) {
        await actividadService.update(selectedActividad.id, data);
        toast.success('Actividad actualizada correctamente');
      } else {
        await actividadService.create(data);
        toast.success('Actividad registrada correctamente');
        // Si se crea para el lote seleccionado, actualizar la lista
        if (data.loteId === Number(selectedLoteId)) {
          refetch();
        } else {
          // Si se crea para otro lote, cambiar la selección
          setSelectedLoteId(String(data.loteId));
        }
      }
      setIsFormOpen(false);
      if (selectedActividad) {
        refetch();
      }
    } catch {
      toast.error(selectedActividad ? 'Error al actualizar' : 'Error al registrar');
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const getTipoVariant = (tipo: string) => {
    switch (tipo) {
      case 'FERTILIZACION':
        return 'success';
      case 'FUMIGACION':
        return 'warning';
      case 'RIEGO':
        return 'secondary';
      case 'PODA':
        return 'outline';
      case 'DESHIERBE':
        return 'default';
      default:
        return 'secondary';
    }
  };

  const columns: Column<ActividadAgronomica>[] = [
    {
      key: 'tipoActividad',
      header: 'Tipo',
      render: (act) => (
        <Badge variant={getTipoVariant(act.tipoActividad)}>
          {act.tipoActividad?.replace('_', ' ')}
        </Badge>
      ),
    },
    {
      key: 'fechaActividad',
      header: 'Fecha',
      render: (act) => (
        <div className="flex items-center gap-2">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{formatDate(act.fechaActividad)}</span>
        </div>
      ),
    },
    {
      key: 'descripcion',
      header: 'Descripción',
      render: (act) => (
        <span className="max-w-[200px] truncate block">
          {act.descripcion || '-'}
        </span>
      ),
    },
    {
      key: 'productosUtilizados',
      header: 'Productos',
      render: (act) => (
        <span className="max-w-[150px] truncate block">
          {act.productosUtilizados || '-'}
        </span>
      ),
    },
    {
      key: 'dosificacion',
      header: 'Dosificación',
      render: (act) => act.dosificacion || '-',
    },
    {
      key: 'responsable',
      header: 'Responsable',
      render: (act) => act.responsable || '-',
    },
    // Solo mostrar columna de acciones si el usuario tiene permisos
    ...(canUpdate('actividades') || canDelete('actividades') ? [{
      key: 'actions' as keyof ActividadAgronomica,
      header: '',
      className: 'w-[50px]',
      render: (act: ActividadAgronomica) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {canUpdate('actividades') && (
              <DropdownMenuItem onClick={() => handleEdit(act)}>
                <Pencil className="mr-2 h-4 w-4" />
                Editar
              </DropdownMenuItem>
            )}
            {canDelete('actividades') && (
              <DropdownMenuItem
                onClick={() => setDeleteId(act.id)}
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

  if (isLoadingLotes) {
    return <PageLoader />;
  }

  if (errorLotes) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px]">
        <p className="text-destructive mb-4">{errorLotes}</p>
        <Button onClick={() => window.location.reload()}>Reintentar</Button>
      </div>
    );
  }

  const selectedLote = lotes?.find(l => l.id === Number(selectedLoteId));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Actividades Agronómicas</h2>
          <p className="text-muted-foreground">
            Gestione las actividades de mantenimiento de sus cultivos
          </p>
        </div>
        <PermissionGate module="actividades" permission="create">
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Nueva Actividad
          </Button>
        </PermissionGate>
      </div>

      {/* Selector de Lote */}
      <div className="flex items-center gap-4 p-4 rounded-lg border bg-card">
        <div className="flex items-center gap-2 text-muted-foreground">
          <Layers className="h-5 w-5" />
          <span className="font-medium">Seleccione un lote:</span>
        </div>
        <Select value={selectedLoteId} onValueChange={setSelectedLoteId}>
          <SelectTrigger className="w-[350px]">
            <SelectValue placeholder="Seleccione un lote para ver sus actividades" />
          </SelectTrigger>
          <SelectContent>
            {lotes?.map((lote) => (
              <SelectItem key={lote.id} value={String(lote.id)}>
                {lote.nombre} - {lote.tipoFruta} ({lote.codigoLote})
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {selectedLote && (
          <Badge variant="outline" className="ml-2">
            {selectedLote.totalActividades} actividades registradas
          </Badge>
        )}
      </div>

      {/* Contenido condicional */}
      {!selectedLoteId ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Sprout className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            Seleccione un lote para ver y gestionar sus actividades agronómicas
          </p>
        </div>
      ) : isLoadingActividades ? (
        <PageLoader />
      ) : (
        <DataTable
          data={actividades}
          columns={columns}
          searchKeys={['tipoActividad', 'descripcion', 'responsable', 'productosUtilizados']}
          searchPlaceholder="Buscar por tipo, descripción o responsable..."
        />
      )}

      {/* Form Dialog */}
      <ActividadFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        actividad={selectedActividad}
        lotes={lotes || []}
        onSubmit={handleFormSubmit}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar actividad?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro de esta actividad agronómica.
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

export default ActividadesPage;
