import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { EventoLogistico, EventoLogisticoRequest, Envio } from '@/types';
import { TIPOS_EVENTO, TIPO_EVENTO_LABELS } from '@/types';
import { envioService } from '@/services';
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

const eventoSchema = z.object({
  envioId: z.string().min(1, 'El envío es requerido'),
  codigoEvento: z.string().optional(),
  tipoEvento: z.string().min(1, 'El tipo de evento es requerido'),
  fechaEvento: z.string().min(1, 'La fecha es requerida'),
  horaEvento: z.string().min(1, 'La hora es requerida'),
  ubicacion: z.string().min(1, 'La ubicación es requerida'),
  ciudad: z.string().optional(),
  pais: z.string().optional(),
  latitud: z.string().optional(),
  longitud: z.string().optional(),
  responsable: z.string().min(1, 'El responsable es requerido'),
  organizacion: z.string().optional(),
  temperaturaRegistrada: z.string().optional(),
  humedadRegistrada: z.string().optional(),
  vehiculo: z.string().optional(),
  conductor: z.string().optional(),
  numeroPrecinto: z.string().optional(),
  observaciones: z.string().optional(),
  urlEvidencia: z.string().optional(),
  incidencia: z.boolean().optional(),
  detalleIncidencia: z.string().optional(),
});

type EventoFormData = z.infer<typeof eventoSchema>;

interface EventoFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  evento: EventoLogistico | null;
  envioId?: number;
  onSubmit: (data: EventoLogisticoRequest) => Promise<void>;
}

