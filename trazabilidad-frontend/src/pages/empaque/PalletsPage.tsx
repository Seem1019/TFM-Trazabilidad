import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import {
  Plus,
  Pencil,
  Trash2,
  Container,
  MoreHorizontal,
  Calendar,
  MapPin,
  Thermometer,
  Package,
} from 'lucide-react';
import { palletService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import type { Pallet, PalletRequest } from '@/types';
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
import { PalletFormDialog } from './components/PalletFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function PalletsPage() {
  const {
    data: pallets,
    isLoading,
    error,
    refetch,
  } = useFetch<Pallet[]>(useCallback(() => palletService.getAll(), []), []);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedPallet, setSelectedPallet] = useState<Pallet | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedPallet(null);
    setIsFormOpen(true);
  };

  const handleEdit = (pallet: Pallet) => {
    setSelectedPallet(pallet);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await palletService.delete(deleteId);
      toast.success('Pallet eliminado correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar el pallet');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: PalletRequest) => {
    try {
      if (selectedPallet) {
        await palletService.update(selectedPallet.id, data);
        toast.success('Pallet actualizado correctamente');
      } else {
        await palletService.create(data);
        toast.success('Pallet creado correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedPallet ? 'Error al actualizar' : 'Error al crear');
    }
  };

  const handleCambiarEstado = async (id: number, estado: string) => {
    try {
      await palletService.cambiarEstado(id, estado);
      toast.success(`Estado cambiado a ${estado}`);
      refetch();
    } catch {
      toast.error('Error al cambiar estado');
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
      case 'PREPARADO':
        return 'secondary';
      case 'LISTO_ENVIO':
        return 'success';
      case 'ENVIADO':
        return 'default';
      case 'ENTREGADO':
        return 'outline';
      case 'RECHAZADO':
        return 'destructive';
      default:
        return 'secondary';
    }
  };

  const columns: Column<Pallet>[] = [
    {
      key: 'codigoPallet',
      header: 'Código',
      render: (pal) => <span className="font-mono font-medium">{pal.codigoPallet}</span>,
    },
    {
      key: 'fechaPaletizado',
      header: 'Fecha',
      render: (pal) => (
        <div className="flex items-center gap-2">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{formatDate(pal.fechaPaletizado)}</span>
        </div>
      ),
    },
    {
      key: 'tipoFruta',
      header: 'Producto',
      render: (pal) => (
        <div>
          <div className="font-medium">{pal.tipoFruta?.replace('_', ' ') || '-'}</div>
          {pal.calidad && <div className="text-sm text-muted-foreground">{pal.calidad}</div>}
        </div>
      ),
    },
    {
      key: 'numeroCajas',
      header: 'Cajas',
      render: (pal) => (
        <div className="flex items-center gap-2">
          <Package className="h-4 w-4 text-muted-foreground" />
          <span>{pal.numeroCajas}</span>
        </div>
      ),
    },
    {
      key: 'pesoNetoTotal',
      header: 'Peso',
      render: (pal) =>
        pal.pesoNetoTotal ? (
          <div>
            <div>{pal.pesoNetoTotal.toLocaleString()} kg neto</div>
            {pal.pesoBrutoTotal && (
              <div className="text-sm text-muted-foreground">
                {pal.pesoBrutoTotal.toLocaleString()} kg bruto
              </div>
            )}
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'temperaturaAlmacenamiento',
      header: 'Temp.',
      render: (pal) =>
        pal.temperaturaAlmacenamiento !== undefined ? (
          <div className="flex items-center gap-1">
            <Thermometer className="h-4 w-4 text-muted-foreground" />
            <span>{pal.temperaturaAlmacenamiento}°C</span>
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'destino',
      header: 'Destino',
      render: (pal) =>
        pal.destino ? (
          <div className="flex items-center gap-2">
            <MapPin className="h-4 w-4 text-muted-foreground" />
            <span className="max-w-[120px] truncate">{pal.destino}</span>
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'totalEtiquetas',
      header: 'Etiquetas',
      render: (pal) => <Badge variant="outline">{pal.totalEtiquetas}</Badge>,
    },
    {
      key: 'estadoPallet',
      header: 'Estado',
      render: (pal) => (
        <Badge variant={getEstadoVariant(pal.estadoPallet)}>
          {pal.estadoPallet?.replace('_', ' ')}
        </Badge>
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (pal) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleEdit(pal)}>
              <Pencil className="mr-2 h-4 w-4" />
              Editar
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            {pal.estadoPallet === 'PREPARADO' && (
              <DropdownMenuItem onClick={() => handleCambiarEstado(pal.id, 'LISTO_ENVIO')}>
                Marcar Listo para Envío
              </DropdownMenuItem>
            )}
            {pal.estadoPallet === 'LISTO_ENVIO' && (
              <DropdownMenuItem onClick={() => handleCambiarEstado(pal.id, 'ENVIADO')}>
                Marcar como Enviado
              </DropdownMenuItem>
            )}
            {pal.estadoPallet === 'ENVIADO' && (
              <>
                <DropdownMenuItem onClick={() => handleCambiarEstado(pal.id, 'ENTREGADO')}>
                  Marcar como Entregado
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => handleCambiarEstado(pal.id, 'RECHAZADO')}>
                  Marcar como Rechazado
                </DropdownMenuItem>
              </>
            )}
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => setDeleteId(pal.id)} className="text-destructive">
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

  // Estadísticas rápidas
  const listosEnvio = pallets?.filter((p) => p.estadoPallet === 'LISTO_ENVIO').length || 0;
  const enviados = pallets?.filter((p) => p.estadoPallet === 'ENVIADO').length || 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Pallets</h2>
          <p className="text-muted-foreground">Gestione los pallets de fruta para exportación</p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="mr-2 h-4 w-4" />
          Nuevo Pallet
        </Button>
      </div>

      {/* Estadísticas rápidas */}
      {pallets && pallets.length > 0 && (
        <div className="flex gap-4">
          <Badge variant="success" className="px-3 py-1">
            {listosEnvio} listos para envío
          </Badge>
          <Badge variant="default" className="px-3 py-1">
            {enviados} en tránsito
          </Badge>
        </div>
      )}

      {pallets && pallets.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Container className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay pallets registrados. Cree un nuevo pallet para comenzar.
          </p>
        </div>
      ) : (
        <DataTable
          data={pallets || []}
          columns={columns}
          searchKeys={['codigoPallet', 'tipoFruta', 'calidad', 'destino']}
          searchPlaceholder="Buscar por código, fruta, calidad o destino..."
        />
      )}

      <PalletFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        pallet={selectedPallet}
        onSubmit={handleFormSubmit}
      />

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar pallet?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro del pallet.
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

export default PalletsPage;
