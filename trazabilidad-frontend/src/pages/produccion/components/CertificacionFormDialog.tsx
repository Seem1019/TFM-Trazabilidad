import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { Certificacion, CertificacionRequest, Finca } from '@/types';
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

const TIPOS_CERTIFICACION = [
  'GLOBAL_GAP',
  'RAINFOREST_ALLIANCE',
  'FAIRTRADE',
  'ORGANICO',
  'USDA_ORGANIC',
  'ICA',
  'OTRO',
];

const ESTADOS_CERTIFICACION = ['VIGENTE', 'VENCIDA', 'SUSPENDIDA', 'EN_PROCESO'];

const certificacionSchema = z.object({
  fincaId: z.string().min(1, 'Seleccione una finca'),
  tipoCertificacion: z.string().min(1, 'El tipo es requerido'),
  entidadEmisora: z.string().min(1, 'La entidad emisora es requerida').max(200),
  numeroCertificado: z.string().max(100).optional(),
  fechaEmision: z.string().min(1, 'La fecha de emisión es requerida'),
  fechaVencimiento: z.string().min(1, 'La fecha de vencimiento es requerida'),
  estado: z.string().min(1, 'El estado es requerido'),
  alcance: z.string().max(500).optional(),
  observaciones: z.string().optional(),
});

type CertificacionFormData = z.infer<typeof certificacionSchema>;

interface CertificacionFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  certificacion: Certificacion | null;
  fincas: Finca[];
  onSubmit: (data: CertificacionRequest) => Promise<void>;
}