export function EventoFormDialog({
  open,
  onOpenChange,
  evento,
  envioId,
  onSubmit,
}: EventoFormDialogProps) {
  const [enviosDisponibles, setEnviosDisponibles] = useState<Envio[]>([]);
  const [loadingEnvios, setLoadingEnvios] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<EventoFormData>({
    resolver: zodResolver(eventoSchema),
    defaultValues: {
      envioId: '',
      codigoEvento: '',
      tipoEvento: '',
      fechaEvento: new Date().toISOString().split('T')[0],
      horaEvento: new Date().toTimeString().slice(0, 5),
      ubicacion: '',
      ciudad: '',
      pais: '',
      latitud: '',
      longitud: '',
      responsable: '',
      organizacion: '',
      temperaturaRegistrada: '',
      humedadRegistrada: '',
      vehiculo: '',
      conductor: '',
      numeroPrecinto: '',
      observaciones: '',
      urlEvidencia: '',
      incidencia: false,
      detalleIncidencia: '',
    },
  });

  const incidenciaActiva = watch('incidencia');

  const loadEnvios = useCallback(async () => {
    setLoadingEnvios(true);
    try {
      const envios = await envioService.getAll();
      // Solo mostrar envíos que no estén cerrados
      setEnviosDisponibles(envios.filter((e) => !e.hashCierre));
    } catch {
      // Silent fail
    } finally {
      setLoadingEnvios(false);
    }
  }, []);

  useEffect(() => {
    if (open) {
      loadEnvios();
      if (evento) {
        reset({
          envioId: String(evento.envioId),
          codigoEvento: evento.codigoEvento || '',
          tipoEvento: evento.tipoEvento,
          fechaEvento: evento.fechaEvento,
          horaEvento: evento.horaEvento,
          ubicacion: evento.ubicacion,
          ciudad: evento.ciudad || '',
          pais: evento.pais || '',
          latitud: evento.latitud ? String(evento.latitud) : '',
          longitud: evento.longitud ? String(evento.longitud) : '',
          responsable: evento.responsable,
          organizacion: evento.organizacion || '',
          temperaturaRegistrada: evento.temperaturaRegistrada
            ? String(evento.temperaturaRegistrada)
            : '',
          humedadRegistrada: evento.humedadRegistrada ? String(evento.humedadRegistrada) : '',
          vehiculo: evento.vehiculo || '',
          conductor: evento.conductor || '',
          numeroPrecinto: evento.numeroPrecinto || '',
          observaciones: evento.observaciones || '',
          urlEvidencia: evento.urlEvidencia || '',
          incidencia: evento.incidencia || false,
          detalleIncidencia: evento.detalleIncidencia || '',
        });
      } else {
        reset({
          envioId: envioId ? String(envioId) : '',
          codigoEvento: '',
          tipoEvento: '',
          fechaEvento: new Date().toISOString().split('T')[0],
          horaEvento: new Date().toTimeString().slice(0, 5),
          ubicacion: '',
          ciudad: '',
          pais: '',
          latitud: '',
          longitud: '',
          responsable: '',
          organizacion: '',
          temperaturaRegistrada: '',
          humedadRegistrada: '',
          vehiculo: '',
          conductor: '',
          numeroPrecinto: '',
          observaciones: '',
          urlEvidencia: '',
          incidencia: false,
          detalleIncidencia: '',
        });
      }
    }
  }, [open, evento, envioId, reset, loadEnvios]);

  const handleFormSubmit = async (data: EventoFormData) => {
    const cleanData: EventoLogisticoRequest = {
      envioId: Number(data.envioId),
      codigoEvento: data.codigoEvento || undefined,
      tipoEvento: data.tipoEvento as EventoLogisticoRequest['tipoEvento'],
      fechaEvento: data.fechaEvento,
      horaEvento: data.horaEvento,
      ubicacion: data.ubicacion,
      ciudad: data.ciudad || undefined,
      pais: data.pais || undefined,
      latitud: data.latitud ? Number(data.latitud) : undefined,
      longitud: data.longitud ? Number(data.longitud) : undefined,
      responsable: data.responsable,
      organizacion: data.organizacion || undefined,
      temperaturaRegistrada: data.temperaturaRegistrada
        ? Number(data.temperaturaRegistrada)
        : undefined,
      humedadRegistrada: data.humedadRegistrada ? Number(data.humedadRegistrada) : undefined,
      vehiculo: data.vehiculo || undefined,
      conductor: data.conductor || undefined,
      numeroPrecinto: data.numeroPrecinto || undefined,
      observaciones: data.observaciones || undefined,
      urlEvidencia: data.urlEvidencia || undefined,
      incidencia: data.incidencia,
      detalleIncidencia: data.detalleIncidencia || undefined,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{evento ? 'Editar Evento' : 'Nuevo Evento Logístico'}</DialogTitle>
          <DialogDescription>
            {evento ? 'Modifique los datos del evento' : 'Registre un nuevo evento logístico'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          {/* Información básica */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Información del Evento</h4>
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="envioId">Envío *</Label>
                {loadingEnvios ? (
                  <p className="text-sm text-muted-foreground">Cargando envíos...</p>
                ) : (
                  <Select
                    value={watch('envioId')}
                    onValueChange={(value) => setValue('envioId', value)}
                    disabled={!!envioId}
                  >
                    <SelectTrigger className={errors.envioId ? 'border-destructive' : ''}>
                      <SelectValue placeholder="Seleccione envío" />
                    </SelectTrigger>
                    <SelectContent>
                      {enviosDisponibles.map((env) => (
                        <SelectItem key={env.id} value={String(env.id)}>
                          {env.codigoEnvio} - {env.paisDestino}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="tipoEvento">Tipo de Evento *</Label>
                <Select
                  value={watch('tipoEvento')}
                  onValueChange={(value) => setValue('tipoEvento', value)}
                >
                  <SelectTrigger className={errors.tipoEvento ? 'border-destructive' : ''}>
                    <SelectValue placeholder="Seleccione tipo" />
                  </SelectTrigger>
                  <SelectContent>
                    {TIPOS_EVENTO.map((tipo) => (
                      <SelectItem key={tipo} value={tipo}>
                        {TIPO_EVENTO_LABELS[tipo]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="codigoEvento">Código Evento</Label>
                <Input id="codigoEvento" placeholder="EVT-001" {...register('codigoEvento')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="fechaEvento">Fecha *</Label>
                <Input
                  id="fechaEvento"
                  type="date"
                  {...register('fechaEvento')}
                  className={errors.fechaEvento ? 'border-destructive' : ''}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="horaEvento">Hora *</Label>
                <Input
                  id="horaEvento"
                  type="time"
                  {...register('horaEvento')}
                  className={errors.horaEvento ? 'border-destructive' : ''}
                />
              </div>
            </div>
          </div>

          {/* Ubicación */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Ubicación</h4>
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2 col-span-2">
                <Label htmlFor="ubicacion">Ubicación *</Label>
                <Input
                  id="ubicacion"
                  placeholder="Terminal Portuaria, Bodega 5..."
                  {...register('ubicacion')}
                  className={errors.ubicacion ? 'border-destructive' : ''}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="ciudad">Ciudad</Label>
                <Input id="ciudad" placeholder="Cartagena" {...register('ciudad')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="pais">País</Label>
                <Input id="pais" placeholder="Colombia" {...register('pais')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="latitud">Latitud</Label>
                <Input
                  id="latitud"
                  type="number"
                  step="0.000001"
                  placeholder="10.3932"
                  {...register('latitud')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="longitud">Longitud</Label>
                <Input
                  id="longitud"
                  type="number"
                  step="0.000001"
                  placeholder="-75.4794"
                  {...register('longitud')}
                />
              </div>
            </div>
          </div>

          {/* Responsable */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Responsable</h4>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="responsable">Responsable *</Label>
                <Input
                  id="responsable"
                  placeholder="Nombre del responsable"
                  {...register('responsable')}
                  className={errors.responsable ? 'border-destructive' : ''}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="organizacion">Organización</Label>
                <Input
                  id="organizacion"
                  placeholder="Empresa, terminal..."
                  {...register('organizacion')}
                />
              </div>
            </div>
          </div>

          {/* Condiciones */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Condiciones Registradas</h4>
            <div className="grid grid-cols-4 gap-4">
              <div className="space-y-2">
                <Label htmlFor="temperaturaRegistrada">Temperatura (°C)</Label>
                <Input
                  id="temperaturaRegistrada"
                  type="number"
                  step="0.1"
                  placeholder="4"
                  {...register('temperaturaRegistrada')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="humedadRegistrada">Humedad (%)</Label>
                <Input
                  id="humedadRegistrada"
                  type="number"
                  step="0.1"
                  placeholder="85"
                  {...register('humedadRegistrada')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="vehiculo">Vehículo/Placa</Label>
                <Input id="vehiculo" placeholder="ABC-123" {...register('vehiculo')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="conductor">Conductor</Label>
                <Input id="conductor" placeholder="Nombre" {...register('conductor')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="numeroPrecinto">Número Precinto</Label>
                <Input id="numeroPrecinto" placeholder="SEAL123456" {...register('numeroPrecinto')} />
              </div>

              <div className="space-y-2 col-span-3">
                <Label htmlFor="urlEvidencia">URL Evidencia</Label>
                <Input
                  id="urlEvidencia"
                  placeholder="https://..."
                  {...register('urlEvidencia')}
                />
              </div>
            </div>
          </div>

          {/* Incidencia */}
          <div className="space-y-2">
            <div className="flex items-center gap-4">
              <Switch
                id="incidencia"
                checked={incidenciaActiva}
                onCheckedChange={(checked) => setValue('incidencia', checked)}
              />
              <Label htmlFor="incidencia" className="text-destructive">
                Reportar Incidencia
              </Label>
            </div>
            {incidenciaActiva && (
              <div className="space-y-2">
                <Label htmlFor="detalleIncidencia">Detalle de la Incidencia</Label>
                <Textarea
                  id="detalleIncidencia"
                  placeholder="Describa la incidencia ocurrida..."
                  {...register('detalleIncidencia')}
                />
              </div>
            )}
          </div>

          {/* Observaciones */}
          <div className="space-y-2">
            <Label htmlFor="observaciones">Observaciones</Label>
            <Textarea
              id="observaciones"
              placeholder="Notas adicionales..."
              {...register('observaciones')}
            />
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
              ) : evento ? (
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

export default EventoFormDialog;
