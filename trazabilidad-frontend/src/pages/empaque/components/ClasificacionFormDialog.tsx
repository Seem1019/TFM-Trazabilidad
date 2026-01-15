import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { Clasificacion, ClasificacionRequest, RecepcionPlanta } from '@/types';
import { CALIDADES, CALIBRES } from '@/types';
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

const clasificacionSchema = z.object({
  recepcionId: z.string().min(1, 'La recepción es requerida'),
  codigoClasificacion: z.string().min(1, 'El código es requerido').max(50),
  fechaClasificacion: z.string().min(1, 'La fecha es requerida'),
  calidad: z.string().min(1, 'La calidad es requerida'),
  cantidadClasificada: z.string().min(1, 'La cantidad es requerida'),
  unidadMedida: z.string().min(1, 'La unidad es requerida'),
  calibre: z.string().optional(),
  porcentajeMerma: z.string().optional(),
  cantidadMerma: z.string().optional(),
  motivoMerma: z.string().optional(),
  responsableClasificacion: z.string().optional(),
  observaciones: z.string().optional(),
});

type ClasificacionFormData = z.infer<typeof clasificacionSchema>;

interface ClasificacionFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  clasificacion: Clasificacion | null;
  recepciones: RecepcionPlanta[];
  onSubmit: (data: ClasificacionRequest) => Promise<void>;
}

