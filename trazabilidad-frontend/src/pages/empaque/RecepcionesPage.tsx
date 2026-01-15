import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { Plus, Pencil, Trash2, Warehouse, MoreHorizontal, Calendar, Truck } from 'lucide-react';
import { recepcionService, loteService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import type { RecepcionPlanta, RecepcionPlantaRequest, Lote } from '@/types';
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
import { DataTable, type Column, PageLoader } from '@/components/shared';
import { RecepcionFormDialog } from './components/RecepcionFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function RecepcionesPage() {
  const {
    data: recepciones,
    isLoading,
    error,
    refetch,
  } = useFetch<RecepcionPlanta[]>(useCallback(() => recepcionService.getAll(), []), []);

  const { data: lotes } = useFetch<Lote[]>(useCallback(() => loteService.getAll(), []), []);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedRecepcion, setSelectedRecepcion] = useState<RecepcionPlanta | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedRecepcion(null);
    setIsFormOpen(true);
  };

  const handleEdit = (recepcion: RecepcionPlanta) => {
    setSelectedRecepcion(recepcion);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await recepcionService.delete(deleteId);
      toast.success('Recepción eliminada correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar la recepción');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: RecepcionPlantaRequest) => {
    try {
      if (selectedRecepcion) {
        await recepcionService.update(selectedRecepcion.id, data);
        toast.success('Recepción actualizada correctamente');
      } else {
        await recepcionService.create(data);
        toast.success('Recepción registrada correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedRecepcion ? 'Error al actualizar' : 'Error al registrar');
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const getEstadoVariant = (estado: string) => {
    switch (estado) {
      case 'ACEPTADA':
        return 'success';
      case 'RECHAZADA':
        return 'destructive';
      case 'CLASIFICADA':
        return 'secondary';
      case 'PENDIENTE':
      default:
        return 'warning';
    }
  };

  const columns: Column<RecepcionPlanta>[] = [
    {
      key: 'codigoRecepcion',
      header: 'Código',
      render: (rec) => (
        <span className="font-mono font-medium">{rec.codigoRecepcion}</span>
      ),
    },
    {
      key: 'loteNombre',
      header: 'Lote',
      render: (rec) => (
        <div>
          <div className="font-medium">{rec.loteNombre}</div>
          <div className="text-sm text-muted-foreground">{rec.fincaNombre}</div>
        </div>
      ),
    },
    {
      key: 'fechaRecepcion',
      header: 'Fecha',
      render: (rec) => (
        <div className="flex items-center gap-2">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <div>
            <div>{formatDate(rec.fechaRecepcion)}</div>
            {rec.horaRecepcion && (
              <div className="text-sm text-muted-foreground">{rec.horaRecepcion}</div>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'cantidadRecibida',
      header: 'Cantidad',
      render: (rec) => (
        <span>
          {rec.cantidadRecibida.toLocaleString()} {rec.unidadMedida}
        </span>
      ),
    },
    {
      key: 'temperaturaFruta',
      header: 'Temp.',
      render: (rec) => (rec.temperaturaFruta ? `${rec.temperaturaFruta}°C` : '-'),
    },
    {
      key: 'vehiculoTransporte',
      header: 'Transporte',
      render: (rec) =>
        rec.vehiculoTransporte ? (
          <div className="flex items-center gap-2">
            <Truck className="h-4 w-4 text-muted-foreground" />
            <span>{rec.vehiculoTransporte}</span>
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'estadoRecepcion',
      header: 'Estado',
      render: (rec) => (
        <Badge variant={getEstadoVariant(rec.estadoRecepcion)}>{rec.estadoRecepcion}</Badge>
      ),
    },
    {
      key: 'totalClasificaciones',
      header: 'Clasif.',
      render: (rec) => (
        <Badge variant="outline">{rec.totalClasificaciones}</Badge>
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (rec) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleEdit(rec)}>
              <Pencil className="mr-2 h-4 w-4" />
              Editar
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setDeleteId(rec.id)} className="text-destructive">
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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Recepciones en Planta</h2>
          <p className="text-muted-foreground">
            Gestione las recepciones de fruta en la planta de empaque
          </p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="mr-2 h-4 w-4" />
          Nueva Recepción
        </Button>
      </div>

      {recepciones && recepciones.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Warehouse className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay recepciones registradas. Cree una nueva recepción para comenzar.
          </p>
        </div>
      ) : (
        <DataTable
          data={recepciones || []}
          columns={columns}
          searchKeys={['codigoRecepcion', 'loteNombre', 'fincaNombre', 'vehiculoTransporte']}
          searchPlaceholder="Buscar por código, lote, finca o vehículo..."
        />
      )}

      <RecepcionFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        recepcion={selectedRecepcion}
        lotes={lotes || []}
        onSubmit={handleFormSubmit}
      />

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar recepción?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro de esta recepción.
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

export default RecepcionesPage;
