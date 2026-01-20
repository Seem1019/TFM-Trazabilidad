import { useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';
import {
  Plus,
  Pencil,
  Trash2,
  FileText,
  MoreHorizontal,
  Calendar,
  CheckCircle,
  XCircle,
  AlertTriangle,
  ExternalLink,
  DollarSign,
} from 'lucide-react';
import { documentoExportacionService, envioService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import type { DocumentoExportacion, DocumentoExportacionRequest, Envio } from '@/types';
import { TIPO_DOCUMENTO_LABELS } from '@/types';
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
import { DataTable, type Column, PageLoader } from '@/components/shared';
import { DocumentoFormDialog } from './components/DocumentoFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function DocumentosPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const envioIdParam = searchParams.get('envioId');
  const [selectedEnvioId, setSelectedEnvioId] = useState<number | null>(
    envioIdParam ? Number(envioIdParam) : null
  );

  const { data: envios, isLoading: loadingEnvios } = useFetch<Envio[]>(
    useCallback(() => envioService.getAll(), []),
    []
  );

  const {
    data: documentos,
    isLoading: loadingDocumentos,
    error,
    refetch,
  } = useFetch<DocumentoExportacion[]>(
    useCallback(
      () =>
        selectedEnvioId
          ? documentoExportacionService.getByEnvio(selectedEnvioId)
          : Promise.resolve([]),
      [selectedEnvioId]
    ),
    [selectedEnvioId]
  );

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedDocumento, setSelectedDocumento] = useState<DocumentoExportacion | null>(null);
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
    setSelectedDocumento(null);
    setIsFormOpen(true);
  };

  const handleEdit = (documento: DocumentoExportacion) => {
    setSelectedDocumento(documento);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await documentoExportacionService.delete(deleteId);
      toast.success('Documento eliminado correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar el documento');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: DocumentoExportacionRequest) => {
    try {
      if (selectedDocumento) {
        await documentoExportacionService.update(selectedDocumento.id, data);
        toast.success('Documento actualizado correctamente');
      } else {
        await documentoExportacionService.create(data);
        toast.success('Documento registrado correctamente');
      }
      setIsFormOpen(false);
      refetch();
    } catch {
      toast.error(selectedDocumento ? 'Error al actualizar' : 'Error al crear');
    }
  };

  const handleCambiarEstado = async (id: number, estado: string) => {
    try {
      await documentoExportacionService.cambiarEstado(id, estado);
      toast.success(`Estado cambiado a ${estado}`);
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
      case 'APROBADO':
        return 'success';
      case 'RECHAZADO':
        return 'destructive';
      case 'VENCIDO':
        return 'warning';
      case 'PENDIENTE':
      default:
        return 'secondary';
    }
  };

  const getEstadoIcon = (estado: string) => {
    switch (estado) {
      case 'APROBADO':
        return <CheckCircle className="h-3 w-3" />;
      case 'RECHAZADO':
        return <XCircle className="h-3 w-3" />;
      case 'VENCIDO':
        return <AlertTriangle className="h-3 w-3" />;
      default:
        return null;
    }
  };

  const columns: Column<DocumentoExportacion>[] = [
    {
      key: 'tipoDocumento',
      header: 'Tipo',
      render: (doc) => (
        <div className="flex items-center gap-2">
          <FileText className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium">
            {TIPO_DOCUMENTO_LABELS[doc.tipoDocumento as keyof typeof TIPO_DOCUMENTO_LABELS] ||
              doc.tipoDocumento}
          </span>
          {doc.obligatorio && (
            <Badge variant="outline" className="text-xs">
              Obligatorio
            </Badge>
          )}
        </div>
      ),
    },
    {
      key: 'numeroDocumento',
      header: 'Número',
      render: (doc) => <span className="font-mono">{doc.numeroDocumento}</span>,
    },
    {
      key: 'fechaEmision',
      header: 'Emisión',
      render: (doc) => (
        <div className="flex items-center gap-2">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{formatDate(doc.fechaEmision)}</span>
        </div>
      ),
    },
    {
      key: 'fechaVencimiento',
      header: 'Vencimiento',
      render: (doc) => (
        <div>
          <span className={doc.estaVencido ? 'text-destructive' : ''}>
            {formatDate(doc.fechaVencimiento)}
          </span>
          {doc.estaVencido && (
            <Badge variant="destructive" className="ml-2 text-xs">
              Vencido
            </Badge>
          )}
        </div>
      ),
    },
    {
      key: 'entidadEmisora',
      header: 'Entidad',
      render: (doc) => doc.entidadEmisora || '-',
    },
    {
      key: 'valor',
      header: 'Valor',
      render: (doc) =>
        doc.valorDeclarado ? (
          <div className="flex items-center gap-1">
            <DollarSign className="h-4 w-4 text-muted-foreground" />
            <span>
              {doc.valorDeclarado.toLocaleString()} {doc.moneda}
            </span>
          </div>
        ) : (
          '-'
        ),
    },
    {
      key: 'archivo',
      header: 'Archivo',
      render: (doc) =>
        doc.tieneArchivo && doc.urlArchivo ? (
          <Button variant="ghost" size="sm" asChild>
            <a href={doc.urlArchivo} target="_blank" rel="noopener noreferrer">
              <ExternalLink className="h-4 w-4" />
            </a>
          </Button>
        ) : (
          <Badge variant="outline" className="text-xs">
            Sin archivo
          </Badge>
        ),
    },
    {
      key: 'estado',
      header: 'Estado',
      render: (doc) => (
        <Badge variant={getEstadoVariant(doc.estado)} className="gap-1">
          {getEstadoIcon(doc.estado)}
          {doc.estado}
        </Badge>
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (doc) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleEdit(doc)}>
              <Pencil className="mr-2 h-4 w-4" />
              Editar
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            {doc.estado === 'PENDIENTE' && (
              <>
                <DropdownMenuItem onClick={() => handleCambiarEstado(doc.id, 'APROBADO')}>
                  <CheckCircle className="mr-2 h-4 w-4" />
                  Aprobar
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => handleCambiarEstado(doc.id, 'RECHAZADO')}>
                  <XCircle className="mr-2 h-4 w-4" />
                  Rechazar
                </DropdownMenuItem>
              </>
            )}
            {doc.estado === 'RECHAZADO' && (
              <DropdownMenuItem onClick={() => handleCambiarEstado(doc.id, 'PENDIENTE')}>
                Volver a Pendiente
              </DropdownMenuItem>
            )}
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => setDeleteId(doc.id)} className="text-destructive">
              <Trash2 className="mr-2 h-4 w-4" />
              Eliminar
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    },
  ];

  if (loadingEnvios) {
    return <PageLoader />;
  }

  // Estadísticas
  const pendientes = documentos?.filter((d) => d.estado === 'PENDIENTE').length || 0;
  const aprobados = documentos?.filter((d) => d.estado === 'APROBADO').length || 0;
  const vencidos = documentos?.filter((d) => d.estaVencido).length || 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Documentos de Exportación</h2>
          <p className="text-muted-foreground">
            Gestione los documentos requeridos para la exportación
          </p>
        </div>
        <Button onClick={handleCreate} disabled={!selectedEnvioId}>
          <Plus className="mr-2 h-4 w-4" />
          Nuevo Documento
        </Button>
      </div>

      {/* Selector de Envío */}
      <div className="flex items-center gap-4">
        <div className="w-80">
          <Select
            value={selectedEnvioId ? String(selectedEnvioId) : ''}
            onValueChange={handleEnvioChange}
          >
            <SelectTrigger>
              <SelectValue placeholder="Seleccione un envío para ver sus documentos" />
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
        {selectedEnvioId && documentos && documentos.length > 0 && (
          <div className="flex gap-2">
            <Badge variant="secondary">{pendientes} pendientes</Badge>
            <Badge variant="success">{aprobados} aprobados</Badge>
            {vencidos > 0 && <Badge variant="destructive">{vencidos} vencidos</Badge>}
          </div>
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
          <FileText className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            Seleccione un envío para ver y gestionar sus documentos de exportación
          </p>
        </div>
      ) : loadingDocumentos ? (
        <PageLoader />
      ) : documentos && documentos.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <FileText className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            No hay documentos registrados para este envío. Agregue los documentos requeridos.
          </p>
        </div>
      ) : (
        <DataTable
          data={documentos || []}
          columns={columns}
          searchKeys={['numeroDocumento', 'tipoDocumento', 'entidadEmisora']}
          searchPlaceholder="Buscar por número, tipo o entidad..."
        />
      )}

      <DocumentoFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        documento={selectedDocumento}
        envioId={selectedEnvioId || undefined}
        onSubmit={handleFormSubmit}
      />

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar documento?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro del documento de
              exportación.
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

export default DocumentosPage;