export function ClasificacionFormDialog({
  open,
  onOpenChange,
  clasificacion,
  recepciones,
  onSubmit,
}: ClasificacionFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<ClasificacionFormData>({
    resolver: zodResolver(clasificacionSchema),
    defaultValues: {
      recepcionId: '',
      codigoClasificacion: '',
      fechaClasificacion: new Date().toISOString().split('T')[0],
      calidad: '',
      cantidadClasificada: '',
      unidadMedida: 'KG',
      calibre: '',
      porcentajeMerma: '',
      cantidadMerma: '',
      motivoMerma: '',
      responsableClasificacion: '',
      observaciones: '',
    },
  });

  const selectedRecepcionId = watch('recepcionId');
  const selectedCalidad = watch('calidad');
  const selectedCalibre = watch('calibre');

  useEffect(() => {
    if (open) {
      if (clasificacion) {
        reset({
          recepcionId: String(clasificacion.recepcionId),
          codigoClasificacion: clasificacion.codigoClasificacion,
          fechaClasificacion: clasificacion.fechaClasificacion,
          calidad: clasificacion.calidad,
          cantidadClasificada: String(clasificacion.cantidadClasificada),
          unidadMedida: clasificacion.unidadMedida,
          calibre: clasificacion.calibre || '',
          porcentajeMerma: clasificacion.porcentajeMerma
            ? String(clasificacion.porcentajeMerma)
            : '',
          cantidadMerma: clasificacion.cantidadMerma ? String(clasificacion.cantidadMerma) : '',
          motivoMerma: clasificacion.motivoMerma || '',
          responsableClasificacion: clasificacion.responsableClasificacion || '',
          observaciones: clasificacion.observaciones || '',
        });
      } else {
        reset({
          recepcionId: '',
          codigoClasificacion: '',
          fechaClasificacion: new Date().toISOString().split('T')[0],
          calidad: '',
          cantidadClasificada: '',
          unidadMedida: 'KG',
          calibre: '',
          porcentajeMerma: '',
          cantidadMerma: '',
          motivoMerma: '',
          responsableClasificacion: '',
          observaciones: '',
        });
      }
    }
  }, [open, clasificacion, reset]);

  const handleFormSubmit = async (data: ClasificacionFormData) => {
    const cleanData: ClasificacionRequest = {
      recepcionId: Number(data.recepcionId),
      codigoClasificacion: data.codigoClasificacion,
      fechaClasificacion: data.fechaClasificacion,
      calidad: data.calidad,
      cantidadClasificada: Number(data.cantidadClasificada),
      unidadMedida: data.unidadMedida,
      calibre: data.calibre || undefined,
      porcentajeMerma: data.porcentajeMerma ? Number(data.porcentajeMerma) : undefined,
      cantidadMerma: data.cantidadMerma ? Number(data.cantidadMerma) : undefined,
      motivoMerma: data.motivoMerma || undefined,
      responsableClasificacion: data.responsableClasificacion || undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {clasificacion ? 'Editar Clasificación' : 'Nueva Clasificación'}
          </DialogTitle>
          <DialogDescription>
            {clasificacion
              ? 'Modifique los datos de la clasificación'
              : 'Registre una nueva clasificación de fruta'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Recepción */}
            <div className="space-y-2">
              <Label htmlFor="recepcionId">Recepción *</Label>
              <Select
                value={selectedRecepcionId}
                onValueChange={(value) => setValue('recepcionId', value)}
              >
                <SelectTrigger className={errors.recepcionId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione la recepción" />
                </SelectTrigger>
                <SelectContent>
                  {recepciones.map((rec) => (
                    <SelectItem key={rec.id} value={String(rec.id)}>
                      {rec.codigoRecepcion} - {rec.loteNombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.recepcionId && (
                <p className="text-sm text-destructive">{errors.recepcionId.message}</p>
              )}
            </div>

            {/* Código */}
            <div className="space-y-2">
              <Label htmlFor="codigoClasificacion">Código Clasificación *</Label>
              <Input
                id="codigoClasificacion"
                placeholder="CLA-001"
                {...register('codigoClasificacion')}
                className={errors.codigoClasificacion ? 'border-destructive' : ''}
              />
              {errors.codigoClasificacion && (
                <p className="text-sm text-destructive">{errors.codigoClasificacion.message}</p>
              )}
            </div>

            {/* Fecha */}
            <div className="space-y-2">
              <Label htmlFor="fechaClasificacion">Fecha *</Label>
              <Input
                id="fechaClasificacion"
                type="date"
                {...register('fechaClasificacion')}
                className={errors.fechaClasificacion ? 'border-destructive' : ''}
              />
              {errors.fechaClasificacion && (
                <p className="text-sm text-destructive">{errors.fechaClasificacion.message}</p>
              )}
            </div>

            {/* Calidad */}
            <div className="space-y-2">
              <Label htmlFor="calidad">Calidad *</Label>
              <Select value={selectedCalidad} onValueChange={(value) => setValue('calidad', value)}>
                <SelectTrigger className={errors.calidad ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione calidad" />
                </SelectTrigger>
                <SelectContent>
                  {CALIDADES.map((cal) => (
                    <SelectItem key={cal} value={cal}>
                      {cal}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.calidad && (
                <p className="text-sm text-destructive">{errors.calidad.message}</p>
              )}
            </div>

            {/* Cantidad */}
            <div className="space-y-2">
              <Label htmlFor="cantidadClasificada">Cantidad Clasificada *</Label>
              <Input
                id="cantidadClasificada"
                type="number"
                step="0.01"
                placeholder="400"
                {...register('cantidadClasificada')}
                className={errors.cantidadClasificada ? 'border-destructive' : ''}
              />
              {errors.cantidadClasificada && (
                <p className="text-sm text-destructive">{errors.cantidadClasificada.message}</p>
              )}
            </div>

            {/* Unidad */}
            <div className="space-y-2">
              <Label htmlFor="unidadMedida">Unidad de Medida *</Label>
              <Select
                value={watch('unidadMedida')}
                onValueChange={(value) => setValue('unidadMedida', value)}
              >
                <SelectTrigger className={errors.unidadMedida ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione unidad" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="KG">Kilogramos (KG)</SelectItem>
                  <SelectItem value="TON">Toneladas (TON)</SelectItem>
                  <SelectItem value="LB">Libras (LB)</SelectItem>
                  <SelectItem value="CAJAS">Cajas</SelectItem>
                </SelectContent>
              </Select>
              {errors.unidadMedida && (
                <p className="text-sm text-destructive">{errors.unidadMedida.message}</p>
              )}
            </div>

            {/* Calibre */}
            <div className="space-y-2">
              <Label htmlFor="calibre">Calibre</Label>
              <Select
                value={selectedCalibre}
                onValueChange={(value) => setValue('calibre', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione calibre" />
                </SelectTrigger>
                <SelectContent>
                  {CALIBRES.map((cal) => (
                    <SelectItem key={cal} value={cal}>
                      {cal.replace('_', ' ')}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Porcentaje Merma */}
            <div className="space-y-2">
              <Label htmlFor="porcentajeMerma">% Merma</Label>
              <Input
                id="porcentajeMerma"
                type="number"
                step="0.1"
                min="0"
                max="100"
                placeholder="5.0"
                {...register('porcentajeMerma')}
              />
            </div>

            {/* Cantidad Merma */}
            <div className="space-y-2">
              <Label htmlFor="cantidadMerma">Cantidad Merma</Label>
              <Input
                id="cantidadMerma"
                type="number"
                step="0.01"
                placeholder="25"
                {...register('cantidadMerma')}
              />
            </div>

            {/* Motivo Merma */}
            <div className="space-y-2">
              <Label htmlFor="motivoMerma">Motivo Merma</Label>
              <Input
                id="motivoMerma"
                placeholder="Daño mecánico, madurez excesiva..."
                {...register('motivoMerma')}
              />
            </div>

            {/* Responsable */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="responsableClasificacion">Responsable</Label>
              <Input
                id="responsableClasificacion"
                placeholder="Nombre del responsable"
                {...register('responsableClasificacion')}
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
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Guardando...
                </>
              ) : clasificacion ? (
                'Actualizar'
              ) : (
                'Crear'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default ClasificacionFormDialog;
