import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { Lote, LoteRequest, Finca } from '@/types';
import { TIPOS_FRUTA } from '@/types';
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

const loteSchema = z.object({
  fincaId: z.string().min(1, 'Seleccione una finca'),
  codigoLote: z.string().min(1, 'El código es requerido').max(50),
  nombre: z.string().min(1, 'El nombre es requerido').max(100),
  tipoFruta: z.string().min(1, 'Seleccione un tipo de fruta'),
  variedad: z.string().max(100).optional(),
  areaHectareas: z.string().optional(),
  fechaSiembra: z.string().optional(),
  fechaPrimeraCosechaEstimada: z.string().optional(),
  densidadSiembra: z.string().optional(),
  ubicacionInterna: z.string().max(200).optional(),
  observaciones: z.string().optional(),
});

type LoteFormData = z.infer<typeof loteSchema>;

interface LoteFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  lote: Lote | null;
  fincas: Finca[];
  onSubmit: (data: LoteRequest) => Promise<void>;
}

export function LoteFormDialog({
  open,
  onOpenChange,
  lote,
  fincas,
  onSubmit,
}: LoteFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<LoteFormData>({
    resolver: zodResolver(loteSchema),
    defaultValues: {
      fincaId: '',
      codigoLote: '',
      nombre: '',
      tipoFruta: '',
      variedad: '',
      areaHectareas: '',
      fechaSiembra: '',
      fechaPrimeraCosechaEstimada: '',
      densidadSiembra: '',
      ubicacionInterna: '',
      observaciones: '',
    },
  });

  const watchFincaId = watch('fincaId');
  const watchTipoFruta = watch('tipoFruta');

  useEffect(() => {
    if (open) {
      if (lote) {
        reset({
          fincaId: String(lote.fincaId),
          codigoLote: lote.codigoLote,
          nombre: lote.nombre,
          tipoFruta: lote.tipoFruta,
          variedad: lote.variedad || '',
          areaHectareas: lote.areaHectareas ? String(lote.areaHectareas) : '',
          fechaSiembra: lote.fechaSiembra || '',
          fechaPrimeraCosechaEstimada: lote.fechaPrimeraCosechaEstimada || '',
          densidadSiembra: lote.densidadSiembra ? String(lote.densidadSiembra) : '',
          ubicacionInterna: lote.ubicacionInterna || '',
          observaciones: lote.observaciones || '',
        });
      } else {
        reset({
          fincaId: '',
          codigoLote: '',
          nombre: '',
          tipoFruta: '',
          variedad: '',
          areaHectareas: '',
          fechaSiembra: '',
          fechaPrimeraCosechaEstimada: '',
          densidadSiembra: '',
          ubicacionInterna: '',
          observaciones: '',
        });
      }
    }
  }, [open, lote, reset]);

  const handleFormSubmit = async (data: LoteFormData) => {
    const cleanData: LoteRequest = {
      fincaId: Number(data.fincaId),
      codigoLote: data.codigoLote,
      nombre: data.nombre,
      tipoFruta: data.tipoFruta,
      variedad: data.variedad || undefined,
      areaHectareas: data.areaHectareas ? Number(data.areaHectareas) : undefined,
      fechaSiembra: data.fechaSiembra || undefined,
      fechaPrimeraCosechaEstimada: data.fechaPrimeraCosechaEstimada || undefined,
      densidadSiembra: data.densidadSiembra ? Number(data.densidadSiembra) : undefined,
      ubicacionInterna: data.ubicacionInterna || undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{lote ? 'Editar Lote' : 'Nuevo Lote'}</DialogTitle>
          <DialogDescription>
            {lote
              ? 'Modifique los datos del lote'
              : 'Complete los datos para registrar un nuevo lote de cultivo'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Finca */}
            <div className="space-y-2">
              <Label>Finca *</Label>
              <Select
                value={watchFincaId}
                onValueChange={(value) => setValue('fincaId', value)}
              >
                <SelectTrigger className={errors.fincaId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione una finca" />
                </SelectTrigger>
                <SelectContent>
                  {fincas.map((finca) => (
                    <SelectItem key={finca.id} value={String(finca.id)}>
                      {finca.nombre} ({finca.codigoFinca})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.fincaId && (
                <p className="text-sm text-destructive">{errors.fincaId.message}</p>
              )}
            </div>

            {/* Código */}
            <div className="space-y-2">
              <Label htmlFor="codigoLote">Código *</Label>
              <Input
                id="codigoLote"
                placeholder="LOT-001"
                {...register('codigoLote')}
                className={errors.codigoLote ? 'border-destructive' : ''}
              />
              {errors.codigoLote && (
                <p className="text-sm text-destructive">{errors.codigoLote.message}</p>
              )}
            </div>

            {/* Nombre */}
            <div className="space-y-2">
              <Label htmlFor="nombre">Nombre *</Label>
              <Input
                id="nombre"
                placeholder="Lote Norte A"
                {...register('nombre')}
                className={errors.nombre ? 'border-destructive' : ''}
              />
              {errors.nombre && (
                <p className="text-sm text-destructive">{errors.nombre.message}</p>
              )}
            </div>

            {/* Tipo de Fruta */}
            <div className="space-y-2">
              <Label>Tipo de Fruta *</Label>
              <Select
                value={watchTipoFruta}
                onValueChange={(value) => setValue('tipoFruta', value)}
              >
                <SelectTrigger className={errors.tipoFruta ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione fruta" />
                </SelectTrigger>
                <SelectContent>
                  {TIPOS_FRUTA.map((fruta) => (
                    <SelectItem key={fruta} value={fruta}>
                      {fruta.replace('_', ' ')}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tipoFruta && (
                <p className="text-sm text-destructive">{errors.tipoFruta.message}</p>
              )}
            </div>

            {/* Variedad */}
            <div className="space-y-2">
              <Label htmlFor="variedad">Variedad</Label>
              <Input
                id="variedad"
                placeholder="Tommy Atkins"
                {...register('variedad')}
              />
            </div>

            {/* Área */}
            <div className="space-y-2">
              <Label htmlFor="areaHectareas">Área (Hectáreas)</Label>
              <Input
                id="areaHectareas"
                type="number"
                step="0.01"
                placeholder="5.5"
                {...register('areaHectareas')}
              />
            </div>

            {/* Fecha Siembra */}
            <div className="space-y-2">
              <Label htmlFor="fechaSiembra">Fecha de Siembra</Label>
              <Input
                id="fechaSiembra"
                type="date"
                {...register('fechaSiembra')}
              />
            </div>

            {/* Fecha Primera Cosecha */}
            <div className="space-y-2">
              <Label htmlFor="fechaPrimeraCosechaEstimada">Primera Cosecha Estimada</Label>
              <Input
                id="fechaPrimeraCosechaEstimada"
                type="date"
                {...register('fechaPrimeraCosechaEstimada')}
              />
            </div>

            {/* Densidad Siembra */}
            <div className="space-y-2">
              <Label htmlFor="densidadSiembra">Densidad (plantas/Ha)</Label>
              <Input
                id="densidadSiembra"
                type="number"
                placeholder="200"
                {...register('densidadSiembra')}
              />
            </div>

            {/* Ubicación Interna */}
            <div className="space-y-2">
              <Label htmlFor="ubicacionInterna">Ubicación Interna</Label>
              <Input
                id="ubicacionInterna"
                placeholder="Sector Norte, cerca del río"
                {...register('ubicacionInterna')}
              />
            </div>

            {/* Observaciones */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="observaciones">Observaciones</Label>
              <Textarea
                id="observaciones"
                placeholder="Notas adicionales sobre el lote..."
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
              ) : lote ? (
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

export default LoteFormDialog;
