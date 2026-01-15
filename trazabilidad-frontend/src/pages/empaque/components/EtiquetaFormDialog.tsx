import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { Etiqueta, EtiquetaRequest, Clasificacion } from '@/types';
import { TIPOS_ETIQUETA } from '@/types';
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

const etiquetaSchema = z.object({
  clasificacionId: z.string().min(1, 'La clasificación es requerida'),
  codigoEtiqueta: z.string().min(1, 'El código es requerido').max(50),
  tipoEtiqueta: z.string().min(1, 'El tipo es requerido'),
  cantidadContenida: z.string().optional(),
  unidadMedida: z.string().optional(),
  pesoNeto: z.string().optional(),
  pesoBruto: z.string().optional(),
  numeroCajas: z.string().optional(),
  observaciones: z.string().optional(),
});

type EtiquetaFormData = z.infer<typeof etiquetaSchema>;

interface EtiquetaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  etiqueta: Etiqueta | null;
  clasificaciones: Clasificacion[];
  onSubmit: (data: EtiquetaRequest) => Promise<void>;
}

export function EtiquetaFormDialog({
  open,
  onOpenChange,
  etiqueta,
  clasificaciones,
  onSubmit,
}: EtiquetaFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<EtiquetaFormData>({
    resolver: zodResolver(etiquetaSchema),
    defaultValues: {
      clasificacionId: '',
      codigoEtiqueta: '',
      tipoEtiqueta: 'CAJA',
      cantidadContenida: '',
      unidadMedida: 'KG',
      pesoNeto: '',
      pesoBruto: '',
      numeroCajas: '',
      observaciones: '',
    },
  });

  const selectedClasificacionId = watch('clasificacionId');
  const selectedTipoEtiqueta = watch('tipoEtiqueta');

  useEffect(() => {
    if (open) {
      if (etiqueta) {
        reset({
          clasificacionId: String(etiqueta.clasificacionId),
          codigoEtiqueta: etiqueta.codigoEtiqueta,
          tipoEtiqueta: etiqueta.tipoEtiqueta,
          cantidadContenida: etiqueta.cantidadContenida
            ? String(etiqueta.cantidadContenida)
            : '',
          unidadMedida: etiqueta.unidadMedida || 'KG',
          pesoNeto: etiqueta.pesoNeto ? String(etiqueta.pesoNeto) : '',
          pesoBruto: etiqueta.pesoBruto ? String(etiqueta.pesoBruto) : '',
          numeroCajas: etiqueta.numeroCajas ? String(etiqueta.numeroCajas) : '',
          observaciones: etiqueta.observaciones || '',
        });
      } else {
        reset({
          clasificacionId: '',
          codigoEtiqueta: '',
          tipoEtiqueta: 'CAJA',
          cantidadContenida: '',
          unidadMedida: 'KG',
          pesoNeto: '',
          pesoBruto: '',
          numeroCajas: '',
          observaciones: '',
        });
      }
    }
  }, [open, etiqueta, reset]);

  const handleFormSubmit = async (data: EtiquetaFormData) => {
    const cleanData: EtiquetaRequest = {
      clasificacionId: Number(data.clasificacionId),
      codigoEtiqueta: data.codigoEtiqueta,
      tipoEtiqueta: data.tipoEtiqueta as 'CAJA' | 'PALLET',
      cantidadContenida: data.cantidadContenida ? Number(data.cantidadContenida) : undefined,
      unidadMedida: data.unidadMedida || undefined,
      pesoNeto: data.pesoNeto ? Number(data.pesoNeto) : undefined,
      pesoBruto: data.pesoBruto ? Number(data.pesoBruto) : undefined,
      numeroCajas: data.numeroCajas ? Number(data.numeroCajas) : undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{etiqueta ? 'Editar Etiqueta' : 'Nueva Etiqueta'}</DialogTitle>
          <DialogDescription>
            {etiqueta
              ? 'Modifique los datos de la etiqueta'
              : 'Registre una nueva etiqueta con código QR'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Clasificación */}
            <div className="space-y-2">
              <Label htmlFor="clasificacionId">Clasificación *</Label>
              <Select
                value={selectedClasificacionId}
                onValueChange={(value) => setValue('clasificacionId', value)}
              >
                <SelectTrigger className={errors.clasificacionId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione la clasificación" />
                </SelectTrigger>
                <SelectContent>
                  {clasificaciones.map((cla) => (
                    <SelectItem key={cla.id} value={String(cla.id)}>
                      {cla.codigoClasificacion} - {cla.calidad}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.clasificacionId && (
                <p className="text-sm text-destructive">{errors.clasificacionId.message}</p>
              )}
            </div>

            {/* Código */}
            <div className="space-y-2">
              <Label htmlFor="codigoEtiqueta">Código Etiqueta *</Label>
              <Input
                id="codigoEtiqueta"
                placeholder="ETQ-001"
                {...register('codigoEtiqueta')}
                className={errors.codigoEtiqueta ? 'border-destructive' : ''}
              />
              {errors.codigoEtiqueta && (
                <p className="text-sm text-destructive">{errors.codigoEtiqueta.message}</p>
              )}
            </div>

            {/* Tipo Etiqueta */}
            <div className="space-y-2">
              <Label htmlFor="tipoEtiqueta">Tipo *</Label>
              <Select
                value={selectedTipoEtiqueta}
                onValueChange={(value) => setValue('tipoEtiqueta', value)}
              >
                <SelectTrigger className={errors.tipoEtiqueta ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione tipo" />
                </SelectTrigger>
                <SelectContent>
                  {TIPOS_ETIQUETA.map((tipo) => (
                    <SelectItem key={tipo} value={tipo}>
                      {tipo}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tipoEtiqueta && (
                <p className="text-sm text-destructive">{errors.tipoEtiqueta.message}</p>
              )}
            </div>

            {/* Cantidad Contenida */}
            <div className="space-y-2">
              <Label htmlFor="cantidadContenida">Cantidad Contenida</Label>
              <Input
                id="cantidadContenida"
                type="number"
                step="0.01"
                placeholder="10"
                {...register('cantidadContenida')}
              />
            </div>

            {/* Unidad */}
            <div className="space-y-2">
              <Label htmlFor="unidadMedida">Unidad de Medida</Label>
              <Select
                value={watch('unidadMedida')}
                onValueChange={(value) => setValue('unidadMedida', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione unidad" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="KG">Kilogramos (KG)</SelectItem>
                  <SelectItem value="LB">Libras (LB)</SelectItem>
                  <SelectItem value="UNIDADES">Unidades</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Peso Neto */}
            <div className="space-y-2">
              <Label htmlFor="pesoNeto">Peso Neto (kg)</Label>
              <Input
                id="pesoNeto"
                type="number"
                step="0.01"
                placeholder="9.5"
                {...register('pesoNeto')}
              />
            </div>

            {/* Peso Bruto */}
            <div className="space-y-2">
              <Label htmlFor="pesoBruto">Peso Bruto (kg)</Label>
              <Input
                id="pesoBruto"
                type="number"
                step="0.01"
                placeholder="10.2"
                {...register('pesoBruto')}
              />
            </div>

            {/* Número de Cajas */}
            <div className="space-y-2">
              <Label htmlFor="numeroCajas">Número de Cajas</Label>
              <Input
                id="numeroCajas"
                type="number"
                placeholder="1"
                {...register('numeroCajas')}
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
              ) : etiqueta ? (
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

export default EtiquetaFormDialog;
