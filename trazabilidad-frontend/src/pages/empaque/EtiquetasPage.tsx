import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { Plus, Pencil, Trash2, Tags, MoreHorizontal, QrCode, ExternalLink } from 'lucide-react';
import { etiquetaService, clasificacionService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import { usePermissions } from '@/hooks/usePermissions';
import type { Etiqueta, EtiquetaRequest, Clasificacion } from '@/types';
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { DataTable, type Column, PageLoader, PermissionGate } from '@/components/shared';
import { EtiquetaFormDialog } from './components/EtiquetaFormDialog';
import { QRCodeSVG } from 'qrcode.react';

export function EtiquetasPage() {
  const {
    data: etiquetas,
    isLoading,
    error,
    refetch,
  } = useFetch<Etiqueta[]>(useCallback(() => etiquetaService.getAll(), []), []);

  const { data: clasificaciones } = useFetch<Clasificacion[]>(
    useCallback(() => clasificacionService.getAll(), []),
    []
  );

  const { canUpdate, canDelete } = usePermissions();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedEtiqueta, setSelectedEtiqueta] = useState<Etiqueta | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [qrEtiqueta, setQrEtiqueta] = useState<Etiqueta | null>(null);

  const handleCreate = () => {
    setSelectedEtiqueta(null);
    setIsFormOpen(true);
  };

  const handleEdit = (etiqueta: Etiqueta) => {
    setSelectedEtiqueta(etiqueta);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await etiquetaService.delete(deleteId);
      toast.success('Etiqueta eliminada correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar la etiqueta');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: EtiquetaRequest) => {
    try {
      if (selectedEtiqueta) {
        await etiquetaService.update(selectedEtiqueta.id, data);
        toast.success('Etiqueta actualizada correctamente');
      } else {
        await etiquetaService.create(data);
        toast.success('Etiqueta creada correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedEtiqueta ? 'Error al actualizar' : 'Error al crear');
    }
  };

  const handleShowQR = (etiqueta: Etiqueta) => {
    setQrEtiqueta(etiqueta);
  };

  const getEstadoVariant = (estado: string) => {
    switch (estado) {
      case 'ACTIVA':
        return 'success';
      case 'USADA':
        return 'secondary';
      case 'INACTIVA':
        return 'warning';
      case 'ANULADA':
        return 'destructive';
      default:
        return 'outline';
    }
  };

  const getTipoVariant = (tipo: string) => {
    switch (tipo) {
      case 'CAJA':
        return 'default';
      case 'PALLET':
        return 'secondary';
      default:
        return 'outline';
    }
  };

  const columns: Column<Etiqueta>[] = [
    {
      key: 'codigoEtiqueta',
      header: 'Código',
      render: (etq) => <span className="font-mono font-medium">{etq.codigoEtiqueta}</span>,
    },
    {
      key: 'tipoEtiqueta',
      header: 'Tipo',
      render: (etq) => <Badge variant={getTipoVariant(etq.tipoEtiqueta)}>{etq.tipoEtiqueta}</Badge>,
    },
    {
      key: 'clasificacionCodigo',
      header: 'Clasificación',
      render: (etq) => (
        <div>
          <div className="font-medium">{etq.clasificacionCodigo}</div>
          <div className="text-sm text-muted-foreground">{etq.calidad}</div>
        </div>
      ),
    },
    {
      key: 'origen',
      header: 'Origen',
      render: (etq) => (
        <div>
          <div className="font-medium">{etq.loteOrigen}</div>
          <div className="text-sm text-muted-foreground">{etq.fincaOrigen}</div>
        </div>
      ),
    },
    {
      key: 'pesoNeto',
      header: 'Peso',
      render: (etq) =>
        etq.pesoNeto ? (
          <div>
            <div>{etq.pesoNeto} kg neto</div>
            {etq.pesoBruto && (
              <div className="text-sm text-muted-foreground">{etq.pesoBruto} kg bruto</div>
            )}
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'codigoQr',
      header: 'QR',
      render: (etq) => (
        <Button variant="ghost" size="sm" onClick={() => handleShowQR(etq)}>
          <QrCode className="h-4 w-4 mr-1" />
          Ver
        </Button>
      ),
    },
    {
      key: 'estadoEtiqueta',
      header: 'Estado',
      render: (etq) => (
        <Badge variant={getEstadoVariant(etq.estadoEtiqueta)}>{etq.estadoEtiqueta}</Badge>
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (etq) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleShowQR(etq)}>
              <QrCode className="mr-2 h-4 w-4" />
              Ver QR
            </DropdownMenuItem>
            {(canUpdate('etiquetas') || canDelete('etiquetas')) && <DropdownMenuSeparator />}
            {canUpdate('etiquetas') && (
              <DropdownMenuItem onClick={() => handleEdit(etq)}>
                <Pencil className="mr-2 h-4 w-4" />
                Editar
              </DropdownMenuItem>
            )}
            {canDelete('etiquetas') && (
              <DropdownMenuItem onClick={() => setDeleteId(etq.id)} className="text-destructive">
                <Trash2 className="mr-2 h-4 w-4" />
                Eliminar
              </DropdownMenuItem>
            )}
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

  // URL base para trazabilidad pública
  const publicBaseUrl = window.location.origin + '/public/trazabilidad/';

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Etiquetas</h2>
          <p className="text-muted-foreground">
            Gestione las etiquetas y códigos QR para trazabilidad
          </p>
        </div>
        <PermissionGate module="etiquetas" permission="create">
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Nueva Etiqueta
          </Button>
        </PermissionGate>
      </div>

      {etiquetas && etiquetas.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Tags className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay etiquetas registradas. Cree una nueva etiqueta para comenzar.
          </p>
        </div>
      ) : (
        <DataTable
          data={etiquetas || []}
          columns={columns}
          searchKeys={['codigoEtiqueta', 'clasificacionCodigo', 'calidad', 'loteOrigen', 'fincaOrigen']}
          searchPlaceholder="Buscar por código, clasificación, calidad u origen..."
        />
      )}

      <EtiquetaFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        etiqueta={selectedEtiqueta}
        clasificaciones={clasificaciones || []}
        onSubmit={handleFormSubmit}
      />

      {/* QR Code Dialog */}
      <Dialog open={!!qrEtiqueta} onOpenChange={() => setQrEtiqueta(null)}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Código QR - {qrEtiqueta?.codigoEtiqueta}</DialogTitle>
            <DialogDescription>
              Escanee este código para ver la trazabilidad del producto
            </DialogDescription>
          </DialogHeader>
          {qrEtiqueta && (
            <div className="flex flex-col items-center space-y-4">
              <div className="p-4 bg-white rounded-lg border">
                <QRCodeSVG
                  value={publicBaseUrl + qrEtiqueta.codigoQr}
                  size={200}
                  level="H"
                  includeMargin
                />
              </div>
              <div className="text-center space-y-2">
                <p className="text-sm font-medium">{qrEtiqueta.codigoQr}</p>
                <div className="flex gap-2 justify-center">
                  <Badge variant={getTipoVariant(qrEtiqueta.tipoEtiqueta)}>
                    {qrEtiqueta.tipoEtiqueta}
                  </Badge>
                  <Badge variant="outline">{qrEtiqueta.calidad}</Badge>
                </div>
                <div className="text-sm text-muted-foreground">
                  <p>Origen: {qrEtiqueta.fincaOrigen}</p>
                  <p>Lote: {qrEtiqueta.loteOrigen}</p>
                </div>
              </div>
              <Button
                variant="outline"
                className="w-full"
                onClick={() => window.open(publicBaseUrl + qrEtiqueta.codigoQr, '_blank')}
              >
                <ExternalLink className="mr-2 h-4 w-4" />
                Abrir página de trazabilidad
              </Button>
            </div>
          )}
        </DialogContent>
      </Dialog>

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar etiqueta?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará la etiqueta y su código QR.
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

export default EtiquetasPage;