export function CertificacionFormDialog({
  open,
  onOpenChange,
  certificacion,
  fincas,
  onSubmit,
}: CertificacionFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<CertificacionFormData>({
    resolver: zodResolver(certificacionSchema),
    defaultValues: {
      fincaId: '',
      tipoCertificacion: '',
      entidadEmisora: '',
      numeroCertificado: '',
      fechaEmision: '',
      fechaVencimiento: '',
      estado: 'VIGENTE',
      alcance: '',
      observaciones: '',
    },
  });

  const watchedFincaId = watch('fincaId');
  const watchedTipo = watch('tipoCertificacion');
  const watchedEstado = watch('estado');

  useEffect(() => {
    if (open) {
      if (certificacion) {
        reset({
          fincaId: String(certificacion.fincaId),
          tipoCertificacion: certificacion.tipoCertificacion,
          entidadEmisora: certificacion.entidadEmisora,
          numeroCertificado: certificacion.numeroCertificado || '',
          fechaEmision: certificacion.fechaEmision?.split('T')[0] || '',
          fechaVencimiento: certificacion.fechaVencimiento?.split('T')[0] || '',
          estado: certificacion.estado || 'VIGENTE',
          alcance: certificacion.alcance || '',
          observaciones: certificacion.observaciones || '',
        });
      } else {
        reset({
          fincaId: '',
          tipoCertificacion: '',
          entidadEmisora: '',
          numeroCertificado: '',
          fechaEmision: new Date().toISOString().split('T')[0],
          fechaVencimiento: '',
          estado: 'VIGENTE',
          alcance: '',
          observaciones: '',
        });
      }
    }
  }, [open, certificacion, reset]);

  const handleFormSubmit = async (data: CertificacionFormData) => {
    const cleanData: CertificacionRequest = {
      fincaId: Number(data.fincaId),
      tipoCertificacion: data.tipoCertificacion,
      entidadEmisora: data.entidadEmisora,
      numeroCertificado: data.numeroCertificado || undefined,
      fechaEmision: data.fechaEmision,
      fechaVencimiento: data.fechaVencimiento,
      estado: data.estado,
      alcance: data.alcance || undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {certificacion ? 'Editar Certificación' : 'Nueva Certificación'}
          </DialogTitle>
          <DialogDescription>
            {certificacion
              ? 'Modifique los datos de la certificación'
              : 'Complete los datos para registrar una nueva certificación'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Finca */}
            <div className="space-y-2">
              <Label htmlFor="fincaId">Finca *</Label>
              <Select
                value={watchedFincaId}
                onValueChange={(value) => setValue('fincaId', value)}
              >
                <SelectTrigger className={errors.fincaId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione finca" />
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

            {/* Tipo Certificación */}
            <div className="space-y-2">
              <Label htmlFor="tipoCertificacion">Tipo de Certificación *</Label>
              <Select
                value={watchedTipo}
                onValueChange={(value) => setValue('tipoCertificacion', value)}
              >
                <SelectTrigger
                  className={errors.tipoCertificacion ? 'border-destructive' : ''}
                >
                  <SelectValue placeholder="Seleccione tipo" />
                </SelectTrigger>
                <SelectContent>
                  {TIPOS_CERTIFICACION.map((tipo) => (
                    <SelectItem key={tipo} value={tipo}>
                      {tipo.replace(/_/g, ' ')}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tipoCertificacion && (
                <p className="text-sm text-destructive">
                  {errors.tipoCertificacion.message}
                </p>
              )}
            </div>

            {/* Entidad Emisora */}
            <div className="space-y-2">
              <Label htmlFor="entidadEmisora">Entidad Emisora *</Label>
              <Input
                id="entidadEmisora"
                placeholder="Control Union, SGS, etc."
                {...register('entidadEmisora')}
                className={errors.entidadEmisora ? 'border-destructive' : ''}
              />
              {errors.entidadEmisora && (
                <p className="text-sm text-destructive">
                  {errors.entidadEmisora.message}
                </p>
              )}
            </div>

            {/* Número de Certificado */}
            <div className="space-y-2">
              <Label htmlFor="numeroCertificado">N° Certificado</Label>
              <Input
                id="numeroCertificado"
                placeholder="CERT-2024-001"
                {...register('numeroCertificado')}
              />
            </div>

            {/* Fecha de Emisión */}
            <div className="space-y-2">
              <Label htmlFor="fechaEmision">Fecha de Emisión *</Label>
              <Input
                id="fechaEmision"
                type="date"
                {...register('fechaEmision')}
                className={errors.fechaEmision ? 'border-destructive' : ''}
              />
              {errors.fechaEmision && (
                <p className="text-sm text-destructive">{errors.fechaEmision.message}</p>
              )}
            </div>

            {/* Fecha de Vencimiento */}
            <div className="space-y-2">
              <Label htmlFor="fechaVencimiento">Fecha de Vencimiento *</Label>
              <Input
                id="fechaVencimiento"
                type="date"
                {...register('fechaVencimiento')}
                className={errors.fechaVencimiento ? 'border-destructive' : ''}
              />
              {errors.fechaVencimiento && (
                <p className="text-sm text-destructive">
                  {errors.fechaVencimiento.message}
                </p>
              )}
            </div>

            {/* Estado */}
            <div className="space-y-2">
              <Label htmlFor="estado">Estado *</Label>
              <Select
                value={watchedEstado}
                onValueChange={(value) => setValue('estado', value)}
              >
                <SelectTrigger className={errors.estado ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Seleccione estado" />
                </SelectTrigger>
                <SelectContent>
                  {ESTADOS_CERTIFICACION.map((estado) => (
                    <SelectItem key={estado} value={estado}>
                      {estado.replace(/_/g, ' ')}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.estado && (
                <p className="text-sm text-destructive">{errors.estado.message}</p>
              )}
            </div>

            {/* Alcance */}
            <div className="space-y-2">
              <Label htmlFor="alcance">Alcance</Label>
              <Input
                id="alcance"
                placeholder="Producción de banano orgánico"
                {...register('alcance')}
              />
            </div>

            {/* Observaciones */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="observaciones">Observaciones</Label>
              <Textarea
                id="observaciones"
                placeholder="Notas adicionales sobre la certificación..."
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
              ) : certificacion ? (
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

export default CertificacionFormDialog;
