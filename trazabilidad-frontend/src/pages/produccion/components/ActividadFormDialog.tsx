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

const UNIDADES_MEDIDA = ['kg', 'g', 'litros', 'ml', 'kg/ha', 'L/ha', 'cc/L', 'g/L'] as const;

const actividadSchema = z.object({
  loteId: z.string().min(1, 'Seleccione un lote'),
  tipoActividad: z.string().min(1, 'Seleccione un tipo de actividad'),
  fechaActividad: z.string().min(1, 'La fecha es requerida'),
  productoAplicado: z.string().max(200, 'Máximo 200 caracteres').optional(),
  dosisoCantidad: z.string().max(100, 'Máximo 100 caracteres').optional(),
  unidadMedida: z.string().max(50).optional(),
  metodoAplicacion: z.string().max(100, 'Máximo 100 caracteres').optional(),
  responsable: z.string().max(150, 'Máximo 150 caracteres').optional(),
  numeroRegistroProducto: z.string().max(100, 'Máximo 100 caracteres').optional(),
  intervaloSeguridadDias: z.string().optional(),
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
      productoAplicado: '',
      dosisoCantidad: '',
      unidadMedida: '',
      metodoAplicacion: '',
      responsable: '',
      numeroRegistroProducto: '',
      intervaloSeguridadDias: '',
      observaciones: '',
    },
  });

  const watchLoteId = watch('loteId');
  const watchTipo = watch('tipoActividad');
  const watchUnidad = watch('unidadMedida');

  useEffect(() => {
    if (open) {
      if (actividad) {
        reset({
          loteId: String(actividad.loteId),
          tipoActividad: actividad.tipoActividad,
          fechaActividad: actividad.fechaActividad?.split('T')[0] || '',
          productoAplicado: actividad.productoAplicado || '',
          dosisoCantidad: actividad.dosisoCantidad || '',
          unidadMedida: actividad.unidadMedida || '',
          metodoAplicacion: actividad.metodoAplicacion || '',
          responsable: actividad.responsable || '',
          numeroRegistroProducto: actividad.numeroRegistroProducto || '',
          intervaloSeguridadDias: actividad.intervaloSeguridadDias != null ? String(actividad.intervaloSeguridadDias) : '',
          observaciones: actividad.observaciones || '',
        });
      } else {
        reset({
          loteId: '',
          tipoActividad: '',
          fechaActividad: new Date().toISOString().split('T')[0],
          productoAplicado: '',
          dosisoCantidad: '',
          unidadMedida: '',
          metodoAplicacion: '',
          responsable: '',
          numeroRegistroProducto: '',
          intervaloSeguridadDias: '',
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
      productoAplicado: data.productoAplicado || undefined,
      dosisoCantidad: data.dosisoCantidad || undefined,
      unidadMedida: data.unidadMedida || undefined,
      metodoAplicacion: data.metodoAplicacion || undefined,
      responsable: data.responsable || undefined,
      numeroRegistroProducto: data.numeroRegistroProducto || undefined,
      intervaloSeguridadDias: data.intervaloSeguridadDias ? Number(data.intervaloSeguridadDias) : undefined,
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

            {/* Producto Aplicado */}
            <div className="space-y-2">
              <Label htmlFor="productoAplicado">Producto Aplicado</Label>
              <Input
                id="productoAplicado"
                placeholder="Fertilizante NPK, Fungicida..."
                {...register('productoAplicado')}
              />
              {errors.productoAplicado && (
                <p className="text-sm text-destructive">{errors.productoAplicado.message}</p>
              )}
            </div>

            {/* Número de Registro del Producto */}
            <div className="space-y-2">
              <Label htmlFor="numeroRegistroProducto">N° Registro Producto (ICA)</Label>
              <Input
                id="numeroRegistroProducto"
                placeholder="Registro ICA del producto"
                {...register('numeroRegistroProducto')}
              />
            </div>

            {/* Dosis o Cantidad */}
            <div className="space-y-2">
              <Label htmlFor="dosisoCantidad">Dosis / Cantidad</Label>
              <Input
                id="dosisoCantidad"
                placeholder="100g por planta, 2L/Ha..."
                {...register('dosisoCantidad')}
              />
            </div>

            {/* Unidad de Medida */}
            <div className="space-y-2">
              <Label>Unidad de Medida</Label>
              <Select
                value={watchUnidad}
                onValueChange={(value) => setValue('unidadMedida', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione unidad" />
                </SelectTrigger>
                <SelectContent>
                  {UNIDADES_MEDIDA.map((unidad) => (
                    <SelectItem key={unidad} value={unidad}>
                      {unidad}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Método de Aplicación */}
            <div className="space-y-2">
              <Label htmlFor="metodoAplicacion">Método de Aplicación</Label>
              <Input
                id="metodoAplicacion"
                placeholder="Aspersión, drench, inyección..."
                {...register('metodoAplicacion')}
              />
            </div>

            {/* Intervalo de Seguridad */}
            <div className="space-y-2">
              <Label htmlFor="intervaloSeguridadDias">Intervalo de Seguridad (días)</Label>
              <Input
                id="intervaloSeguridadDias"
                type="number"
                min="0"
                placeholder="7"
                {...register('intervaloSeguridadDias')}
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
