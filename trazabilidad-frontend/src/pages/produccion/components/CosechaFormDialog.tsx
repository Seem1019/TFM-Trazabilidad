import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { Cosecha, CosechaRequest, Lote } from '@/types';
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

const CALIDADES = ['PREMIUM', 'PRIMERA', 'SEGUNDA', 'TERCERA', 'DESCARTE'] as const;
const UNIDADES = ['kg', 'lb', 'ton', 'cajas', 'canastillas'] as const;

const cosechaSchema = z.object({
  loteId: z.string().min(1, 'Seleccione un lote'),
  fechaCosecha: z.string().min(1, 'La fecha es requerida'),
  cantidadCosechada: z.string().min(1, 'La cantidad es requerida'),
  unidadMedida: z.string().min(1, 'Seleccione una unidad'),
  calidadInicial: z.string().optional(),
  estadoFruta: z.string().max(100).optional(),
  responsableCosecha: z.string().max(150).optional(),
  numeroTrabajadores: z.string().optional(),
  horaInicio: z.string().optional(),
  horaFin: z.string().optional(),
  temperaturaAmbiente: z.string().optional(),
  observaciones: z.string().optional(),
});

type CosechaFormData = z.infer<typeof cosechaSchema>;

interface CosechaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  cosecha: Cosecha | null;
  lotes: Lote[];
  onSubmit: (data: CosechaRequest) => Promise<void>;
}

