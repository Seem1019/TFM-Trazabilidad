import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { ControlCalidad, ControlCalidadRequest, Clasificacion, Pallet } from '@/types';
import { TIPOS_CONTROL, RESULTADOS_CONTROL } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
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

const controlCalidadSchema = z.object({
  clasificacionId: z.string().optional(),
  palletId: z.string().optional(),
  codigoControl: z.string().min(1, 'El código es requerido').max(50),
  fechaControl: z.string().min(1, 'La fecha es requerida'),
  tipoControl: z.string().min(1, 'El tipo es requerido'),
  parametroEvaluado: z.string().optional(),
  valorMedido: z.string().optional(),
  valorEsperado: z.string().optional(),
  cumpleEspecificacion: z.boolean().optional(),
  resultado: z.string().min(1, 'El resultado es requerido'),
  responsableControl: z.string().min(1, 'El responsable es requerido'),
  laboratorio: z.string().optional(),
  numeroCertificado: z.string().optional(),
  accionCorrectiva: z.string().optional(),
  observaciones: z.string().optional(),
});

type ControlCalidadFormData = z.infer<typeof controlCalidadSchema>;

interface ControlCalidadFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  control: ControlCalidad | null;
  clasificaciones: Clasificacion[];
  pallets: Pallet[];
  onSubmit: (data: ControlCalidadRequest) => Promise<void>;
}

