import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { Finca, FincaRequest } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

const fincaSchema = z.object({
  codigoFinca: z.string().min(1, 'El código es requerido').max(50),
  nombre: z.string().min(1, 'El nombre es requerido').max(200),
  ubicacion: z.string().max(500).optional(),
  municipio: z.string().max(200).optional(),
  departamento: z.string().max(100).optional(),
  pais: z.string().max(50).optional(),
  areaHectareas: z.string().optional(),
  propietario: z.string().max(100).optional(),
  encargado: z.string().max(100).optional(),
  telefono: z.string().max(20).optional(),
  email: z.string().email('Email inválido').max(100).optional().or(z.literal('')),
  latitud: z.string().optional(),
  longitud: z.string().optional(),
  observaciones: z.string().optional(),
});

type FincaFormData = z.infer<typeof fincaSchema>;

interface FincaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  finca: Finca | null;
  onSubmit: (data: FincaRequest) => Promise<void>;
}

export function FincaFormDialog({
  open,
  onOpenChange,
  finca,
  onSubmit,
}: FincaFormDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FincaFormData>({
    resolver: zodResolver(fincaSchema),
    defaultValues: {
      codigoFinca: '',
      nombre: '',
      ubicacion: '',
      municipio: '',
      departamento: '',
      pais: 'Colombia',
      areaHectareas: '',
      propietario: '',
      encargado: '',
      telefono: '',
      email: '',
      latitud: '',
      longitud: '',
      observaciones: '',
    },
  });

  useEffect(() => {
    if (open) {
      if (finca) {
        reset({
          codigoFinca: finca.codigoFinca,
          nombre: finca.nombre,
          ubicacion: finca.ubicacion || '',
          municipio: finca.municipio || '',
          departamento: finca.departamento || '',
          pais: finca.pais || 'Colombia',
          areaHectareas: finca.areaHectareas ? String(finca.areaHectareas) : '',
          propietario: finca.propietario || '',
          encargado: finca.encargado || '',
          telefono: finca.telefono || '',
          email: finca.email || '',
          latitud: finca.latitud ? String(finca.latitud) : '',
          longitud: finca.longitud ? String(finca.longitud) : '',
          observaciones: finca.observaciones || '',
        });
      } else {
        reset({
          codigoFinca: '',
          nombre: '',
          ubicacion: '',
          municipio: '',
          departamento: '',
          pais: 'Colombia',
          areaHectareas: '',
          propietario: '',
          encargado: '',
          telefono: '',
          email: '',
          latitud: '',
          longitud: '',
          observaciones: '',
        });
      }
    }
  }, [open, finca, reset]);

  const handleFormSubmit = async (data: FincaFormData) => {
    const cleanData: FincaRequest = {
      codigoFinca: data.codigoFinca,
      nombre: data.nombre,
      ubicacion: data.ubicacion || undefined,
      municipio: data.municipio || undefined,
      departamento: data.departamento || undefined,
      pais: data.pais || undefined,
      areaHectareas: data.areaHectareas ? Number(data.areaHectareas) : undefined,
      propietario: data.propietario || undefined,
      encargado: data.encargado || undefined,
      telefono: data.telefono || undefined,
      email: data.email || undefined,
      latitud: data.latitud ? Number(data.latitud) : undefined,
      longitud: data.longitud ? Number(data.longitud) : undefined,
      observaciones: data.observaciones || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{finca ? 'Editar Finca' : 'Nueva Finca'}</DialogTitle>
          <DialogDescription>
            {finca
              ? 'Modifique los datos de la finca'
              : 'Complete los datos para registrar una nueva finca'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Código */}
            <div className="space-y-2">
              <Label htmlFor="codigoFinca">Código *</Label>
              <Input
                id="codigoFinca"
                placeholder="FIN-001"
                {...register('codigoFinca')}
                className={errors.codigoFinca ? 'border-destructive' : ''}
              />
              {errors.codigoFinca && (
                <p className="text-sm text-destructive">{errors.codigoFinca.message}</p>
              )}
            </div>

            {/* Nombre */}
            <div className="space-y-2">
              <Label htmlFor="nombre">Nombre *</Label>
              <Input
                id="nombre"
                placeholder="Finca El Paraíso"
                {...register('nombre')}
                className={errors.nombre ? 'border-destructive' : ''}
              />
              {errors.nombre && (
                <p className="text-sm text-destructive">{errors.nombre.message}</p>
              )}
            </div>

            {/* Ubicación */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="ubicacion">Dirección</Label>
              <Input
                id="ubicacion"
                placeholder="Vereda La Esperanza, Km 5"
                {...register('ubicacion')}
              />
            </div>

            {/* Municipio */}
            <div className="space-y-2">
              <Label htmlFor="municipio">Municipio</Label>
              <Input
                id="municipio"
                placeholder="Ciénaga"
                {...register('municipio')}
              />
            </div>

            {/* Departamento */}
            <div className="space-y-2">
              <Label htmlFor="departamento">Departamento</Label>
              <Input
                id="departamento"
                placeholder="Magdalena"
                {...register('departamento')}
              />
            </div>

            {/* País */}
            <div className="space-y-2">
              <Label htmlFor="pais">País</Label>
              <Input
                id="pais"
                placeholder="Colombia"
                {...register('pais')}
              />
            </div>

            {/* Área */}
            <div className="space-y-2">
              <Label htmlFor="areaHectareas">Área (Hectáreas)</Label>
              <Input
                id="areaHectareas"
                type="number"
                step="0.01"
                placeholder="50.5"
                {...register('areaHectareas')}
              />
            </div>

            {/* Propietario */}
            <div className="space-y-2">
              <Label htmlFor="propietario">Propietario</Label>
              <Input
                id="propietario"
                placeholder="Juan Pérez"
                {...register('propietario')}
              />
            </div>

            {/* Encargado */}
            <div className="space-y-2">
              <Label htmlFor="encargado">Encargado</Label>
              <Input
                id="encargado"
                placeholder="Carlos López"
                {...register('encargado')}
              />
            </div>

            {/* Teléfono */}
            <div className="space-y-2">
              <Label htmlFor="telefono">Teléfono</Label>
              <Input
                id="telefono"
                placeholder="+57 300 123 4567"
                {...register('telefono')}
              />
            </div>

            {/* Email */}
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="finca@email.com"
                {...register('email')}
                className={errors.email ? 'border-destructive' : ''}
              />
              {errors.email && (
                <p className="text-sm text-destructive">{errors.email.message}</p>
              )}
            </div>

            {/* Latitud */}
            <div className="space-y-2">
              <Label htmlFor="latitud">Latitud</Label>
              <Input
                id="latitud"
                type="number"
                step="0.000001"
                placeholder="10.4567"
                {...register('latitud')}
              />
            </div>

            {/* Longitud */}
            <div className="space-y-2">
              <Label htmlFor="longitud">Longitud</Label>
              <Input
                id="longitud"
                type="number"
                step="0.000001"
                placeholder="-74.1234"
                {...register('longitud')}
              />
            </div>

            {/* Observaciones */}
            <div className="col-span-2 space-y-2">
              <Label htmlFor="observaciones">Observaciones</Label>
              <Textarea
                id="observaciones"
                placeholder="Notas adicionales sobre la finca..."
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
              ) : finca ? (
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

export default FincaFormDialog;