export function CosechaFormDialog({
  open,
  onOpenChange,
  cosecha,
  lotes,
  onSubmit,
}: CosechaFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<CosechaFormData>({
    resolver: zodResolver(cosechaSchema),
    defaultValues: {
      loteId: '',
      fechaCosecha: new Date().toISOString().split('T')[0],
      cantidadCosechada: '',
      unidadMedida: 'kg',
      calidadInicial: '',
      estadoFruta: '',
      responsableCosecha: '',
      numeroTrabajadores: '',
      horaInicio: '',
      horaFin: '',
      temperaturaAmbiente: '',
      observaciones: '',
    },
  });

  const watchLoteId = watch('loteId');
  const watchUnidad = watch('unidadMedida');
  const watchCalidad = watch('calidadInicial');

  useEffect(() => {
    if (open) {
      if (cosecha) {
        reset({
          loteId: String(cosecha.loteId),
          fechaCosecha: cosecha.fechaCosecha,
          cantidadCosechada: String(cosecha.cantidadCosechada),
          unidadMedida: cosecha.unidadMedida,
          calidadInicial: cosecha.calidadInicial || '',
          estadoFruta: cosecha.estadoFruta || '',
          responsableCosecha: cosecha.responsableCosecha || '',
          numeroTrabajadores: cosecha.numeroTrabajadores ? String(cosecha.numeroTrabajadores) : '',
          horaInicio: cosecha.horaInicio || '',
          horaFin: cosecha.horaFin || '',
          temperaturaAmbiente: cosecha.temperaturaAmbiente ? String(cosecha.temperaturaAmbiente) : '',
          observaciones: cosecha.observaciones || '',
        });
      } else {
        reset({
          loteId: '',
          fechaCosecha: new Date().toISOString().split('T')[0],
          cantidadCosechada: '',
          unidadMedida: 'kg',
          calidadInicial: '',
          estadoFruta: '',
          responsableCosecha: '',
          numeroTrabajadores: '',
          horaInicio: '',
          horaFin: '',
          temperaturaAmbiente: '',
          observaciones: '',
        });
      }
    }
  }, [open, cosecha, reset]);

  const handleFormSubmit = async (data: CosechaFormData) => {
    const cleanData: CosechaRequest = {
      loteId: Number(data.loteId),
      fechaCosecha: data.fechaCosecha,
      cantidadCosechada: Number(data.cantidadCosechada),
      unidadMedida: data.unidadMedida,
      calidadInicial: data.calidadInicial || undefined,
      estadoFruta: data.estadoFruta || undefined,
      responsableCosecha: data.responsableCosecha || undefined,
      numeroTrabajadores: data.numeroTrabajadores ? Number(data.numeroTrabajadores) : undefined,
      horaInicio: data.horaInicio || undefined,
      horaFin: data.horaFin || undefined,
      temperaturaAmbiente: data.temperaturaAmbiente ? Number(data.temperaturaAmbiente) : undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{cosecha ? 'Editar Cosecha' : 'Registrar Cosecha'}</DialogTitle>
          <DialogDescription>
            {cosecha
              ? 'Modifique los datos de la cosecha'
              : 'Complete los datos para registrar una nueva cosecha'}
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

            {/* Fecha */}
            <div className="space-y-2">
              <Label htmlFor="fechaCosecha">Fecha de Cosecha *</Label>
              <Input
                id="fechaCosecha"
                type="date"
                {...register('fechaCosecha')}
                className={errors.fechaCosecha ? 'border-destructive' : ''}
              />
              {errors.fechaCosecha && (
                <p className="text-sm text-destructive">{errors.fechaCosecha.message}</p>
              )}
            </div>

            {/* Cantidad */}
            <div className="space-y-2">
              <Label htmlFor="cantidadCosechada">Cantidad *</Label>
              <Input
                id="cantidadCosechada"
                type="number"
                step="0.01"
                placeholder="1500"
                {...register('cantidadCosechada')}
                className={errors.cantidadCosechada ? 'border-destructive' : ''}
              />
              {errors.cantidadCosechada && (
                <p className="text-sm text-destructive">{errors.cantidadCosechada.message}</p>
              )}
            </div>

            {/* Unidad */}
            <div className="space-y-2">
              <Label>Unidad de Medida *</Label>
              <Select
                value={watchUnidad}
                onValueChange={(value) => setValue('unidadMedida', value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {UNIDADES.map((unidad) => (
                    <SelectItem key={unidad} value={unidad}>
                      {unidad}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Calidad */}
            <div className="space-y-2">
              <Label>Calidad Inicial</Label>
              <Select
                value={watchCalidad}
                onValueChange={(value) => setValue('calidadInicial', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione calidad" />
                </SelectTrigger>
                <SelectContent>
                  {CALIDADES.map((calidad) => (
                    <SelectItem key={calidad} value={calidad}>
                      {calidad}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Estado Fruta */}
            <div className="space-y-2">
              <Label htmlFor="estadoFruta">Estado de la Fruta</Label>
              <Input
                id="estadoFruta"
                placeholder="Madura, verde, pintón..."
                {...register('estadoFruta')}
              />
            </div>

            {/* Responsable */}
            <div className="space-y-2">
              <Label htmlFor="responsableCosecha">Responsable</Label>
              <Input
                id="responsableCosecha"
                placeholder="Juan Pérez"
                {...register('responsableCosecha')}
              />
            </div>

            {/* Trabajadores */}
            <div className="space-y-2">
              <Label htmlFor="numeroTrabajadores">N° Trabajadores</Label>
              <Input
                id="numeroTrabajadores"
                type="number"
                placeholder="10"
                {...register('numeroTrabajadores')}
              />
            </div>

            {/* Hora Inicio */}
            <div className="space-y-2">
              <Label htmlFor="horaInicio">Hora Inicio</Label>
              <Input
                id="horaInicio"
                type="time"
                {...register('horaInicio')}
              />
            </div>

            {/* Hora Fin */}
            <div className="space-y-2">
              <Label htmlFor="horaFin">Hora Fin</Label>
              <Input
                id="horaFin"
                type="time"
                {...register('horaFin')}
              />
            </div>

            {/* Temperatura */}
            <div className="space-y-2">
              <Label htmlFor="temperaturaAmbiente">Temperatura (°C)</Label>
              <Input
                id="temperaturaAmbiente"
                type="number"
                step="0.1"
                placeholder="28"
                {...register('temperaturaAmbiente')}
              />
            </div>

            {/* Observaciones */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="observaciones">Observaciones</Label>
              <Textarea
                id="observaciones"
                placeholder="Notas sobre la cosecha..."
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
              ) : cosecha ? (
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

export default CosechaFormDialog;
