import { useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';
import {
  Plus,
  Pencil,
  Trash2,
  MapPin,
  MoreHorizontal,
  Calendar,
  Clock,
  AlertTriangle,
  Thermometer,
  Droplets,
} from 'lucide-react';
import { eventoLogisticoService, envioService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import { usePermissions } from '@/hooks/usePermissions';
import type { EventoLogistico, EventoLogisticoRequest, Envio } from '@/types';
import { TIPO_EVENTO_LABELS } from '@/types';
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
import { DataTable, type Column, PageLoader, PermissionGate } from '@/components/shared';
import { EventoFormDialog } from './components/EventoFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function EventosPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const envioIdParam = searchParams.get('envioId');
  const [selectedEnvioId, setSelectedEnvioId] = useState<number | null>(
    envioIdParam ? Number(envioIdParam) : null
  );

  const {
    data: envios,
    isLoading: loadingEnvios,
  } = useFetch<Envio[]>(useCallback(() => envioService.getAll(), []), []);

  const {
    data: eventos,
    isLoading: loadingEventos,
    error,
    refetch,
  } = useFetch<EventoLogistico[]>(
    useCallback(
      () =>
        selectedEnvioId
          ? eventoLogisticoService.getByEnvio(selectedEnvioId)
          : Promise.resolve([]),
      [selectedEnvioId]
    ),
    [selectedEnvioId]
  );

  const { canUpdate, canDelete } = usePermissions();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedEvento, setSelectedEvento] = useState<EventoLogistico | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleEnvioChange = (value: string) => {
    const id = value ? Number(value) : null;
    setSelectedEnvioId(id);
    if (id) {
      setSearchParams({ envioId: String(id) });
    } else {
      setSearchParams({});
    }
  };

  const handleCreate = () => {
    setSelectedEvento(null);
    setIsFormOpen(true);
  };

  const handleEdit = (evento: EventoLogistico) => {
    setSelectedEvento(evento);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await eventoLogisticoService.delete(deleteId);
      toast.success('Evento eliminado correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar el evento');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: EventoLogisticoRequest) => {
    try {
      if (selectedEvento) {
        await eventoLogisticoService.update(selectedEvento.id, data);
        toast.success('Evento actualizado correctamente');
      } else {
        await eventoLogisticoService.create(data);
        toast.success('Evento registrado correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedEvento ? 'Error al actualizar' : 'Error al crear');
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const getTipoEventoVariant = (tipo: string) => {
    switch (tipo) {
      case 'CARGA':
        return 'secondary';
      case 'SALIDA_PLANTA':
        return 'default';
      case 'ARRIBO_PUERTO':
        return 'default';
      case 'CONSOLIDACION':
        return 'outline';
      case 'DESPACHO':
        return 'default';
      case 'ARRIBO_DESTINO':
        return 'success';
      default:
        return 'secondary';
    }
  };

  const columns: Column<EventoLogistico>[] = [
    {
      key: 'tipoEvento',
      header: 'Tipo',
      render: (evt) => (
        <Badge variant={getTipoEventoVariant(evt.tipoEvento)}>
          {TIPO_EVENTO_LABELS[evt.tipoEvento as keyof typeof TIPO_EVENTO_LABELS] || evt.tipoEvento}
        </Badge>
      ),
    },
    {
      key: 'fechaEvento',
      header: 'Fecha/Hora',
      render: (evt) => (
        <div>
          <div className="flex items-center gap-2">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <span>{formatDate(evt.fechaEvento)}</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Clock className="h-3 w-3" />
            <span>{evt.horaEvento}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'ubicacion',
      header: 'Ubicación',
      render: (evt) => (
        <div className="flex items-start gap-2">
          <MapPin className="h-4 w-4 text-muted-foreground mt-0.5" />
          <div>
            <div className="font-medium">{evt.ubicacion}</div>
            {(evt.ciudad || evt.pais) && (
              <div className="text-sm text-muted-foreground">
                {[evt.ciudad, evt.pais].filter(Boolean).join(', ')}
              </div>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'responsable',
      header: 'Responsable',
      render: (evt) => (
        <div>
          <div className="font-medium">{evt.responsable}</div>
          {evt.organizacion && (
            <div className="text-sm text-muted-foreground">{evt.organizacion}</div>
          )}
        </div>
      ),
    },
    {
      key: 'condiciones',
      header: 'Condiciones',
      render: (evt) =>
        evt.temperaturaRegistrada || evt.humedadRegistrada ? (
          <div className="flex gap-3">
            {evt.temperaturaRegistrada !== undefined && (
              <div className="flex items-center gap-1">
                <Thermometer className="h-4 w-4 text-muted-foreground" />
                <span>{evt.temperaturaRegistrada}°C</span>
              </div>
            )}
            {evt.humedadRegistrada !== undefined && (
              <div className="flex items-center gap-1">
                <Droplets className="h-4 w-4 text-muted-foreground" />
                <span>{evt.humedadRegistrada}%</span>
              </div>
            )}
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'incidencia',
      header: 'Incidencia',
      render: (evt) =>
        evt.incidencia ? (
          <Badge variant="destructive" className="gap-1">
            <AlertTriangle className="h-3 w-3" />
            Sí
          </Badge>
        ) : (
          <Badge variant="outline">No</Badge>
        ),
    },
    // Solo mostrar columna de acciones si el usuario tiene permisos
    ...(canUpdate('eventos') || canDelete('eventos') ? [{
      key: 'actions' as keyof EventoLogistico,
      header: '',
      className: 'w-[50px]',
      render: (evt: EventoLogistico) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {canUpdate('eventos') && (
              <DropdownMenuItem onClick={() => handleEdit(evt)}>
                <Pencil className="mr-2 h-4 w-4" />
                Editar
              </DropdownMenuItem>
            )}
            {canDelete('eventos') && (
              <>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => setDeleteId(evt.id)} className="text-destructive">
                  <Trash2 className="mr-2 h-4 w-4" />
                  Eliminar
                </DropdownMenuItem>
              </>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    }] : []),
  ];

  if (loadingEnvios) {
    return <PageLoader />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Eventos Logísticos</h2>
          <p className="text-muted-foreground">
            Registre y consulte los eventos de seguimiento de envíos
          </p>
        </div>
        <PermissionGate module="eventos" permission="create">
          <Button onClick={handleCreate} disabled={!selectedEnvioId}>
            <Plus className="mr-2 h-4 w-4" />
            Nuevo Evento
          </Button>
        </PermissionGate>
      </div>

      {/* Selector de Envío */}
      <div className="flex items-center gap-4">
        <div className="w-80">
          <Select
            value={selectedEnvioId ? String(selectedEnvioId) : ''}
            onValueChange={handleEnvioChange}
          >
            <SelectTrigger>
              <SelectValue placeholder="Seleccione un envío para ver sus eventos" />
            </SelectTrigger>
            <SelectContent>
              {envios?.map((env) => (
                <SelectItem key={env.id} value={String(env.id)}>
                  {env.codigoEnvio} - {env.paisDestino}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        {selectedEnvioId && eventos && (
          <Badge variant="outline">{eventos.length} eventos registrados</Badge>
        )}
      </div>

      {error && (
        <div className="flex flex-col items-center justify-center min-h-[200px]">
          <p className="text-destructive mb-4">{error}</p>
          <Button onClick={refetch}>Reintentar</Button>
        </div>
      )}

      {!selectedEnvioId ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <MapPin className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            Seleccione un envío para ver y gestionar sus eventos logísticos
          </p>
        </div>
      ) : loadingEventos ? (
        <PageLoader />
      ) : eventos && eventos.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <MapPin className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay eventos registrados para este envío. Registre el primer evento.
          </p>
        </div>
      ) : (
        <DataTable
          data={eventos || []}
          columns={columns}
          searchKeys={['ubicacion', 'responsable', 'ciudad', 'pais']}
          searchPlaceholder="Buscar por ubicación, responsable..."
        />
      )}

      <EventoFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        evento={selectedEvento}
        envioId={selectedEnvioId || undefined}
        onSubmit={handleFormSubmit}
      />

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar evento?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro del evento logístico.
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

export default EventosPage;
