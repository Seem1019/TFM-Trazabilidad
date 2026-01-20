import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import type { DocumentoExportacion, DocumentoExportacionRequest, Envio } from '@/types';
import { TIPOS_DOCUMENTO, TIPO_DOCUMENTO_LABELS, MONEDAS } from '@/types';
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

const documentoSchema = z.object({
  envioId: z.string().min(1, 'El envío es requerido'),
  tipoDocumento: z.string().min(1, 'El tipo de documento es requerido'),
  numeroDocumento: z.string().min(1, 'El número de documento es requerido'),
  fechaEmision: z.string().min(1, 'La fecha de emisión es requerida'),
  fechaVencimiento: z.string().optional(),
  entidadEmisora: z.string().optional(),
  funcionarioEmisor: z.string().optional(),
  urlArchivo: z.string().optional(),
  tipoArchivo: z.string().optional(),
  tamanoArchivo: z.string().optional(),
  hashArchivo: z.string().optional(),
  descripcion: z.string().optional(),
  valorDeclarado: z.string().optional(),
  moneda: z.string().optional(),
  obligatorio: z.boolean().optional(),
});

type DocumentoFormData = z.infer<typeof documentoSchema>;

interface DocumentoFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  documento: DocumentoExportacion | null;
  envioId?: number;
  onSubmit: (data: DocumentoExportacionRequest) => Promise<void>;
}

