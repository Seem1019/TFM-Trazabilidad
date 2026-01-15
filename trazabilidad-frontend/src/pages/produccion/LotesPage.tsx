import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { Plus, Pencil, Trash2, Layers, MoreHorizontal, Sprout } from 'lucide-react';
import { loteService, fincaService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import type { Lote, LoteRequest, Finca } from '@/types';
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
import { LoteFormDialog } from './components/LoteFormDialog';

export function LotesPage() {
  const { data: lotes, isLoading, error, refetch } = useFetch<Lote[]>(
    useCallback(() => loteService.getAll(), []),
    []
  );

  const { data: fincas } = useFetch<Finca[]>(
    useCallback(() => fincaService.getAll(), []),
    []
  );

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedLote, setSelectedLote] = useState<Lote | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedLote(null);
    setIsFormOpen(true);
  };

  const handleEdit = (lote: Lote) => {
    setSelectedLote(lote);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await loteService.delete(deleteId);
      toast.success('Lote eliminado correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar el lote');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: LoteRequest) => {
    try {
      if (selectedLote) {
        await loteService.update(selectedLote.id, data);
        toast.success('Lote actualizado correctamente');
      } else {
        await loteService.create(data);
        toast.success('Lote creado correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedLote ? 'Error al actualizar el lote' : 'Error al crear el lote');
    }
  };

  const getEstadoVariant = (estado: string) => {
    switch (estado) {
      case 'ACTIVO':
        return 'success';
      case 'EN_COSECHA':
        return 'warning';
      case 'EN_REPOSO':
        return 'secondary';
      default:
        return 'outline';
    }
  };

  const columns: Column<Lote>[] = [
    {
      key: 'codigoLote',
      header: 'Código',
      render: (lote) => (
        <span className="font-medium">{lote.codigoLote}</span>
      ),
    },
    {
      key: 'nombre',
      header: 'Nombre',
      render: (lote) => (
        <div className="flex items-center gap-2">
          <Layers className="h-4 w-4 text-primary" />
          <span>{lote.nombre}</span>
        </div>
      ),
    },
    {
      key: 'fincaNombre',
      header: 'Finca',
    },
    {
      key: 'tipoFruta',
      header: 'Fruta',
      render: (lote) => (
        <div className="flex items-center gap-2">
          <Sprout className="h-4 w-4 text-green-600" />
          <span>{lote.tipoFruta}</span>
          {lote.variedad && (
            <span className="text-muted-foreground text-xs">({lote.variedad})</span>
          )}
        </div>
      ),
    },
    {
      key: 'areaHectareas',
      header: 'Área (Ha)',
      render: (lote) => lote.areaHectareas?.toFixed(2) || '-',
    },
    {
      key: 'estadoLote',
      header: 'Estado',
      render: (lote) => (
        <Badge variant={getEstadoVariant(lote.estadoLote)}>
          {lote.estadoLote?.replace('_', ' ') || 'N/A'}
        </Badge>
      ),
    },
    {
      key: 'listoParaCosechar',
      header: 'Cosecha',
      render: (lote) => (
        lote.listoParaCosechar ? (
          <Badge variant="success">Listo</Badge>
        ) : (
          <Badge variant="secondary">No listo</Badge>
        )
      ),
    },
    {
      key: 'totalCosechado',
      header: 'Cosechado',
      render: (lote) => (
        <span>{lote.totalCosechado?.toLocaleString() || 0} kg</span>
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (lote) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleEdit(lote)}>
              <Pencil className="mr-2 h-4 w-4" />
              Editar
            </DropdownMenuItem>
            <DropdownMenuItem
              onClick={() => setDeleteId(lote.id)}
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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Lotes</h2>
          <p className="text-muted-foreground">
            Gestione los lotes de cultivo de sus fincas
          </p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="mr-2 h-4 w-4" />
          Nuevo Lote
        </Button>
      </div>

      <DataTable
        data={lotes || []}
        columns={columns}
        searchKeys={['codigoLote', 'nombre', 'fincaNombre', 'tipoFruta']}
        searchPlaceholder="Buscar por código, nombre, finca o fruta..."
      />

      {/* Form Dialog */}
      <LoteFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        lote={selectedLote}
        fincas={fincas || []}
        onSubmit={handleFormSubmit}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar lote?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminarán todas las cosechas
              y actividades asociadas a este lote.
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

export default LotesPage;
