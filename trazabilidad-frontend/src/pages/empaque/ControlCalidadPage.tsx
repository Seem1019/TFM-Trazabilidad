import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import {
  Plus,
  Pencil,
  Trash2,
  ClipboardCheck,
  MoreHorizontal,
  Calendar,
  CheckCircle,
  XCircle,
  AlertCircle,
} from 'lucide-react';
import { controlCalidadService, clasificacionService, palletService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import { usePermissions } from '@/hooks/usePermissions';
import type { ControlCalidad, ControlCalidadRequest, Clasificacion, Pallet } from '@/types';
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
import { ControlCalidadFormDialog } from './components/ControlCalidadFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function ControlCalidadPage() {
  const {
    data: controles,
    isLoading,
    error,
    refetch,
  } = useFetch<ControlCalidad[]>(useCallback(() => controlCalidadService.getAll(), []), []);

  const { data: clasificaciones } = useFetch<Clasificacion[]>(
    useCallback(() => clasificacionService.getAll(), []),
    []
  );

  const { data: pallets } = useFetch<Pallet[]>(
    useCallback(() => palletService.getAll(), []),
    []
  );

  const { canUpdate, canDelete } = usePermissions();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedControl, setSelectedControl] = useState<ControlCalidad | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedControl(null);
    setIsFormOpen(true);
  };

  const handleEdit = (control: ControlCalidad) => {
    setSelectedControl(control);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await controlCalidadService.delete(deleteId);
      toast.success('Control eliminado correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar el control');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: ControlCalidadRequest) => {
    try {
      if (selectedControl) {
        await controlCalidadService.update(selectedControl.id, data);
        toast.success('Control actualizado correctamente');
      } else {
        await controlCalidadService.create(data);
        toast.success('Control registrado correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedControl ? 'Error al actualizar' : 'Error al registrar');
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const getResultadoVariant = (resultado: string) => {
    switch (resultado) {
      case 'APROBADO':
        return 'success';
      case 'RECHAZADO':
        return 'destructive';
      case 'CONDICIONAL':
        return 'warning';
      default:
        return 'secondary';
    }
  };

  const getResultadoIcon = (resultado: string) => {
    switch (resultado) {
      case 'APROBADO':
        return <CheckCircle className="h-4 w-4" />;
      case 'RECHAZADO':
        return <XCircle className="h-4 w-4" />;
      case 'CONDICIONAL':
        return <AlertCircle className="h-4 w-4" />;
      default:
        return null;
    }
  };

  const columns: Column<ControlCalidad>[] = [
    {
      key: 'codigoControl',
      header: 'Código',
      render: (ctrl) => <span className="font-mono font-medium">{ctrl.codigoControl}</span>,
    },
    {
      key: 'fechaControl',
      header: 'Fecha',
      render: (ctrl) => (
        <div className="flex items-center gap-2">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{formatDate(ctrl.fechaControl)}</span>
        </div>
      ),
    },
    {
      key: 'tipoControl',
      header: 'Tipo',
      render: (ctrl) => <Badge variant="outline">{ctrl.tipoControl}</Badge>,
    },
    {
      key: 'referencia',
      header: 'Referencia',
      render: (ctrl) => (
        <div>
          {ctrl.clasificacionCodigo && (
            <div className="font-medium">Clasif: {ctrl.clasificacionCodigo}</div>
          )}
          {ctrl.palletCodigo && <div className="font-medium">Pallet: {ctrl.palletCodigo}</div>}
          {!ctrl.clasificacionCodigo && !ctrl.palletCodigo && (
            <span className="text-muted-foreground">-</span>
          )}
        </div>
      ),
    },
    {
      key: 'parametroEvaluado',
      header: 'Parámetro',
      render: (ctrl) =>
        ctrl.parametroEvaluado ? (
          <div>
            <div className="font-medium">{ctrl.parametroEvaluado}</div>
            {ctrl.valorMedido && (
              <div className="text-sm text-muted-foreground">Medido: {ctrl.valorMedido}</div>
            )}
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'cumpleEspecificacion',
      header: 'Cumple',
      render: (ctrl) =>
        ctrl.cumpleEspecificacion !== undefined ? (
          ctrl.cumpleEspecificacion ? (
            <CheckCircle className="h-5 w-5 text-green-500" />
          ) : (
            <XCircle className="h-5 w-5 text-red-500" />
          )
        ) : (
          '-'
        ),
    },
    {
      key: 'resultado',
      header: 'Resultado',
      render: (ctrl) => (
        <Badge variant={getResultadoVariant(ctrl.resultado)} className="gap-1">
          {getResultadoIcon(ctrl.resultado)}
          {ctrl.resultado}
        </Badge>
      ),
    },
    {
      key: 'responsableControl',
      header: 'Responsable',
      render: (ctrl) => (
        <span className="max-w-[100px] truncate block">{ctrl.responsableControl}</span>
      ),
    },
    // Solo mostrar columna de acciones si el usuario tiene permisos
    ...(canUpdate('control-calidad') || canDelete('control-calidad') ? [{
      key: 'actions' as keyof ControlCalidad,
      header: '',
      className: 'w-[50px]',
      render: (ctrl: ControlCalidad) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {canUpdate('control-calidad') && (
              <DropdownMenuItem onClick={() => handleEdit(ctrl)}>
                <Pencil className="mr-2 h-4 w-4" />
                Editar
              </DropdownMenuItem>
            )}
            {canDelete('control-calidad') && (
              <DropdownMenuItem onClick={() => setDeleteId(ctrl.id)} className="text-destructive">
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

  // Estadísticas
  const aprobados = controles?.filter((c) => c.resultado === 'APROBADO').length || 0;
  const rechazados = controles?.filter((c) => c.resultado === 'RECHAZADO').length || 0;
  const condicionales = controles?.filter((c) => c.resultado === 'CONDICIONAL').length || 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Control de Calidad</h2>
          <p className="text-muted-foreground">
            Gestione los controles de calidad de productos
          </p>
        </div>
        <PermissionGate module="control-calidad" permission="create">
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Nuevo Control
          </Button>
        </PermissionGate>
      </div>

      {/* Estadísticas */}
      {controles && controles.length > 0 && (
        <div className="flex gap-4">
          <Badge variant="success" className="px-3 py-1 gap-1">
            <CheckCircle className="h-3 w-3" />
            {aprobados} aprobados
          </Badge>
          <Badge variant="warning" className="px-3 py-1 gap-1">
            <AlertCircle className="h-3 w-3" />
            {condicionales} condicionales
          </Badge>
          <Badge variant="destructive" className="px-3 py-1 gap-1">
            <XCircle className="h-3 w-3" />
            {rechazados} rechazados
          </Badge>
        </div>
      )}

      {controles && controles.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <ClipboardCheck className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay controles de calidad registrados. Cree un nuevo control para comenzar.
          </p>
        </div>
      ) : (
        <DataTable
          data={controles || []}
          columns={columns}
          searchKeys={[
            'codigoControl',
            'tipoControl',
            'parametroEvaluado',
            'resultado',
            'responsableControl',
          ]}
          searchPlaceholder="Buscar por código, tipo, parámetro o responsable..."
        />
      )}

      <ControlCalidadFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        control={selectedControl}
        clasificaciones={clasificaciones || []}
        pallets={pallets || []}
        onSubmit={handleFormSubmit}
      />

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar control de calidad?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro del control.
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

export default ControlCalidadPage;
