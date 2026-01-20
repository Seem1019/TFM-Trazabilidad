import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import {
  Plus,
  Pencil,
  Trash2,
  Truck,
  MoreHorizontal,
  Calendar,
  MapPin,
  Ship,
  Plane,
  Package,
  FileText,
  Lock,
  Eye,
} from 'lucide-react';
import { envioService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import type { Envio, EnvioRequest } from '@/types';
import { ESTADO_ENVIO_LABELS } from '@/types';
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
import { EnvioFormDialog } from './components/EnvioFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Link } from 'react-router-dom';

export function EnviosPage() {
  const {
    data: envios,
    isLoading,
    error,
    refetch,
  } = useFetch<Envio[]>(useCallback(() => envioService.getAll(), []), []);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedEnvio, setSelectedEnvio] = useState<Envio | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [cerrarId, setCerrarId] = useState<number | null>(null);

  const handleCreate = () => {
    setSelectedEnvio(null);
    setIsFormOpen(true);
  };

  const handleEdit = (envio: Envio) => {
    setSelectedEnvio(envio);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await envioService.delete(deleteId);
      toast.success('Envío eliminado correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar el envío');
    } finally {
      setDeleteId(null);
    }
  };

  const handleCerrar = async () => {
    if (!cerrarId) return;
    try {
      await envioService.cerrar(cerrarId);
      toast.success('Envío cerrado correctamente. Hash de integridad generado.');
      refetch();
    } catch {
      toast.error('Error al cerrar el envío');
    } finally {
      setCerrarId(null);
    }
  };

  const handleFormSubmit = async (data: EnvioRequest) => {
    try {
      if (selectedEnvio) {
        await envioService.update(selectedEnvio.id, data);
        toast.success('Envío actualizado correctamente');
      } else {
        await envioService.create(data);
        toast.success('Envío creado correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedEnvio ? 'Error al actualizar' : 'Error al crear');
    }
  };

  const handleCambiarEstado = async (id: number, estado: string) => {
    try {
      await envioService.cambiarEstado(id, estado);
      toast.success(`Estado cambiado a ${ESTADO_ENVIO_LABELS[estado as keyof typeof ESTADO_ENVIO_LABELS] || estado}`);
      refetch();
    } catch {
      toast.error('Error al cambiar estado');
    }
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-';
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const getEstadoVariant = (estado: string) => {
    switch (estado) {
      case 'CREADO':
        return 'secondary';
      case 'EN_PREPARACION':
        return 'secondary';
      case 'LISTO_ENVIO':
        return 'warning';
      case 'EN_TRANSITO':
        return 'default';
      case 'EN_PUERTO_ORIGEN':
      case 'EN_PUERTO_DESTINO':
        return 'default';
      case 'ENTREGADO':
        return 'success';
      case 'CERRADO':
        return 'outline';
      case 'CANCELADO':
        return 'destructive';
      default:
        return 'secondary';
    }
  };

  const getTransporteIcon = (tipo: string) => {
    switch (tipo) {
      case 'MARITIMO':
        return <Ship className="h-4 w-4" />;
      case 'AEREO':
        return <Plane className="h-4 w-4" />;
      default:
        return <Truck className="h-4 w-4" />;
    }
  };

  const columns: Column<Envio>[] = [
    {
      key: 'codigoEnvio',
      header: 'Código',
      render: (env) => <span className="font-mono font-medium">{env.codigoEnvio}</span>,
    },
    {
      key: 'fechaCreacion',
      header: 'Fecha',
      render: (env) => (
        <div className="flex items-center gap-2">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{formatDate(env.fechaCreacion)}</span>
        </div>
      ),
    },
    {
      key: 'destino',
      header: 'Destino',
      render: (env) => (
        <div className="flex items-center gap-2">
          <MapPin className="h-4 w-4 text-muted-foreground" />
          <div>
            <div className="font-medium">{env.paisDestino}</div>
            {env.puertoDestino && (
              <div className="text-sm text-muted-foreground">{env.puertoDestino}</div>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'tipoTransporte',
      header: 'Transporte',
      render: (env) => (
        <div className="flex items-center gap-2">
          {getTransporteIcon(env.tipoTransporte)}
          <span>{env.tipoTransporte}</span>
        </div>
      ),
    },
    {
      key: 'contenedor',
      header: 'Contenedor',
      render: (env) =>
        env.codigoContenedor ? (
          <div>
            <div className="font-mono text-sm">{env.codigoContenedor}</div>
            {env.tipoContenedor && (
              <div className="text-xs text-muted-foreground">{env.tipoContenedor}</div>
            )}
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'pallets',
      header: 'Carga',
      render: (env) => (
        <div className="flex items-center gap-2">
          <Package className="h-4 w-4 text-muted-foreground" />
          <div>
            <div>{env.numeroPallets || 0} pallets</div>
            <div className="text-sm text-muted-foreground">{env.numeroCajas || 0} cajas</div>
          </div>
        </div>
      ),
    },
    {
      key: 'eventos',
      header: 'Eventos',
      render: (env) => (
        <div className="flex gap-2">
          <Badge variant="outline" className="gap-1">
            <FileText className="h-3 w-3" />
            {env.numeroEventos}
          </Badge>
          <Badge variant="outline" className="gap-1">
            <FileText className="h-3 w-3" />
            {env.numeroDocumentos}
          </Badge>
        </div>
      ),
    },
    {
      key: 'estado',
      header: 'Estado',
      render: (env) => (
        <div className="flex items-center gap-2">
          <Badge variant={getEstadoVariant(env.estado)}>
            {ESTADO_ENVIO_LABELS[env.estado as keyof typeof ESTADO_ENVIO_LABELS] ||
              env.estado?.replace('_', ' ')}
          </Badge>
          {env.hashCierre && <Lock className="h-3 w-3 text-muted-foreground" />}
        </div>
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (env) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem asChild>
              <Link to={`/envios/${env.id}`}>
                <Eye className="mr-2 h-4 w-4" />
                Ver Detalle
              </Link>
            </DropdownMenuItem>
            {!env.hashCierre && (
              <>
                <DropdownMenuItem onClick={() => handleEdit(env)}>
                  <Pencil className="mr-2 h-4 w-4" />
                  Editar
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                {/* Cambios de estado según el estado actual */}
                {env.estado === 'CREADO' && (
                  <DropdownMenuItem onClick={() => handleCambiarEstado(env.id, 'EN_PREPARACION')}>
                    Pasar a En Preparación
                  </DropdownMenuItem>
                )}
                {env.estado === 'EN_PREPARACION' && (
                  <DropdownMenuItem onClick={() => handleCambiarEstado(env.id, 'LISTO_ENVIO')}>
                    Marcar Listo para Envío
                  </DropdownMenuItem>
                )}
                {env.estado === 'LISTO_ENVIO' && (
                  <DropdownMenuItem onClick={() => handleCambiarEstado(env.id, 'EN_TRANSITO')}>
                    Iniciar Tránsito
                  </DropdownMenuItem>
                )}
                {env.estado === 'EN_TRANSITO' && (
                  <>
                    <DropdownMenuItem onClick={() => handleCambiarEstado(env.id, 'EN_PUERTO_ORIGEN')}>
                      Arribo Puerto Origen
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleCambiarEstado(env.id, 'EN_PUERTO_DESTINO')}>
                      Arribo Puerto Destino
                    </DropdownMenuItem>
                  </>
                )}
                {(env.estado === 'EN_PUERTO_DESTINO' || env.estado === 'EN_TRANSITO') && (
                  <DropdownMenuItem onClick={() => handleCambiarEstado(env.id, 'ENTREGADO')}>
                    Marcar como Entregado
                  </DropdownMenuItem>
                )}
                {env.estado === 'ENTREGADO' && (
                  <DropdownMenuItem onClick={() => setCerrarId(env.id)}>
                    <Lock className="mr-2 h-4 w-4" />
                    Cerrar Envío
                  </DropdownMenuItem>
                )}
                <DropdownMenuSeparator />
              </>
            )}
            {!env.hashCierre && (
              <DropdownMenuItem onClick={() => setDeleteId(env.id)} className="text-destructive">
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

  // Estadísticas rápidas
  const enTransito = envios?.filter((e) => e.estado === 'EN_TRANSITO').length || 0;
  const entregados = envios?.filter((e) => e.estado === 'ENTREGADO').length || 0;
  const cerrados = envios?.filter((e) => e.hashCierre).length || 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Envíos</h2>
          <p className="text-muted-foreground">Gestione los envíos de exportación</p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="mr-2 h-4 w-4" />
          Nuevo Envío
        </Button>
      </div>

      {/* Estadísticas rápidas */}
      {envios && envios.length > 0 && (
        <div className="flex gap-4">
          <Badge variant="default" className="px-3 py-1">
            {enTransito} en tránsito
          </Badge>
          <Badge variant="success" className="px-3 py-1">
            {entregados} entregados
          </Badge>
          <Badge variant="outline" className="px-3 py-1">
            {cerrados} cerrados
          </Badge>
        </div>
      )}

      {envios && envios.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Truck className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay envíos registrados. Cree un nuevo envío para comenzar.
          </p>
        </div>
      ) : (
        <DataTable
          data={envios || []}
          columns={columns}
          searchKeys={['codigoEnvio', 'paisDestino', 'puertoDestino', 'codigoContenedor']}
          searchPlaceholder="Buscar por código, destino o contenedor..."
        />
      )}

      <EnvioFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        envio={selectedEnvio}
        onSubmit={handleFormSubmit}
      />

      {/* Dialog eliminar */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar envío?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro del envío y todos sus
              eventos y documentos asociados.
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

      {/* Dialog cerrar envío */}
      <AlertDialog open={!!cerrarId} onOpenChange={() => setCerrarId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Cerrar envío?</AlertDialogTitle>
            <AlertDialogDescription>
              Al cerrar el envío se generará un hash de integridad (SHA-256) que garantiza la
              inmutabilidad de los datos. Esta acción NO se puede deshacer y el envío ya no podrá
              ser modificado.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleCerrar}>
              <Lock className="mr-2 h-4 w-4" />
              Cerrar Envío
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default EnviosPage;
