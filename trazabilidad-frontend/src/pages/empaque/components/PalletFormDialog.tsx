import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { Pallet, PalletRequest } from '@/types';
import { TIPOS_FRUTA, CALIDADES } from '@/types';
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

const palletSchema = z.object({
  codigoPallet: z.string().min(1, 'El código es requerido').max(50),
  fechaPaletizado: z.string().min(1, 'La fecha es requerida'),
  tipoPallet: z.string().optional(),
  numeroCajas: z.string().min(1, 'El número de cajas es requerido'),
  pesoNetoTotal: z.string().optional(),
  pesoBrutoTotal: z.string().optional(),
  alturaPallet: z.string().optional(),
  tipoFruta: z.string().optional(),
  calidad: z.string().optional(),
  destino: z.string().optional(),
  temperaturaAlmacenamiento: z.string().optional(),
  responsablePaletizado: z.string().optional(),
  observaciones: z.string().optional(),
});

type PalletFormData = z.infer<typeof palletSchema>;

interface PalletFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  pallet: Pallet | null;
  onSubmit: (data: PalletRequest) => Promise<void>;
}

export function PalletFormDialog({
  open,
  onOpenChange,
  pallet,
  onSubmit,
}: PalletFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<PalletFormData>({
    resolver: zodResolver(palletSchema),
    defaultValues: {
      codigoPallet: '',
      fechaPaletizado: new Date().toISOString().split('T')[0],
      tipoPallet: '',
      numeroCajas: '',
      pesoNetoTotal: '',
      pesoBrutoTotal: '',
      alturaPallet: '',
      tipoFruta: '',
      calidad: '',
      destino: '',
      temperaturaAlmacenamiento: '',
      responsablePaletizado: '',
      observaciones: '',
    },
  });

  useEffect(() => {
    if (open) {
      if (pallet) {
        reset({
          codigoPallet: pallet.codigoPallet,
          fechaPaletizado: pallet.fechaPaletizado,
          tipoPallet: pallet.tipoPallet || '',
          numeroCajas: String(pallet.numeroCajas),
          pesoNetoTotal: pallet.pesoNetoTotal ? String(pallet.pesoNetoTotal) : '',
          pesoBrutoTotal: pallet.pesoBrutoTotal ? String(pallet.pesoBrutoTotal) : '',
          alturaPallet: pallet.alturaPallet ? String(pallet.alturaPallet) : '',
          tipoFruta: pallet.tipoFruta || '',
          calidad: pallet.calidad || '',
          destino: pallet.destino || '',
          temperaturaAlmacenamiento: pallet.temperaturaAlmacenamiento
            ? String(pallet.temperaturaAlmacenamiento)
            : '',
          responsablePaletizado: pallet.responsablePaletizado || '',
          observaciones: pallet.observaciones || '',
        });
      } else {
        reset({
          codigoPallet: '',
          fechaPaletizado: new Date().toISOString().split('T')[0],
          tipoPallet: '',
          numeroCajas: '',
          pesoNetoTotal: '',
          pesoBrutoTotal: '',
          alturaPallet: '',
          tipoFruta: '',
          calidad: '',
          destino: '',
          temperaturaAlmacenamiento: '',
          responsablePaletizado: '',
          observaciones: '',
        });
      }
    }
  }, [open, pallet, reset]);

  const handleFormSubmit = async (data: PalletFormData) => {
    const cleanData: PalletRequest = {
      codigoPallet: data.codigoPallet,
      fechaPaletizado: data.fechaPaletizado,
      tipoPallet: data.tipoPallet || undefined,
      numeroCajas: Number(data.numeroCajas),
      pesoNetoTotal: data.pesoNetoTotal ? Number(data.pesoNetoTotal) : undefined,
      pesoBrutoTotal: data.pesoBrutoTotal ? Number(data.pesoBrutoTotal) : undefined,
      alturaPallet: data.alturaPallet ? Number(data.alturaPallet) : undefined,
      tipoFruta: data.tipoFruta || undefined,
      calidad: data.calidad || undefined,
      destino: data.destino || undefined,
      temperaturaAlmacenamiento: data.temperaturaAlmacenamiento
        ? Number(data.temperaturaAlmacenamiento)
        : undefined,
      responsablePaletizado: data.responsablePaletizado || undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{pallet ? 'Editar Pallet' : 'Nuevo Pallet'}</DialogTitle>
          <DialogDescription>
            {pallet
              ? 'Modifique los datos del pallet'
              : 'Registre un nuevo pallet de fruta'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Código */}
            <div className="space-y-2">
              <Label htmlFor="codigoPallet">Código Pallet *</Label>
              <Input
                id="codigoPallet"
                placeholder="PAL-001"
                {...register('codigoPallet')}
                className={errors.codigoPallet ? 'border-destructive' : ''}
              />
              {errors.codigoPallet && (
                <p className="text-sm text-destructive">{errors.codigoPallet.message}</p>
              )}
            </div>

            {/* Fecha */}
            <div className="space-y-2">
              <Label htmlFor="fechaPaletizado">Fecha Paletizado *</Label>
              <Input
                id="fechaPaletizado"
                type="date"
                {...register('fechaPaletizado')}
                className={errors.fechaPaletizado ? 'border-destructive' : ''}
              />
              {errors.fechaPaletizado && (
                <p className="text-sm text-destructive">{errors.fechaPaletizado.message}</p>
              )}
            </div>

            {/* Tipo Pallet */}
            <div className="space-y-2">
              <Label htmlFor="tipoPallet">Tipo de Pallet</Label>
              <Select
                value={watch('tipoPallet')}
                onValueChange={(value) => setValue('tipoPallet', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione tipo" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="EUROPEO">Europeo (1200x800)</SelectItem>
                  <SelectItem value="AMERICANO">Americano (1200x1000)</SelectItem>
                  <SelectItem value="MEDIO">Medio (600x800)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Número de Cajas */}
            <div className="space-y-2">
              <Label htmlFor="numeroCajas">Número de Cajas *</Label>
              <Input
                id="numeroCajas"
                type="number"
                placeholder="48"
                {...register('numeroCajas')}
                className={errors.numeroCajas ? 'border-destructive' : ''}
              />
              {errors.numeroCajas && (
                <p className="text-sm text-destructive">{errors.numeroCajas.message}</p>
              )}
            </div>

            {/* Peso Neto */}
            <div className="space-y-2">
              <Label htmlFor="pesoNetoTotal">Peso Neto Total (kg)</Label>
              <Input
                id="pesoNetoTotal"
                type="number"
                step="0.01"
                placeholder="480"
                {...register('pesoNetoTotal')}
              />
            </div>

            {/* Peso Bruto */}
            <div className="space-y-2">
              <Label htmlFor="pesoBrutoTotal">Peso Bruto Total (kg)</Label>
              <Input
                id="pesoBrutoTotal"
                type="number"
                step="0.01"
                placeholder="520"
                {...register('pesoBrutoTotal')}
              />
            </div>

            {/* Altura */}
            <div className="space-y-2">
              <Label htmlFor="alturaPallet">Altura (m)</Label>
              <Input
                id="alturaPallet"
                type="number"
                step="0.01"
                placeholder="1.8"
                {...register('alturaPallet')}
              />
            </div>

            {/* Temperatura */}
            <div className="space-y-2">
              <Label htmlFor="temperaturaAlmacenamiento">Temperatura (°C)</Label>
              <Input
                id="temperaturaAlmacenamiento"
                type="number"
                step="0.1"
                placeholder="4"
                {...register('temperaturaAlmacenamiento')}
              />
            </div>

            {/* Tipo Fruta */}
            <div className="space-y-2">
              <Label htmlFor="tipoFruta">Tipo de Fruta</Label>
              <Select
                value={watch('tipoFruta')}
                onValueChange={(value) => setValue('tipoFruta', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione fruta" />
                </SelectTrigger>
                <SelectContent>
                  {TIPOS_FRUTA.map((tipo) => (
                    <SelectItem key={tipo} value={tipo}>
                      {tipo.replace('_', ' ')}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Calidad */}
            <div className="space-y-2">
              <Label htmlFor="calidad">Calidad</Label>
              <Select
                value={watch('calidad')}
                onValueChange={(value) => setValue('calidad', value)}
              >
                <SelectTrigger>
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
            </div>

            {/* Destino */}
            <div className="space-y-2">
              <Label htmlFor="destino">Destino</Label>
              <Input
                id="destino"
                placeholder="Rotterdam, Países Bajos"
                {...register('destino')}
              />
            </div>

            {/* Responsable */}
            <div className="space-y-2">
              <Label htmlFor="responsablePaletizado">Responsable</Label>
              <Input
                id="responsablePaletizado"
                placeholder="Nombre del responsable"
                {...register('responsablePaletizado')}
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
              ) : pallet ? (
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

export default PalletFormDialog;
