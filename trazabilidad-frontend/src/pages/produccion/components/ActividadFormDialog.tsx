import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { ActividadAgronomica, ActividadAgronomicarRequest, Lote, TipoActividad } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

const TIPOS_ACTIVIDAD: TipoActividad[] = [
  'FERTILIZACION',
  'FUMIGACION',
  'RIEGO',
  'PODA',
  'DESHIERBE',
  'OTRO',
];

const actividadSchema = z.object({
  loteId: z.string().min(1, 'Seleccione un lote'),
  tipoActividad: z.string().min(1, 'Seleccione un tipo de actividad'),
  fechaActividad: z.string().min(1, 'La fecha es requerida'),
  descripcion: z.string().max(500).optional(),
  productosUtilizados: z.string().max(500).optional(),
  dosificacion: z.string().max(200).optional(),
  responsable: z.string().max(150).optional(),
  observaciones: z.string().optional(),
});

type ActividadFormData = z.infer<typeof actividadSchema>;

interface ActividadFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  actividad: ActividadAgronomica | null;
  lotes: Lote[];
  onSubmit: (data: ActividadAgronomicarRequest) => Promise<void>;
}

export function ActividadFormDialog({
  open,
  onOpenChange,
  actividad,
  lotes,
  onSubmit,
}: ActividadFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<ActividadFormData>({
    resolver: zodResolver(actividadSchema),
    defaultValues: {
      loteId: '',
      tipoActividad: '',
      fechaActividad: new Date().toISOString().split('T')[0],
      descripcion: '',
      productosUtilizados: '',
      dosificacion: '',
      responsable: '',
      observaciones: '',
    },
  });

  const watchLoteId = watch('loteId');
  const watchTipo = watch('tipoActividad');

  useEffect(() => {
    if (open) {
      if (actividad) {
        reset({
          loteId: String(actividad.loteId),
          tipoActividad: actividad.tipoActividad,
          fechaActividad: actividad.fechaActividad?.split('T')[0] || '',
          descripcion: actividad.descripcion || '',
          productosUtilizados: actividad.productosUtilizados || '',
          dosificacion: actividad.dosificacion || '',
          responsable: actividad.responsable || '',
          observaciones: actividad.observaciones || '',
        });
      } else {
        reset({
          loteId: '',
          tipoActividad: '',
          fechaActividad: new Date().toISOString().split('T')[0],
          descripcion: '',
          productosUtilizados: '',
          dosificacion: '',
          responsable: '',
          observaciones: '',
        });
      }
    }
  }, [open, actividad, reset]);

  const handleFormSubmit = async (data: ActividadFormData) => {
    const cleanData: ActividadAgronomicarRequest = {
      loteId: Number(data.loteId),
      tipoActividad: data.tipoActividad as TipoActividad,
      fechaActividad: data.fechaActividad,
      descripcion: data.descripcion || undefined,
      productosUtilizados: data.productosUtilizados || undefined,
      dosificacion: data.dosificacion || undefined,
      responsable: data.responsable || undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {actividad ? 'Editar Actividad' : 'Nueva Actividad Agronómica'}
          </DialogTitle>
          <DialogDescription>
            {actividad
              ? 'Modifique los datos de la actividad'
              : 'Complete los datos para registrar una nueva actividad'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Lote */}
            <div className="space-y-2">
              <Label>Lote *</Label>
              <Select
                value={watchLoteId}
                onValueChange={(value) => setValue('loteId', value)}
              >
                <SelectTrigger className={errors.loteId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione un lote" />
                </SelectTrigger>
                <SelectContent>
                  {lotes.map((lote) => (
                    <SelectItem key={lote.id} value={String(lote.id)}>
                      {lote.nombre} - {lote.tipoFruta} ({lote.codigoLote})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.loteId && (
                <p className="text-sm text-destructive">{errors.loteId.message}</p>
              )}
            </div>

            {/* Tipo de Actividad */}
            <div className="space-y-2">
              <Label>Tipo de Actividad *</Label>
              <Select
                value={watchTipo}
                onValueChange={(value) => setValue('tipoActividad', value)}
              >
                <SelectTrigger className={errors.tipoActividad ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione tipo" />
                </SelectTrigger>
                <SelectContent>
                  {TIPOS_ACTIVIDAD.map((tipo) => (
                    <SelectItem key={tipo} value={tipo}>
                      {tipo.replace('_', ' ')}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tipoActividad && (
                <p className="text-sm text-destructive">{errors.tipoActividad.message}</p>
              )}
            </div>

            {/* Fecha */}
            <div className="space-y-2">
              <Label htmlFor="fechaActividad">Fecha de Actividad *</Label>
              <Input
                id="fechaActividad"
                type="date"
                {...register('fechaActividad')}
                className={errors.fechaActividad ? 'border-destructive' : ''}
              />
              {errors.fechaActividad && (
                <p className="text-sm text-destructive">{errors.fechaActividad.message}</p>
              )}
            </div>

            {/* Responsable */}
            <div className="space-y-2">
              <Label htmlFor="responsable">Responsable</Label>
              <Input
                id="responsable"
                placeholder="Juan Pérez"
                {...register('responsable')}
              />
            </div>

            {/* Descripción */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="descripcion">Descripción</Label>
              <Textarea
                id="descripcion"
                placeholder="Describa la actividad realizada..."
                {...register('descripcion')}
              />
            </div>

            {/* Productos Utilizados */}
            <div className="space-y-2">
              <Label htmlFor="productosUtilizados">Productos Utilizados</Label>
              <Input
                id="productosUtilizados"
                placeholder="Fertilizante NPK, Fungicida..."
                {...register('productosUtilizados')}
              />
            </div>

            {/* Dosificación */}
            <div className="space-y-2">
              <Label htmlFor="dosificacion">Dosificación</Label>
              <Input
                id="dosificacion"
                placeholder="100g por planta, 2L/Ha..."
                {...register('dosificacion')}
              />
            </div>

            {/* Observaciones */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="observaciones">Observaciones</Label>
              <Textarea
                id="observaciones"
                placeholder="Notas adicionales..."
                {...register('observaciones')}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Guardando...
                </>
              ) : actividad ? (
                'Actualizar'
              ) : (
                'Registrar'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default ActividadFormDialog;
