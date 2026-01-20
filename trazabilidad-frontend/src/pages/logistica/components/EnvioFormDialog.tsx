import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { Envio, EnvioRequest, Pallet } from '@/types';
import { TIPOS_TRANSPORTE, INCOTERMS, TIPOS_CONTENEDOR } from '@/types';
import { palletService } from '@/services';
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

const envioSchema = z.object({
  codigoEnvio: z.string().min(1, 'El código es requerido').max(50),
  fechaCreacion: z.string().min(1, 'La fecha de creación es requerida'),
  fechaSalidaEstimada: z.string().optional(),
  exportador: z.string().optional(),
  paisDestino: z.string().min(1, 'El país de destino es requerido'),
  puertoDestino: z.string().optional(),
  ciudadDestino: z.string().optional(),
  tipoTransporte: z.string().min(1, 'El tipo de transporte es requerido'),
  codigoContenedor: z.string().optional(),
  tipoContenedor: z.string().optional(),
  temperaturaContenedor: z.string().optional(),
  transportista: z.string().optional(),
  numeroBooking: z.string().optional(),
  numeroBL: z.string().optional(),
  observaciones: z.string().optional(),
  clienteImportador: z.string().optional(),
  incoterm: z.string().optional(),
});

type EnvioFormData = z.infer<typeof envioSchema>;

interface EnvioFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  envio: Envio | null;
  onSubmit: (data: EnvioRequest) => Promise<void>;
}