export function DocumentoFormDialog({
  open,
  onOpenChange,
  documento,
  envioId,
  onSubmit,
}: DocumentoFormDialogProps) {
  const [enviosDisponibles, setEnviosDisponibles] = useState<Envio[]>([]);
  const [loadingEnvios, setLoadingEnvios] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<DocumentoFormData>({
    resolver: zodResolver(documentoSchema),
    defaultValues: {
      envioId: '',
      tipoDocumento: '',
      numeroDocumento: '',
      fechaEmision: new Date().toISOString().split('T')[0],
      fechaVencimiento: '',
      entidadEmisora: '',
      funcionarioEmisor: '',
      urlArchivo: '',
      tipoArchivo: '',
      tamanoArchivo: '',
      hashArchivo: '',
      descripcion: '',
      valorDeclarado: '',
      moneda: 'USD',
      obligatorio: false,
    },
  });

  const loadEnvios = useCallback(async () => {
    setLoadingEnvios(true);
    try {
      const envios = await envioService.getAll();
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
      if (documento) {
        reset({
          envioId: String(documento.envioId),
          tipoDocumento: documento.tipoDocumento,
          numeroDocumento: documento.numeroDocumento,
          fechaEmision: documento.fechaEmision,
          fechaVencimiento: documento.fechaVencimiento || '',
          entidadEmisora: documento.entidadEmisora || '',
          funcionarioEmisor: documento.funcionarioEmisor || '',
          urlArchivo: documento.urlArchivo || '',
          tipoArchivo: documento.tipoArchivo || '',
          tamanoArchivo: documento.tamanoArchivo ? String(documento.tamanoArchivo) : '',
          hashArchivo: documento.hashArchivo || '',
          descripcion: documento.descripcion || '',
          valorDeclarado: documento.valorDeclarado ? String(documento.valorDeclarado) : '',
          moneda: documento.moneda || 'USD',
          obligatorio: documento.obligatorio || false,
        });
      } else {
        reset({
          envioId: envioId ? String(envioId) : '',
          tipoDocumento: '',
          numeroDocumento: '',
          fechaEmision: new Date().toISOString().split('T')[0],
          fechaVencimiento: '',
          entidadEmisora: '',
          funcionarioEmisor: '',
          urlArchivo: '',
          tipoArchivo: '',
          tamanoArchivo: '',
          hashArchivo: '',
          descripcion: '',
          valorDeclarado: '',
          moneda: 'USD',
          obligatorio: false,
        });
      }
    }
  }, [open, documento, envioId, reset, loadEnvios]);

  const handleFormSubmit = async (data: DocumentoFormData) => {
    const cleanData: DocumentoExportacionRequest = {
      envioId: Number(data.envioId),
      tipoDocumento: data.tipoDocumento as DocumentoExportacionRequest['tipoDocumento'],
      numeroDocumento: data.numeroDocumento,
      fechaEmision: data.fechaEmision,
      fechaVencimiento: data.fechaVencimiento || undefined,
      entidadEmisora: data.entidadEmisora || undefined,
      funcionarioEmisor: data.funcionarioEmisor || undefined,
      urlArchivo: data.urlArchivo || undefined,
      tipoArchivo: data.tipoArchivo || undefined,
      tamanoArchivo: data.tamanoArchivo ? Number(data.tamanoArchivo) : undefined,
      hashArchivo: data.hashArchivo || undefined,
      descripcion: data.descripcion || undefined,
      valorDeclarado: data.valorDeclarado ? Number(data.valorDeclarado) : undefined,
      moneda: data.moneda as DocumentoExportacionRequest['moneda'],
      obligatorio: data.obligatorio,
    };
    await onSubmit(cleanData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {documento ? 'Editar Documento' : 'Nuevo Documento de Exportación'}
          </DialogTitle>
          <DialogDescription>
            {documento
              ? 'Modifique los datos del documento'
              : 'Registre un nuevo documento de exportación'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          {/* Información básica */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">
              Información del Documento
            </h4>
            <div className="grid grid-cols-2 gap-4">
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
                <Label htmlFor="tipoDocumento">Tipo de Documento *</Label>
                <Select
                  value={watch('tipoDocumento')}
                  onValueChange={(value) => setValue('tipoDocumento', value)}
                >
                  <SelectTrigger className={errors.tipoDocumento ? 'border-destructive' : ''}>
                    <SelectValue placeholder="Seleccione tipo" />
                  </SelectTrigger>
                  <SelectContent>
                    {TIPOS_DOCUMENTO.map((tipo) => (
                      <SelectItem key={tipo} value={tipo}>
                        {TIPO_DOCUMENTO_LABELS[tipo]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="numeroDocumento">Número Documento *</Label>
                <Input
                  id="numeroDocumento"
                  placeholder="DOC-2024-001"
                  {...register('numeroDocumento')}
                  className={errors.numeroDocumento ? 'border-destructive' : ''}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="fechaEmision">Fecha Emisión *</Label>
                <Input
                  id="fechaEmision"
                  type="date"
                  {...register('fechaEmision')}
                  className={errors.fechaEmision ? 'border-destructive' : ''}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="fechaVencimiento">Fecha Vencimiento</Label>
                <Input id="fechaVencimiento" type="date" {...register('fechaVencimiento')} />
              </div>

              <div className="space-y-2 flex items-end">
                <div className="flex items-center gap-4">
                  <Switch
                    id="obligatorio"
                    checked={watch('obligatorio')}
                    onCheckedChange={(checked) => setValue('obligatorio', checked)}
                  />
                  <Label htmlFor="obligatorio">Documento Obligatorio</Label>
                </div>
              </div>
            </div>
          </div>

          {/* Entidad emisora */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Emisión</h4>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="entidadEmisora">Entidad Emisora</Label>
                <Input
                  id="entidadEmisora"
                  placeholder="ICA, DIAN, Cámara de Comercio..."
                  {...register('entidadEmisora')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="funcionarioEmisor">Funcionario Emisor</Label>
                <Input
                  id="funcionarioEmisor"
                  placeholder="Nombre del funcionario"
                  {...register('funcionarioEmisor')}
                />
              </div>
            </div>
          </div>

          {/* Archivo */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Archivo Digital</h4>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2 col-span-2">
                <Label htmlFor="urlArchivo">URL del Archivo</Label>
                <Input
                  id="urlArchivo"
                  placeholder="https://storage.ejemplo.com/documento.pdf"
                  {...register('urlArchivo')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="tipoArchivo">Tipo de Archivo</Label>
                <Select
                  value={watch('tipoArchivo')}
                  onValueChange={(value) => setValue('tipoArchivo', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="application/pdf">PDF</SelectItem>
                    <SelectItem value="image/jpeg">JPEG</SelectItem>
                    <SelectItem value="image/png">PNG</SelectItem>
                    <SelectItem value="application/xml">XML</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="tamanoArchivo">Tamaño (bytes)</Label>
                <Input
                  id="tamanoArchivo"
                  type="number"
                  placeholder="1024000"
                  {...register('tamanoArchivo')}
                />
              </div>

              <div className="space-y-2 col-span-2">
                <Label htmlFor="hashArchivo">Hash SHA-256</Label>
                <Input
                  id="hashArchivo"
                  placeholder="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                  {...register('hashArchivo')}
                />
              </div>
            </div>
          </div>

          {/* Valor */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm text-muted-foreground">Valor Comercial</h4>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="valorDeclarado">Valor Declarado</Label>
                <Input
                  id="valorDeclarado"
                  type="number"
                  step="0.01"
                  placeholder="50000.00"
                  {...register('valorDeclarado')}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="moneda">Moneda</Label>
                <Select
                  value={watch('moneda')}
                  onValueChange={(value) => setValue('moneda', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione" />
                  </SelectTrigger>
                  <SelectContent>
                    {MONEDAS.map((m) => (
                      <SelectItem key={m} value={m}>
                        {m}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          {/* Descripción */}
          <div className="space-y-2">
            <Label htmlFor="descripcion">Descripción</Label>
            <Textarea
              id="descripcion"
              placeholder="Detalles adicionales del documento..."
              {...register('descripcion')}
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
              ) : documento ? (
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

export default DocumentoFormDialog;
