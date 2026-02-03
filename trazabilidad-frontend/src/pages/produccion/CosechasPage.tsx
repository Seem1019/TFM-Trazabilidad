import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { Plus, Pencil, Trash2, Sprout, MoreHorizontal, Calendar } from 'lucide-react';
import { cosechaService, loteService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import { usePermissions } from '@/hooks/usePermissions';
import type { Cosecha, CosechaRequest, Lote } from '@/types';
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
import { CosechaFormDialog } from './components/CosechaFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function CosechasPage() {
  const { data: cosechas, isLoading, error, refetch } = useFetch<Cosecha[]>(
    useCallback(() => cosechaService.getAll(), []),
    []
  );

  const { data: lotes } = useFetch<Lote[]>(
    useCallback(() => loteService.getAll(), []),
    []
  );

  const { canUpdate, canDelete } = usePermissions();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedCosecha, setSelectedCosecha] = useState<Cosecha | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedCosecha(null);
    setIsFormOpen(true);
  };

  const handleEdit = (cosecha: Cosecha) => {
    setSelectedCosecha(cosecha);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await cosechaService.delete(deleteId);
      toast.success('Cosecha eliminada correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar la cosecha');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: CosechaRequest) => {
    try {
      if (selectedCosecha) {
        await cosechaService.update(selectedCosecha.id, data);
        toast.success('Cosecha actualizada correctamente');
      } else {
        await cosechaService.create(data);
        toast.success('Cosecha registrada correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedCosecha ? 'Error al actualizar' : 'Error al registrar la cosecha');
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const columns: Column<Cosecha>[] = [
    {
      key: 'fechaCosecha',
      header: 'Fecha',
      render: (cosecha) => (
        <div className="flex items-center gap-2">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{formatDate(cosecha.fechaCosecha)}</span>
        </div>
      ),
    },
    {
      key: 'loteNombre',
      header: 'Lote',
      render: (cosecha) => (
        <div>
          <span className="font-medium">{cosecha.loteNombre}</span>
          <span className="text-muted-foreground text-xs ml-2">({cosecha.loteCodigoLote})</span>
        </div>
      ),
    },
    {
      key: 'cantidadCosechada',
      header: 'Cantidad',
      render: (cosecha) => (
        <div className="flex items-center gap-2">
          <Sprout className="h-4 w-4 text-green-600" />
          <span className="font-medium">{cosecha.cantidadCosechada.toLocaleString()}</span>
          <span className="text-muted-foreground">{cosecha.unidadMedida}</span>
        </div>
      ),
    },
    {
      key: 'calidadInicial',
      header: 'Calidad',
      render: (cosecha) => {
        const variant = cosecha.calidadInicial === 'PREMIUM' ? 'success' :
                        cosecha.calidadInicial === 'PRIMERA' ? 'info' :
                        cosecha.calidadInicial === 'SEGUNDA' ? 'warning' : 'secondary';
        return cosecha.calidadInicial ? (
          <Badge variant={variant}>{cosecha.calidadInicial}</Badge>
        ) : '-';
      },
    },
    {
      key: 'responsableCosecha',
      header: 'Responsable',
      render: (cosecha) => cosecha.responsableCosecha || '-',
    },
    {
      key: 'reciente',
      header: 'Estado',
      render: (cosecha) => (
        cosecha.reciente ? (
          <Badge variant="success">Reciente</Badge>
        ) : (
          <Badge variant="secondary">Anterior</Badge>
        )
      ),
    },
    // Solo mostrar columna de acciones si el usuario tiene permisos
    ...(canUpdate('cosechas') || canDelete('cosechas') ? [{
      key: 'actions' as keyof Cosecha,
      header: '',
      className: 'w-[50px]',
      render: (cosecha: Cosecha) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {canUpdate('cosechas') && (
              <DropdownMenuItem onClick={() => handleEdit(cosecha)}>
                <Pencil className="mr-2 h-4 w-4" />
                Editar
              </DropdownMenuItem>
            )}
            {canDelete('cosechas') && (
              <DropdownMenuItem
                onClick={() => setDeleteId(cosecha.id)}
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
          <h2 className="text-3xl font-bold tracking-tight">Cosechas</h2>
          <p className="text-muted-foreground">
            Registre las cosechas de sus lotes de producción
          </p>
        </div>
        <PermissionGate module="cosechas" permission="create">
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Registrar Cosecha
          </Button>
        </PermissionGate>
      </div>

      <DataTable
        data={cosechas || []}
        columns={columns}
        searchKeys={['loteNombre', 'loteCodigoLote', 'responsableCosecha']}
        searchPlaceholder="Buscar por lote o responsable..."
      />

      {/* Form Dialog */}
      <CosechaFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        cosecha={selectedCosecha}
        lotes={lotes || []}
        onSubmit={handleFormSubmit}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar registro de cosecha?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro de esta cosecha.
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

export default CosechasPage;
