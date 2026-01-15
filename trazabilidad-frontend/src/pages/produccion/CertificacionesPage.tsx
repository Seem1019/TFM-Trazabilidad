import { useState, useCallback, useEffect } from 'react';
import { toast } from 'sonner';
import { Plus, Pencil, Trash2, Award, MoreHorizontal, AlertTriangle, MapPin } from 'lucide-react';
import { certificacionService, fincaService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import type { Certificacion, CertificacionRequest, Finca } from '@/types';
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
import { CertificacionFormDialog } from './components/CertificacionFormDialog';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export function CertificacionesPage() {
  const [selectedFincaId, setSelectedFincaId] = useState<string>('');
  const [certificaciones, setCertificaciones] = useState<Certificacion[]>([]);
  const [isLoadingCerts, setIsLoadingCerts] = useState(false);

  const { data: fincas, isLoading: isLoadingFincas, error: errorFincas } = useFetch<Finca[]>(
    useCallback(() => fincaService.getAll(), []),
    []
  );

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedCertificacion, setSelectedCertificacion] = useState<Certificacion | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  // Cargar certificaciones cuando se selecciona una finca
  useEffect(() => {
    const loadCertificaciones = async () => {
      if (!selectedFincaId) {
        setCertificaciones([]);
        return;
      }

      setIsLoadingCerts(true);
      try {
        const data = await certificacionService.getByFinca(Number(selectedFincaId));
        setCertificaciones(data);
      } catch {
        toast.error('Error al cargar certificaciones');
        setCertificaciones([]);
      } finally {
        setIsLoadingCerts(false);
      }
    };

    loadCertificaciones();
  }, [selectedFincaId]);

  const refetch = async () => {
    if (selectedFincaId) {
      setIsLoadingCerts(true);
      try {
        const data = await certificacionService.getByFinca(Number(selectedFincaId));
        setCertificaciones(data);
      } catch {
        toast.error('Error al cargar certificaciones');
      } finally {
        setIsLoadingCerts(false);
      }
    }
  };

  const handleCreate = () => {
    setSelectedCertificacion(null);
    setIsFormOpen(true);
  };

  const handleEdit = (cert: Certificacion) => {
    setSelectedCertificacion(cert);
    setIsFormOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await certificacionService.delete(deleteId);
      toast.success('Certificación eliminada correctamente');
      refetch();
    } catch {
      toast.error('Error al eliminar la certificación');
    } finally {
      setDeleteId(null);
    }
  };

  const handleFormSubmit = async (data: CertificacionRequest) => {
    try {
      if (selectedCertificacion) {
        await certificacionService.update(selectedCertificacion.id, data);
        toast.success('Certificación actualizada correctamente');
      } else {
        await certificacionService.create(data);
        toast.success('Certificación registrada correctamente');
        // Si se crea para la finca seleccionada, actualizar la lista
        if (data.fincaId === Number(selectedFincaId)) {
          refetch();
        } else {
          // Si se crea para otra finca, cambiar la selección
          setSelectedFincaId(String(data.fincaId));
        }
      }
      setIsFormOpen(false);
      if (selectedCertificacion) {
        refetch();
      }
    } catch {
      toast.error(selectedCertificacion ? 'Error al actualizar' : 'Error al registrar');
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
      case 'VIGENTE':
        return 'success';
      case 'VENCIDA':
        return 'destructive';
      case 'SUSPENDIDA':
        return 'warning';
      default:
        return 'secondary';
    }
  };

  const columns: Column<Certificacion>[] = [
    {
      key: 'tipoCertificacion',
      header: 'Certificación',
      render: (cert) => (
        <div className="flex items-center gap-2">
          <Award className="h-4 w-4 text-primary" />
          <span className="font-medium">{cert.tipoCertificacion?.replace(/_/g, ' ')}</span>
        </div>
      ),
    },
    {
      key: 'entidadEmisora',
      header: 'Entidad',
    },
    {
      key: 'numeroCertificado',
      header: 'N° Certificado',
      render: (cert) => cert.numeroCertificado || '-',
    },
    {
      key: 'fechaEmision',
      header: 'Emisión',
      render: (cert) => formatDate(cert.fechaEmision),
    },
    {
      key: 'fechaVencimiento',
      header: 'Vencimiento',
      render: (cert) => (
        <div className="flex items-center gap-2">
          <span>{formatDate(cert.fechaVencimiento)}</span>
          {cert.diasParaVencer <= 30 && cert.diasParaVencer > 0 && (
            <AlertTriangle className="h-4 w-4 text-yellow-500" />
          )}
        </div>
      ),
    },
    {
      key: 'estado',
      header: 'Estado',
      render: (cert) => (
        <Badge variant={getEstadoVariant(cert.estado)}>
          {cert.estado}
        </Badge>
      ),
    },
    {
      key: 'diasParaVencer',
      header: 'Días',
      render: (cert) => (
        cert.diasParaVencer > 0 ? (
          <span className={cert.diasParaVencer <= 30 ? 'text-yellow-600 font-medium' : ''}>
            {cert.diasParaVencer} días
          </span>
        ) : (
          <span className="text-red-600">Vencida</span>
        )
      ),
    },
    {
      key: 'actions',
      header: '',
      className: 'w-[50px]',
      render: (cert) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleEdit(cert)}>
              <Pencil className="mr-2 h-4 w-4" />
              Editar
            </DropdownMenuItem>
            <DropdownMenuItem
              onClick={() => setDeleteId(cert.id)}
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

  if (isLoadingFincas) {
    return <PageLoader />;
  }

  if (errorFincas) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px]">
        <p className="text-destructive mb-4">{errorFincas}</p>
        <Button onClick={() => window.location.reload()}>Reintentar</Button>
      </div>
    );
  }

  // Alertas de certificaciones próximas a vencer
  const proximasVencer = certificaciones?.filter(c => c.diasParaVencer <= 30 && c.diasParaVencer > 0) || [];
  const selectedFinca = fincas?.find(f => f.id === Number(selectedFincaId));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Certificaciones</h2>
          <p className="text-muted-foreground">
            Gestione las certificaciones de calidad de sus fincas
          </p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="mr-2 h-4 w-4" />
          Nueva Certificación
        </Button>
      </div>

      {/* Selector de Finca */}
      <div className="flex items-center gap-4 p-4 rounded-lg border bg-card">
        <div className="flex items-center gap-2 text-muted-foreground">
          <MapPin className="h-5 w-5" />
          <span className="font-medium">Seleccione una finca:</span>
        </div>
        <Select value={selectedFincaId} onValueChange={setSelectedFincaId}>
          <SelectTrigger className="w-[300px]">
            <SelectValue placeholder="Seleccione una finca para ver sus certificaciones" />
          </SelectTrigger>
          <SelectContent>
            {fincas?.map((finca) => (
              <SelectItem key={finca.id} value={String(finca.id)}>
                {finca.nombre} ({finca.codigoFinca})
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {selectedFinca && (
          <Badge variant="outline" className="ml-2">
            {selectedFinca.totalCertificacionesVigentes} certificaciones vigentes
          </Badge>
        )}
      </div>

      {/* Contenido condicional */}
      {!selectedFincaId ? (
        <div className="flex flex-col items-center justify-center min-h-[300px] rounded-lg border border-dashed">
          <Award className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground text-center">
            Seleccione una finca para ver y gestionar sus certificaciones
          </p>
        </div>
      ) : isLoadingCerts ? (
        <PageLoader />
      ) : (
        <>
          {/* Alerta de certificaciones próximas a vencer */}
          {proximasVencer.length > 0 && (
            <div className="rounded-lg border border-yellow-200 bg-yellow-50 p-4">
              <div className="flex items-center gap-2 text-yellow-800">
                <AlertTriangle className="h-5 w-5" />
                <span className="font-medium">
                  {proximasVencer.length} certificación(es) próxima(s) a vencer en los próximos 30 días
                </span>
              </div>
            </div>
          )}

          <DataTable
            data={certificaciones}
            columns={columns}
            searchKeys={['tipoCertificacion', 'entidadEmisora', 'numeroCertificado']}
            searchPlaceholder="Buscar por tipo, entidad o número..."
          />
        </>
      )}

      {/* Form Dialog */}
      <CertificacionFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        certificacion={selectedCertificacion}
        fincas={fincas || []}
        onSubmit={handleFormSubmit}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar certificación?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. Se eliminará el registro de esta certificación.
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

export default CertificacionesPage;
