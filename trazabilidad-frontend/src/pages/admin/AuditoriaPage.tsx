import { useState, useCallback } from 'react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Shield,
  AlertTriangle,
  Info,
  Filter,
  X,
  FileText,
  User,
  Clock,
  Globe,
} from 'lucide-react';
import { auditoriaService } from '@/services/auditoriaService';
import type { AuditoriaEvento, AuditoriaFiltros } from '@/services/auditoriaService';
import { useFetch } from '@/hooks/useFetch';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { DataTable, type Column, PageLoader } from '@/components/shared';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

const MODULOS = [
  { value: 'PRODUCCION', label: 'Producción' },
  { value: 'EMPAQUE', label: 'Empaque' },
  { value: 'LOGISTICA', label: 'Logística' },
  { value: 'ADMINISTRACION', label: 'Administración' },
  { value: 'SISTEMA', label: 'Sistema' },
];

const OPERACIONES = [
  { value: 'CREATE', label: 'Creación' },
  { value: 'UPDATE', label: 'Actualización' },
  { value: 'DELETE', label: 'Eliminación' },
  { value: 'CLOSE', label: 'Cierre' },
];

const CRITICIDADES = [
  { value: 'INFO', label: 'Info' },
  { value: 'WARNING', label: 'Advertencia' },
  { value: 'CRITICAL', label: 'Crítico' },
];