export function ControlCalidadFormDialog({
  open,
  onOpenChange,
  control,
  clasificaciones,
  pallets,
  onSubmit,
}: ControlCalidadFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<ControlCalidadFormData>({
    resolver: zodResolver(controlCalidadSchema),
    defaultValues: {
      clasificacionId: '',
      palletId: '',
      codigoControl: '',
      fechaControl: new Date().toISOString().split('T')[0],
      tipoControl: '',
      parametroEvaluado: '',
      valorMedido: '',
      valorEsperado: '',
      cumpleEspecificacion: true,
      resultado: '',
      responsableControl: '',
      laboratorio: '',
      numeroCertificado: '',
      accionCorrectiva: '',
      observaciones: '',
    },
  });

  const selectedClasificacionId = watch('clasificacionId');
  const selectedPalletId = watch('palletId');
  const cumpleEspecificacion = watch('cumpleEspecificacion');

  useEffect(() => {
    if (open) {
      if (control) {
        reset({
          clasificacionId: control.clasificacionId ? String(control.clasificacionId) : '',
          palletId: control.palletId ? String(control.palletId) : '',
          codigoControl: control.codigoControl,
          fechaControl: control.fechaControl,
          tipoControl: control.tipoControl,
          parametroEvaluado: control.parametroEvaluado || '',
          valorMedido: control.valorMedido || '',
          valorEsperado: control.valorEsperado || '',
          cumpleEspecificacion: control.cumpleEspecificacion ?? true,
          resultado: control.resultado,
          responsableControl: control.responsableControl,
          laboratorio: control.laboratorio || '',
          numeroCertificado: control.numeroCertificado || '',
          accionCorrectiva: control.accionCorrectiva || '',
          observaciones: control.observaciones || '',
        });
      } else {
        reset({
          clasificacionId: '',
          palletId: '',
          codigoControl: '',
          fechaControl: new Date().toISOString().split('T')[0],
          tipoControl: '',
          parametroEvaluado: '',
          valorMedido: '',
          valorEsperado: '',
          cumpleEspecificacion: true,
          resultado: '',
          responsableControl: '',
          laboratorio: '',
          numeroCertificado: '',
          accionCorrectiva: '',
          observaciones: '',
        });
      }
    }
  }, [open, control, reset]);

  const handleFormSubmit = async (data: ControlCalidadFormData) => {
    const cleanData: ControlCalidadRequest = {
      clasificacionId: data.clasificacionId ? Number(data.clasificacionId) : undefined,
      palletId: data.palletId ? Number(data.palletId) : undefined,
      codigoControl: data.codigoControl,
      fechaControl: data.fechaControl,
      tipoControl: data.tipoControl,
      parametroEvaluado: data.parametroEvaluado || undefined,
      valorMedido: data.valorMedido || undefined,
      valorEsperado: data.valorEsperado || undefined,
      cumpleEspecificacion: data.cumpleEspecificacion,
      resultado: data.resultado as 'APROBADO' | 'RECHAZADO' | 'CONDICIONAL',
      responsableControl: data.responsableControl,
      laboratorio: data.laboratorio || undefined,
      numeroCertificado: data.numeroCertificado || undefined,
      accionCorrectiva: data.accionCorrectiva || undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {control ? 'Editar Control de Calidad' : 'Nuevo Control de Calidad'}
          </DialogTitle>
          <DialogDescription>
            {control
              ? 'Modifique los datos del control'
              : 'Registre un nuevo control de calidad'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Clasificación o Pallet */}
            <div className="space-y-2">
              <Label htmlFor="clasificacionId">Clasificación</Label>
              <Select
                value={selectedClasificacionId}
                onValueChange={(value) => {
                  setValue('clasificacionId', value);
                  if (value) setValue('palletId', '');
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione clasificación" />
                </SelectTrigger>
                <SelectContent>
                  {clasificaciones.map((cla) => (
                    <SelectItem key={cla.id} value={String(cla.id)}>
                      {cla.codigoClasificacion} - {cla.calidad}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="palletId">Pallet</Label>
              <Select
                value={selectedPalletId}
                onValueChange={(value) => {
                  setValue('palletId', value);
                  if (value) setValue('clasificacionId', '');
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione pallet" />
                </SelectTrigger>
                <SelectContent>
                  {pallets.map((pal) => (
                    <SelectItem key={pal.id} value={String(pal.id)}>
                      {pal.codigoPallet}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Código */}
            <div className="space-y-2">
              <Label htmlFor="codigoControl">Código Control *</Label>
              <Input
                id="codigoControl"
                placeholder="CC-001"
                {...register('codigoControl')}
                className={errors.codigoControl ? 'border-destructive' : ''}
              />
              {errors.codigoControl && (
                <p className="text-sm text-destructive">{errors.codigoControl.message}</p>
              )}
            </div>

            {/* Fecha */}
            <div className="space-y-2">
              <Label htmlFor="fechaControl">Fecha *</Label>
              <Input
                id="fechaControl"
                type="date"
                {...register('fechaControl')}
                className={errors.fechaControl ? 'border-destructive' : ''}
              />
              {errors.fechaControl && (
                <p className="text-sm text-destructive">{errors.fechaControl.message}</p>
              )}
            </div>

            {/* Tipo Control */}
            <div className="space-y-2">
              <Label htmlFor="tipoControl">Tipo de Control *</Label>
              <Select
                value={watch('tipoControl')}
                onValueChange={(value) => setValue('tipoControl', value)}
              >
                <SelectTrigger className={errors.tipoControl ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione tipo" />
                </SelectTrigger>
                <SelectContent>
                  {TIPOS_CONTROL.map((tipo) => (
                    <SelectItem key={tipo} value={tipo}>
                      {tipo}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tipoControl && (
                <p className="text-sm text-destructive">{errors.tipoControl.message}</p>
              )}
            </div>

            {/* Resultado */}
            <div className="space-y-2">
              <Label htmlFor="resultado">Resultado *</Label>
              <Select
                value={watch('resultado')}
                onValueChange={(value) => setValue('resultado', value)}
              >
                <SelectTrigger className={errors.resultado ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione resultado" />
                </SelectTrigger>
                <SelectContent>
                  {RESULTADOS_CONTROL.map((res) => (
                    <SelectItem key={res} value={res}>
                      {res}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.resultado && (
                <p className="text-sm text-destructive">{errors.resultado.message}</p>
              )}
            </div>

            {/* Parámetro Evaluado */}
            <div className="space-y-2">
              <Label htmlFor="parametroEvaluado">Parámetro Evaluado</Label>
              <Input
                id="parametroEvaluado"
                placeholder="Brix, pH, Color..."
                {...register('parametroEvaluado')}
              />
            </div>

            {/* Valor Medido */}
            <div className="space-y-2">
              <Label htmlFor="valorMedido">Valor Medido</Label>
              <Input
                id="valorMedido"
                placeholder="14.5"
                {...register('valorMedido')}
              />
            </div>

            {/* Valor Esperado */}
            <div className="space-y-2">
              <Label htmlFor="valorEsperado">Valor Esperado</Label>
              <Input
                id="valorEsperado"
                placeholder="12-16"
                {...register('valorEsperado')}
              />
            </div>

            {/* Cumple Especificación */}
            <div className="flex items-center space-x-2">
              <Switch
                id="cumpleEspecificacion"
                checked={cumpleEspecificacion}
                onCheckedChange={(checked: boolean) => setValue('cumpleEspecificacion', checked)}
              />
              <Label htmlFor="cumpleEspecificacion">Cumple especificación</Label>
            </div>

            {/* Responsable */}
            <div className="space-y-2">
              <Label htmlFor="responsableControl">Responsable *</Label>
              <Input
                id="responsableControl"
                placeholder="Nombre del inspector"
                {...register('responsableControl')}
                className={errors.responsableControl ? 'border-destructive' : ''}
              />
              {errors.responsableControl && (
                <p className="text-sm text-destructive">{errors.responsableControl.message}</p>
              )}
            </div>

            {/* Laboratorio */}
            <div className="space-y-2">
              <Label htmlFor="laboratorio">Laboratorio</Label>
              <Input
                id="laboratorio"
                placeholder="Nombre del laboratorio"
                {...register('laboratorio')}
              />
            </div>

            {/* Número Certificado */}
            <div className="space-y-2">
              <Label htmlFor="numeroCertificado">N° Certificado</Label>
              <Input
                id="numeroCertificado"
                placeholder="CERT-001"
                {...register('numeroCertificado')}
              />
            </div>

            {/* Acción Correctiva */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="accionCorrectiva">Acción Correctiva</Label>
              <Textarea
                id="accionCorrectiva"
                placeholder="Acciones tomadas si no cumple..."
                {...register('accionCorrectiva')}
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
              ) : control ? (
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

export default ControlCalidadFormDialog;
