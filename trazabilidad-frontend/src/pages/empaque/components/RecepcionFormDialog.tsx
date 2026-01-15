import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { RecepcionPlanta, RecepcionPlantaRequest, Lote } from '@/types';
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

const recepcionSchema = z.object({
  loteId: z.string().min(1, 'El lote es requerido'),
  codigoRecepcion: z.string().min(1, 'El código es requerido').max(50),
  fechaRecepcion: z.string().min(1, 'La fecha es requerida'),
  horaRecepcion: z.string().optional(),
  cantidadRecibida: z.string().min(1, 'La cantidad es requerida'),
  unidadMedida: z.string().min(1, 'La unidad es requerida'),
  temperaturaFruta: z.string().optional(),
  estadoInicial: z.string().optional(),
  responsableRecepcion: z.string().optional(),
  vehiculoTransporte: z.string().optional(),
  conductor: z.string().optional(),
  observaciones: z.string().optional(),
});

type RecepcionFormData = z.infer<typeof recepcionSchema>;

interface RecepcionFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  recepcion: RecepcionPlanta | null;
  lotes: Lote[];
  onSubmit: (data: RecepcionPlantaRequest) => Promise<void>;
}

export function RecepcionFormDialog({
  open,
  onOpenChange,
  recepcion,
  lotes,
  onSubmit,
}: RecepcionFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RecepcionFormData>({
    resolver: zodResolver(recepcionSchema),
    defaultValues: {
      loteId: '',
      codigoRecepcion: '',
      fechaRecepcion: new Date().toISOString().split('T')[0],
      horaRecepcion: '',
      cantidadRecibida: '',
      unidadMedida: 'KG',
      temperaturaFruta: '',
      estadoInicial: '',
      responsableRecepcion: '',
      vehiculoTransporte: '',
      conductor: '',
      observaciones: '',
    },
  });

  const selectedLoteId = watch('loteId');

  useEffect(() => {
    if (open) {
      if (recepcion) {
        reset({
          loteId: String(recepcion.loteId),
          codigoRecepcion: recepcion.codigoRecepcion,
          fechaRecepcion: recepcion.fechaRecepcion,
          horaRecepcion: recepcion.horaRecepcion || '',
          cantidadRecibida: String(recepcion.cantidadRecibida),
          unidadMedida: recepcion.unidadMedida,
          temperaturaFruta: recepcion.temperaturaFruta ? String(recepcion.temperaturaFruta) : '',
          estadoInicial: recepcion.estadoInicial || '',
          responsableRecepcion: recepcion.responsableRecepcion || '',
          vehiculoTransporte: recepcion.vehiculoTransporte || '',
          conductor: recepcion.conductor || '',
          observaciones: recepcion.observaciones || '',
        });
      } else {
        reset({
          loteId: '',
          codigoRecepcion: '',
          fechaRecepcion: new Date().toISOString().split('T')[0],
          horaRecepcion: '',
          cantidadRecibida: '',
          unidadMedida: 'KG',
          temperaturaFruta: '',
          estadoInicial: '',
          responsableRecepcion: '',
          vehiculoTransporte: '',
          conductor: '',
          observaciones: '',
        });
      }
    }
  }, [open, recepcion, reset]);

  const handleFormSubmit = async (data: RecepcionFormData) => {
    const cleanData: RecepcionPlantaRequest = {
      loteId: Number(data.loteId),
      codigoRecepcion: data.codigoRecepcion,
      fechaRecepcion: data.fechaRecepcion,
      horaRecepcion: data.horaRecepcion || undefined,
      cantidadRecibida: Number(data.cantidadRecibida),
      unidadMedida: data.unidadMedida,
      temperaturaFruta: data.temperaturaFruta ? Number(data.temperaturaFruta) : undefined,
      estadoInicial: data.estadoInicial || undefined,
      responsableRecepcion: data.responsableRecepcion || undefined,
      vehiculoTransporte: data.vehiculoTransporte || undefined,
      conductor: data.conductor || undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{recepcion ? 'Editar Recepción' : 'Nueva Recepción'}</DialogTitle>
          <DialogDescription>
            {recepcion
              ? 'Modifique los datos de la recepción'
              : 'Registre una nueva recepción de fruta en planta'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Lote */}
            <div className="space-y-2">
              <Label htmlFor="loteId">Lote *</Label>
              <Select value={selectedLoteId} onValueChange={(value) => setValue('loteId', value)}>
                <SelectTrigger className={errors.loteId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione el lote" />
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

            {/* Código */}
            <div className="space-y-2">
              <Label htmlFor="codigoRecepcion">Código Recepción *</Label>
              <Input
                id="codigoRecepcion"
                placeholder="REC-001"
                {...register('codigoRecepcion')}
                className={errors.codigoRecepcion ? 'border-destructive' : ''}
              />
              {errors.codigoRecepcion && (
                <p className="text-sm text-destructive">{errors.codigoRecepcion.message}</p>
              )}
            </div>

            {/* Fecha */}
            <div className="space-y-2">
              <Label htmlFor="fechaRecepcion">Fecha *</Label>
              <Input
                id="fechaRecepcion"
                type="date"
                {...register('fechaRecepcion')}
                className={errors.fechaRecepcion ? 'border-destructive' : ''}
              />
              {errors.fechaRecepcion && (
                <p className="text-sm text-destructive">{errors.fechaRecepcion.message}</p>
              )}
            </div>

            {/* Hora */}
            <div className="space-y-2">
              <Label htmlFor="horaRecepcion">Hora</Label>
              <Input
                id="horaRecepcion"
                type="time"
                {...register('horaRecepcion')}
              />
            </div>

            {/* Cantidad */}
            <div className="space-y-2">
              <Label htmlFor="cantidadRecibida">Cantidad Recibida *</Label>
              <Input
                id="cantidadRecibida"
                type="number"
                step="0.01"
                placeholder="500"
                {...register('cantidadRecibida')}
                className={errors.cantidadRecibida ? 'border-destructive' : ''}
              />
              {errors.cantidadRecibida && (
                <p className="text-sm text-destructive">{errors.cantidadRecibida.message}</p>
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

            {/* Temperatura */}
            <div className="space-y-2">
              <Label htmlFor="temperaturaFruta">Temperatura Fruta (°C)</Label>
              <Input
                id="temperaturaFruta"
                type="number"
                step="0.1"
                placeholder="22.5"
                {...register('temperaturaFruta')}
              />
            </div>

            {/* Estado Inicial */}
            <div className="space-y-2">
              <Label htmlFor="estadoInicial">Estado Inicial</Label>
              <Select
                value={watch('estadoInicial')}
                onValueChange={(value) => setValue('estadoInicial', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione estado" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="EXCELENTE">Excelente</SelectItem>
                  <SelectItem value="BUENO">Bueno</SelectItem>
                  <SelectItem value="REGULAR">Regular</SelectItem>
                  <SelectItem value="MALO">Malo</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Responsable */}
            <div className="space-y-2">
              <Label htmlFor="responsableRecepcion">Responsable</Label>
              <Input
                id="responsableRecepcion"
                placeholder="Nombre del responsable"
                {...register('responsableRecepcion')}
              />
            </div>

            {/* Vehículo */}
            <div className="space-y-2">
              <Label htmlFor="vehiculoTransporte">Vehículo</Label>
              <Input
                id="vehiculoTransporte"
                placeholder="ABC-123"
                {...register('vehiculoTransporte')}
              />
            </div>

            {/* Conductor */}
            <div className="space-y-2">
              <Label htmlFor="conductor">Conductor</Label>
              <Input
                id="conductor"
                placeholder="Nombre del conductor"
                {...register('conductor')}
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
              ) : recepcion ? (
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

export default RecepcionFormDialog;