export function AuditoriaPage() {
  const [filtros, setFiltros] = useState<AuditoriaFiltros>({});
  const [filtrosAplicados, setFiltrosAplicados] = useState<AuditoriaFiltros>({});
  const [detalleEvento, setDetalleEvento] = useState<AuditoriaEvento | null>(null);

  const {
    data: eventos,
    isLoading,
    error,
    refetch,
  } = useFetch<AuditoriaEvento[]>(
    useCallback(() => auditoriaService.listar(filtrosAplicados), [filtrosAplicados]),
    [filtrosAplicados]
  );

  const aplicarFiltros = () => {
    setFiltrosAplicados({ ...filtros });
  };

  const limpiarFiltros = () => {
    setFiltros({});
    setFiltrosAplicados({});
  };

  const hayFiltrosActivos = Object.values(filtrosAplicados).some(
    (v) => v !== undefined && v !== ''
  );

  const formatDateTime = (dateStr: string) => {
    try {
      return format(new Date(dateStr), "dd MMM yyyy HH:mm:ss", { locale: es });
    } catch (error) {
      return dateStr;
    }
  };

  const getOperacionBadge = (operacion: string) => {
    switch (operacion) {
      case 'CREATE':
        return <Badge variant="success">Creación</Badge>;
      case 'UPDATE':
        return <Badge variant="secondary">Actualización</Badge>;
      case 'DELETE':
        return <Badge variant="destructive">Eliminación</Badge>;
      case 'CLOSE':
        return <Badge variant="default">Cierre</Badge>;
      default:
        return <Badge variant="outline">{operacion}</Badge>;
    }
  };

  const getCriticidadIcon = (nivel: string) => {
    switch (nivel) {
      case 'CRITICAL':
        return <AlertTriangle className="h-4 w-4 text-red-500" />;
      case 'WARNING':
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      default:
        return <Info className="h-4 w-4 text-blue-500" />;
    }
  };

  const getModuloLabel = (modulo: string) => {
    return MODULOS.find((m) => m.value === modulo)?.label || modulo;
  };

  const columns: Column<AuditoriaEvento>[] = [
    {
      key: 'fechaEvento',
      header: 'Fecha',
      render: (e) => (
        <div className="flex items-center gap-1 text-sm">
          <Clock className="h-3 w-3 text-muted-foreground" />
          {formatDateTime(e.fechaEvento)}
        </div>
      ),
    },
    {
      key: 'usuarioNombre',
      header: 'Usuario',
      render: (e) => (
        <div className="flex items-center gap-1 text-sm">
          <User className="h-3 w-3 text-muted-foreground" />
          {e.usuarioNombre}
        </div>
      ),
    },
    {
      key: 'tipoOperacion',
      header: 'Operación',
      render: (e) => getOperacionBadge(e.tipoOperacion),
    },
    {
      key: 'modulo',
      header: 'Módulo',
      render: (e) => (
        <span className="text-sm">{getModuloLabel(e.modulo)}</span>
      ),
    },
    {
      key: 'descripcionOperacion',
      header: 'Descripción',
      render: (e) => (
        <span className="text-sm max-w-[300px] truncate block">
          {e.descripcionOperacion}
        </span>
      ),
    },
    {
      key: 'nivelCriticidad',
      header: 'Nivel',
      render: (e) => (
        <div className="flex items-center gap-1">
          {getCriticidadIcon(e.nivelCriticidad)}
          <span className="text-sm">{e.nivelCriticidad}</span>
        </div>
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (e) => (
        <Button variant="ghost" size="icon" onClick={() => setDetalleEvento(e)}>
          <FileText className="h-4 w-4" />
        </Button>
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
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Auditoría del Sistema</h2>
        <p className="text-muted-foreground">
          Registro de todas las operaciones realizadas en el sistema
        </p>
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap items-end gap-3 rounded-lg border p-4">
        <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
          <Filter className="h-4 w-4" />
          Filtros
        </div>

        <div className="flex-1 min-w-[140px] max-w-[180px]">
          <Select
            value={filtros.modulo || ''}
            onValueChange={(v) => setFiltros({ ...filtros, modulo: v || undefined })}
          >
            <SelectTrigger className="h-9">
              <SelectValue placeholder="Módulo" />
            </SelectTrigger>
            <SelectContent>
              {MODULOS.map((m) => (
                <SelectItem key={m.value} value={m.value}>
                  {m.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex-1 min-w-[140px] max-w-[180px]">
          <Select
            value={filtros.tipoOperacion || ''}
            onValueChange={(v) => setFiltros({ ...filtros, tipoOperacion: v || undefined })}
          >
            <SelectTrigger className="h-9">
              <SelectValue placeholder="Operación" />
            </SelectTrigger>
            <SelectContent>
              {OPERACIONES.map((o) => (
                <SelectItem key={o.value} value={o.value}>
                  {o.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex-1 min-w-[140px] max-w-[180px]">
          <Select
            value={filtros.nivelCriticidad || ''}
            onValueChange={(v) => setFiltros({ ...filtros, nivelCriticidad: v || undefined })}
          >
            <SelectTrigger className="h-9">
              <SelectValue placeholder="Criticidad" />
            </SelectTrigger>
            <SelectContent>
              {CRITICIDADES.map((c) => (
                <SelectItem key={c.value} value={c.value}>
                  {c.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex-1 min-w-[130px] max-w-[160px]">
          <Input
            type="date"
            className="h-9"
            placeholder="Desde"
            value={filtros.desde || ''}
            onChange={(e) => setFiltros({ ...filtros, desde: e.target.value || undefined })}
          />
        </div>

        <div className="flex-1 min-w-[130px] max-w-[160px]">
          <Input
            type="date"
            className="h-9"
            placeholder="Hasta"
            value={filtros.hasta || ''}
            onChange={(e) => setFiltros({ ...filtros, hasta: e.target.value || undefined })}
          />
        </div>

        <Button size="sm" onClick={aplicarFiltros}>
          Aplicar
        </Button>
        {hayFiltrosActivos && (
          <Button size="sm" variant="ghost" onClick={limpiarFiltros}>
            <X className="mr-1 h-3 w-3" />
            Limpiar
          </Button>
        )}
      </div>

      {/* Estadísticas rápidas */}
      {eventos && eventos.length > 0 && (
        <div className="flex gap-4 flex-wrap">
          <Badge variant="outline" className="px-3 py-1">
            {eventos.length} eventos
          </Badge>
          <Badge variant="success" className="px-3 py-1">
            {eventos.filter((e) => e.tipoOperacion === 'CREATE').length} creaciones
          </Badge>
          <Badge variant="secondary" className="px-3 py-1">
            {eventos.filter((e) => e.tipoOperacion === 'UPDATE').length} actualizaciones
          </Badge>
          <Badge variant="destructive" className="px-3 py-1">
            {eventos.filter((e) => e.nivelCriticidad === 'CRITICAL').length} críticos
          </Badge>
        </div>
      )}

      {/* Tabla */}
      {eventos && eventos.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Shield className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No se encontraron eventos de auditoría con los filtros seleccionados.
          </p>
        </div>
      ) : (
        <DataTable
          data={eventos || []}
          columns={columns}
          searchKeys={['descripcionOperacion', 'usuarioNombre', 'codigoEntidad'] as never[]}
          searchPlaceholder="Buscar por descripción, usuario o código..."
          pageSize={15}
        />
      )}

      {/* Modal de detalle */}
      <Dialog open={!!detalleEvento} onOpenChange={() => setDetalleEvento(null)}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Detalle del Evento de Auditoría</DialogTitle>
          </DialogHeader>
          {detalleEvento && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Fecha</p>
                  <p className="text-sm">{formatDateTime(detalleEvento.fechaEvento)}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Usuario</p>
                  <p className="text-sm">{detalleEvento.usuarioNombre} ({detalleEvento.usuarioEmail})</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Operación</p>
                  <div>{getOperacionBadge(detalleEvento.tipoOperacion)}</div>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Módulo</p>
                  <p className="text-sm">{getModuloLabel(detalleEvento.modulo)}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Entidad</p>
                  <p className="text-sm">{detalleEvento.tipoEntidad} - {detalleEvento.codigoEntidad || `ID ${detalleEvento.entidadId}`}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Criticidad</p>
                  <div className="flex items-center gap-1">
                    {getCriticidadIcon(detalleEvento.nivelCriticidad)}
                    <span className="text-sm">{detalleEvento.nivelCriticidad}</span>
                  </div>
                </div>
              </div>

              <div>
                <p className="text-sm font-medium text-muted-foreground">Descripción</p>
                <p className="text-sm">{detalleEvento.descripcionOperacion}</p>
              </div>

              {detalleEvento.ipOrigen && (
                <div className="flex items-center gap-1">
                  <Globe className="h-3 w-3 text-muted-foreground" />
                  <p className="text-sm text-muted-foreground">IP: {detalleEvento.ipOrigen}</p>
                </div>
              )}

              {detalleEvento.camposModificados && (
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Campos modificados</p>
                  <p className="text-sm font-mono bg-muted rounded p-2">{detalleEvento.camposModificados}</p>
                </div>
              )}

              {detalleEvento.datosAnteriores && (
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Datos anteriores</p>
                  <pre className="text-xs bg-muted rounded p-2 overflow-x-auto whitespace-pre-wrap">
                    {detalleEvento.datosAnteriores}
                  </pre>
                </div>
              )}

              {detalleEvento.datosNuevos && (
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Datos nuevos</p>
                  <pre className="text-xs bg-muted rounded p-2 overflow-x-auto whitespace-pre-wrap">
                    {detalleEvento.datosNuevos}
                  </pre>
                </div>
              )}

              {detalleEvento.enCadena && (
                <div className="rounded-lg border p-3 bg-muted/50">
                  <p className="text-sm font-medium">Blockchain</p>
                  <p className="text-xs font-mono text-muted-foreground break-all mt-1">
                    Hash: {detalleEvento.hashEvento}
                  </p>
                  {detalleEvento.hashAnterior && (
                    <p className="text-xs font-mono text-muted-foreground break-all">
                      Hash anterior: {detalleEvento.hashAnterior}
                    </p>
                  )}
                  {detalleEvento.integridadVerificada !== null && (
                    <Badge
                      variant={detalleEvento.integridadVerificada ? 'success' : 'destructive'}
                      className="mt-2"
                    >
                      {detalleEvento.integridadVerificada ? 'Integridad verificada' : 'Integridad comprometida'}
                    </Badge>
                  )}
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default AuditoriaPage;