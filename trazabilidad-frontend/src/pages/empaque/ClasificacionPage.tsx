import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { Plus, Pencil, Trash2, Package, MoreHorizontal, Calendar, Scale } from 'lucide-react';
import { clasificacionService, recepcionService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import { usePermissions } from '@/hooks/usePermissions';
import type { Clasificacion, ClasificacionRequest, RecepcionPlanta } from '@/types';
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
import { ClasificacionFormDialog } from './components/ClasificacionFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function ClasificacionPage() {
  const {
    data: clasificaciones,
    isLoading,
    error,
    refetch,
  } = useFetch<Clasificacion[]>(useCallback(() => clasificacionService.getAll(), []), []);

  const { data: recepciones } = useFetch<RecepcionPlanta[]>(
    useCallback(() => recepcionService.getAll(), []),
    []
  );

  const { canUpdate, canDelete } = usePermissions();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedClasificacion, setSelectedClasificacion] = useState<Clasificacion | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedClasificacion(null);
    setIsFormOpen(true);
  };

  const handleEdit = (clasificacion: Clasificacion) => {
    setSelectedClasificacion(clasificacion);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await clasificacionService.delete(deleteId);
      toast.success('Clasificación eliminada correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar la clasificación');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: ClasificacionRequest) => {
    try {
      if (selectedClasificacion) {
        await clasificacionService.update(selectedClasificacion.id, data);
        toast.success('Clasificación actualizada correctamente');
      } else {
        await clasificacionService.create(data);
        toast.success('Clasificación registrada correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedClasificacion ? 'Error al actualizar' : 'Error al registrar');
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const getCalidadVariant = (calidad: string) => {
    switch (calidad) {
      case 'PREMIUM':
        return 'default';
      case 'PRIMERA':
        return 'success';
      case 'SEGUNDA':
        return 'secondary';
      case 'TERCERA':
        return 'warning';
      case 'DESCARTE':
        return 'destructive';
      default:
        return 'outline';
    }
  };

  const columns: Column<Clasificacion>[] = [
    {
      key: 'codigoClasificacion',
      header: 'Código',
      render: (cla) => <span className="font-mono font-medium">{cla.codigoClasificacion}</span>,
    },
    {
      key: 'recepcionCodigo',
      header: 'Recepción',
      render: (cla) => (
        <div>
          <div className="font-medium">{cla.recepcionCodigo}</div>
          <div className="text-sm text-muted-foreground">{cla.loteNombre}</div>
        </div>
      ),
    },
    {
      key: 'fechaClasificacion',
      header: 'Fecha',
      render: (cla) => (
        <div className="flex items-center gap-2">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{formatDate(cla.fechaClasificacion)}</span>
        </div>
      ),
    },
    {
      key: 'calidad',
      header: 'Calidad',
      render: (cla) => <Badge variant={getCalidadVariant(cla.calidad)}>{cla.calidad}</Badge>,
    },
    {
      key: 'calibre',
      header: 'Calibre',
      render: (cla) => (cla.calibre ? cla.calibre.replace('_', ' ') : '-'),
    },
    {
      key: 'cantidadClasificada',
      header: 'Cantidad',
      render: (cla) => (
        <div className="flex items-center gap-2">
          <Scale className="h-4 w-4 text-muted-foreground" />
          <span>
            {cla.cantidadClasificada.toLocaleString()} {cla.unidadMedida}
          </span>
        </div>
      ),
    },
    {
      key: 'porcentajeMerma',
      header: 'Merma',
      render: (cla) =>
        cla.porcentajeMerma ? (
          <span className={cla.porcentajeMerma > 10 ? 'text-destructive font-medium' : ''}>
            {cla.porcentajeMerma}%
          </span>
        ) : (
          '-'
        ),
    },
    {
      key: 'totalEtiquetas',
      header: 'Etiquetas',
      render: (cla) => <Badge variant="outline">{cla.totalEtiquetas}</Badge>,
    },
    // Solo mostrar columna de acciones si el usuario tiene permisos
    ...(canUpdate('clasificacion') || canDelete('clasificacion') ? [{
      key: 'actions' as keyof Clasificacion,
      header: '',
      className: 'w-[50px]',
      render: (cla: Clasificacion) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {canUpdate('clasificacion') && (
              <DropdownMenuItem onClick={() => handleEdit(cla)}>
                <Pencil className="mr-2 h-4 w-4" />
                Editar
              </DropdownMenuItem>
            )}
            {canDelete('clasificacion') && (
              <DropdownMenuItem onClick={() => setDeleteId(cla.id)} className="text-destructive">
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
          <h2 className="text-3xl font-bold tracking-tight">Clasificación</h2>
          <p className="text-muted-foreground">
            Gestione la clasificación de fruta por calidad y calibre
          </p>
        </div>
        <PermissionGate module="clasificacion" permission="create">
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Nueva Clasificación
          </Button>
        </PermissionGate>
      </div>

      {clasificaciones && clasificaciones.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Package className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay clasificaciones registradas. Cree una nueva clasificación para comenzar.
          </p>
        </div>
      ) : (
        <DataTable
          data={clasificaciones || []}
          columns={columns}
          searchKeys={['codigoClasificacion', 'recepcionCodigo', 'loteNombre', 'calidad']}
          searchPlaceholder="Buscar por código, recepción, lote o calidad..."
        />
      )}

      <ClasificacionFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        clasificacion={selectedClasificacion}
        recepciones={recepciones || []}
        onSubmit={handleFormSubmit}
      />

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar clasificación?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro de esta clasificación.
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

export default ClasificacionPage;