export function EnvioFormDialog({
  open,
  onOpenChange,
  envio,
  onSubmit,
}: EnvioFormDialogProps) {
  const [palletsDisponibles, setPalletsDisponibles] = useState<Pallet[]>([]);
  const [selectedPallets, setSelectedPallets] = useState<number[]>([]);
  const [loadingPallets, setLoadingPallets] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<EnvioFormData>({
    resolver: zodResolver(envioSchema),
    defaultValues: {
      codigoEnvio: '',
      fechaCreacion: new Date().toISOString().split('T')[0],
      fechaSalidaEstimada: '',
      exportador: '',
      paisDestino: '',
      puertoDestino: '',
      ciudadDestino: '',
      tipoTransporte: '',
      codigoContenedor: '',
      tipoContenedor: '',
      temperaturaContenedor: '',
      transportista: '',
      numeroBooking: '',
      numeroBL: '',
      observaciones: '',
      clienteImportador: '',
      incoterm: '',
    },
  });

  const loadPalletsDisponibles = useCallback(async () => {
    setLoadingPallets(true);
    try {
      const pallets = await palletService.getListosEnvio();
      setPalletsDisponibles(pallets);
    } catch {
      // Silent fail
    } finally {
      setLoadingPallets(false);
    }
  }, []);

  useEffect(() => {
    if (open) {
      loadPalletsDisponibles();
      if (envio) {
        reset({
          codigoEnvio: envio.codigoEnvio,
          fechaCreacion: envio.fechaCreacion,
          fechaSalidaEstimada: envio.fechaSalidaEstimada || '',
          exportador: envio.exportador || '',
          paisDestino: envio.paisDestino,
          puertoDestino: envio.puertoDestino || '',
          ciudadDestino: envio.ciudadDestino || '',
          tipoTransporte: envio.tipoTransporte,
          codigoContenedor: envio.codigoContenedor || '',
          tipoContenedor: envio.tipoContenedor || '',
          temperaturaContenedor: envio.temperaturaContenedor
            ? String(envio.temperaturaContenedor)
            : '',
          transportista: envio.transportista || '',
          numeroBooking: envio.numeroBooking || '',
          numeroBL: envio.numeroBL || '',
          observaciones: envio.observaciones || '',
          clienteImportador: envio.clienteImportador || '',
          incoterm: envio.incoterm || '',
        });
        setSelectedPallets([]);
      } else {
        reset({
          codigoEnvio: '',
          fechaCreacion: new Date().toISOString().split('T')[0],
          fechaSalidaEstimada: '',
          exportador: '',
          paisDestino: '',
          puertoDestino: '',
          ciudadDestino: '',
          tipoTransporte: '',
          codigoContenedor: '',
          tipoContenedor: '',
          temperaturaContenedor: '',
          transportista: '',
          numeroBooking: '',
          numeroBL: '',
          observaciones: '',
          clienteImportador: '',
          incoterm: '',
        });
        setSelectedPallets([]);
      }
    }
  }, [open, envio, reset, loadPalletsDisponibles]);

  const handleFormSubmit = async (data: EnvioFormData) => {
    const cleanData: EnvioRequest = {
      codigoEnvio: data.codigoEnvio,
      fechaCreacion: data.fechaCreacion,
      fechaSalidaEstimada: data.fechaSalidaEstimada || undefined,
      exportador: data.exportador || undefined,
      paisDestino: data.paisDestino,
      puertoDestino: data.puertoDestino || undefined,
      ciudadDestino: data.ciudadDestino || undefined,
      tipoTransporte: data.tipoTransporte as 'MARITIMO' | 'AEREO' | 'TERRESTRE',
      codigoContenedor: data.codigoContenedor || undefined,
      tipoContenedor: data.tipoContenedor || undefined,
      temperaturaContenedor: data.temperaturaContenedor
        ? Number(data.temperaturaContenedor)
        : undefined,
      transportista: data.transportista || undefined,
      numeroBooking: data.numeroBooking || undefined,
      numeroBL: data.numeroBL || undefined,
      observaciones: data.observaciones || undefined,
      clienteImportador: data.clienteImportador || undefined,
      incoterm: data.incoterm as EnvioRequest['incoterm'],
      palletsIds: selectedPallets.length > 0 ? selectedPallets : undefined,
    };
    await onSubmit(cleanData);
  };

  const togglePallet = (id: number) => {
    setSelectedPallets((prev) =>
      prev.includes(id) ? prev.filter((p) => p !== id) : [...prev, id]
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{envio ? 'Editar Envío' : 'Nuevo Envío'}</DialogTitle>
          <DialogDescription>
            {envio
              ? 'Modifique los datos del envío'
              : 'Registre un nuevo envío de exportación'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          {/* Información básica */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Información Básica</h4>
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="codigoEnvio">Código Envío *</Label>
                <Input
                  id="codigoEnvio"
                  placeholder="ENV-2024-001"
                  {...register('codigoEnvio')}
                  className={errors.codigoEnvio ? 'border-destructive' : ''}
                />
                {errors.codigoEnvio && (
                  <p className="text-sm text-destructive">{errors.codigoEnvio.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="fechaCreacion">Fecha Creación *</Label>
                <Input
                  id="fechaCreacion"
                  type="date"
                  {...register('fechaCreacion')}
                  className={errors.fechaCreacion ? 'border-destructive' : ''}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="fechaSalidaEstimada">Fecha Salida Estimada</Label>
                <Input
                  id="fechaSalidaEstimada"
                  type="date"
                  {...register('fechaSalidaEstimada')}
                />
              </div>
            </div>
          </div>

          {/* Destino */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Destino</h4>
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="paisDestino">País Destino *</Label>
                <Input
                  id="paisDestino"
                  placeholder="Países Bajos"
                  {...register('paisDestino')}
                  className={errors.paisDestino ? 'border-destructive' : ''}
                />
                {errors.paisDestino && (
                  <p className="text-sm text-destructive">{errors.paisDestino.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="puertoDestino">Puerto Destino</Label>
                <Input id="puertoDestino" placeholder="Rotterdam" {...register('puertoDestino')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="ciudadDestino">Ciudad Destino</Label>
                <Input id="ciudadDestino" placeholder="Ámsterdam" {...register('ciudadDestino')} />
              </div>
            </div>
          </div>

          {/* Transporte */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Transporte</h4>
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="tipoTransporte">Tipo Transporte *</Label>
                <Select
                  value={watch('tipoTransporte')}
                  onValueChange={(value) => setValue('tipoTransporte', value)}
                >
                  <SelectTrigger className={errors.tipoTransporte ? 'border-destructive' : ''}>
                    <SelectValue placeholder="Seleccione" />
                  </SelectTrigger>
                  <SelectContent>
                    {TIPOS_TRANSPORTE.map((tipo) => (
                      <SelectItem key={tipo} value={tipo}>
                        {tipo}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="codigoContenedor">Código Contenedor</Label>
                <Input
                  id="codigoContenedor"
                  placeholder="MSCU1234567"
                  {...register('codigoContenedor')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="tipoContenedor">Tipo Contenedor</Label>
                <Select
                  value={watch('tipoContenedor')}
                  onValueChange={(value) => setValue('tipoContenedor', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione" />
                  </SelectTrigger>
                  <SelectContent>
                    {TIPOS_CONTENEDOR.map((tipo) => (
                      <SelectItem key={tipo} value={tipo}>
                        {tipo}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="temperaturaContenedor">Temperatura (°C)</Label>
                <Input
                  id="temperaturaContenedor"
                  type="number"
                  step="0.1"
                  placeholder="4"
                  {...register('temperaturaContenedor')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="transportista">Transportista</Label>
                <Input id="transportista" placeholder="MSC, Maersk..." {...register('transportista')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="numeroBooking">Número Booking</Label>
                <Input id="numeroBooking" placeholder="BK123456" {...register('numeroBooking')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="numeroBL">Número B/L</Label>
                <Input id="numeroBL" placeholder="BL123456" {...register('numeroBL')} />
              </div>
            </div>
          </div>

          {/* Comercial */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Información Comercial</h4>
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="exportador">Exportador</Label>
                <Input id="exportador" placeholder="Nombre exportador" {...register('exportador')} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="clienteImportador">Cliente Importador</Label>
                <Input
                  id="clienteImportador"
                  placeholder="Nombre importador"
                  {...register('clienteImportador')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="incoterm">Incoterm</Label>
                <Select
                  value={watch('incoterm')}
                  onValueChange={(value) => setValue('incoterm', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione" />
                  </SelectTrigger>
                  <SelectContent>
                    {INCOTERMS.map((term) => (
                      <SelectItem key={term} value={term}>
                        {term}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          {/* Pallets disponibles (solo para nuevo envío) */}
          {!envio && palletsDisponibles.length > 0 && (
            <div className="space-y-2">
              <h4 className="font-medium text-sm text-muted-foreground">
                Pallets Listos para Envío ({palletsDisponibles.length} disponibles)
              </h4>
              {loadingPallets ? (
                <p className="text-sm text-muted-foreground">Cargando pallets...</p>
              ) : (
                <div className="border rounded-lg p-3 max-h-40 overflow-y-auto">
                  <div className="grid grid-cols-2 gap-2">
                    {palletsDisponibles.map((pallet) => (
                      <label
                        key={pallet.id}
                        className={`flex items-center gap-2 p-2 rounded cursor-pointer border ${
                          selectedPallets.includes(pallet.id)
                            ? 'bg-primary/10 border-primary'
                            : 'hover:bg-muted'
                        }`}
                      >
                        <input
                          type="checkbox"
                          checked={selectedPallets.includes(pallet.id)}
                          onChange={() => togglePallet(pallet.id)}
                          className="rounded"
                        />
                        <div className="text-sm">
                          <span className="font-mono">{pallet.codigoPallet}</span>
                          <span className="text-muted-foreground ml-2">
                            {pallet.numeroCajas} cajas
                          </span>
                        </div>
                      </label>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

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
              ) : envio ? (
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

export default EnvioFormDialog;
