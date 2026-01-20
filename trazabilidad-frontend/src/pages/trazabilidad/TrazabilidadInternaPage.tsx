import { useState, useCallback } from 'react';
import { toast } from 'sonner';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Search,
  Loader2,
  MapPin,
  Calendar,
  Factory,
  Truck,
  Award,
  FileText,
  CheckCircle,
  XCircle,
  Hash,
  Building2,
  Package,
  QrCode,
  ExternalLink,
  Copy,
} from 'lucide-react';
import { etiquetaService } from '@/services';
import { trazabilidadService, type TrazabilidadCompleta } from '@/services/trazabilidadService';
import { useFetch } from '@/hooks/useFetch';
import type { Etiqueta } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

// Componente Accordion simple para mostrar secciones expandibles
function SimpleAccordion({
  title,
  icon,
  children,
  defaultOpen = false,
}: {
  title: string;
  icon: React.ReactNode;
  children: React.ReactNode;
  defaultOpen?: boolean;
}) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <Card>
      <CardHeader
        className="cursor-pointer"
        onClick={() => setOpen(!open)}
      >
        <CardTitle className="flex items-center justify-between text-lg">
          <span className="flex items-center gap-2">
            {icon}
            {title}
          </span>
          <span className="text-muted-foreground">{open ? '−' : '+'}</span>
        </CardTitle>
      </CardHeader>
      {open && <CardContent>{children}</CardContent>}
    </Card>
  );
}

export function TrazabilidadInternaPage() {
  const [selectedEtiquetaId, setSelectedEtiquetaId] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [trazabilidad, setTrazabilidad] = useState<TrazabilidadCompleta | null>(null);
  const [loading, setLoading] = useState(false);

  const { data: etiquetas, isLoading: loadingEtiquetas } = useFetch<Etiqueta[]>(
    useCallback(() => etiquetaService.getAll(), []),
    []
  );

  const handleBuscar = async () => {
    if (!selectedEtiquetaId) {
      toast.error('Seleccione una etiqueta');
      return;
    }

    setLoading(true);
    try {
      const result = await trazabilidadService.getCompleta(selectedEtiquetaId);
      setTrazabilidad(result);
    } catch {
      toast.error('Error al obtener la trazabilidad');
      setTrazabilidad(null);
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    toast.success('Copiado al portapapeles');
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-';
    try {
      return format(new Date(dateStr), 'dd MMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return '-';
    try {
      return format(new Date(dateStr), 'dd MMM yyyy HH:mm', { locale: es });
    } catch {
      return dateStr;
    }
  };

  // Filtrar etiquetas por búsqueda
  const filteredEtiquetas = etiquetas?.filter(
    (e) =>
      e.codigoEtiqueta.toLowerCase().includes(searchTerm.toLowerCase()) ||
      e.codigoQr?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Consulta de Trazabilidad</h2>
        <p className="text-muted-foreground">
          Consulte el historial completo de trazabilidad de un producto
        </p>
      </div>

      {/* Selector de Etiqueta */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Buscar Producto</CardTitle>
          <CardDescription>
            Seleccione una etiqueta para ver su trazabilidad completa
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4">
            <div className="flex-1">
              <Input
                placeholder="Buscar por código de etiqueta o QR..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="mb-2"
              />
              <Select
                value={selectedEtiquetaId ? String(selectedEtiquetaId) : ''}
                onValueChange={(value) => setSelectedEtiquetaId(Number(value))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione una etiqueta" />
                </SelectTrigger>
                <SelectContent>
                  {loadingEtiquetas ? (
                    <SelectItem value="loading" disabled>
                      Cargando etiquetas...
                    </SelectItem>
                  ) : (
                    filteredEtiquetas?.map((etiq) => (
                      <SelectItem key={etiq.id} value={String(etiq.id)}>
                        <span className="font-mono">{etiq.codigoEtiqueta}</span>
                        <span className="text-muted-foreground ml-2">
                          - {etiq.calidad} - {etiq.fincaOrigen}
                        </span>
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            </div>
            <Button onClick={handleBuscar} disabled={!selectedEtiquetaId || loading}>
              {loading ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Search className="mr-2 h-4 w-4" />
              )}
              Consultar
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Resultados */}
      {trazabilidad && (
        <div className="space-y-4">
          {/* Resumen */}
          <Card className="border-primary/50">
            <CardHeader className="bg-primary/5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <QrCode className="h-10 w-10 text-primary" />
                  <div>
                    <CardTitle>{trazabilidad.codigoEtiqueta}</CardTitle>
                    <CardDescription>
                      Tipo: {trazabilidad.tipoEtiqueta} | Estado: {trazabilidad.estadoEtiqueta}
                    </CardDescription>
                  </div>
                </div>
                <div className="text-right">
                  {trazabilidad.urlQr && (
                    <Button variant="outline" size="sm" asChild>
                      <a href={trazabilidad.urlQr} target="_blank" rel="noopener noreferrer">
                        <ExternalLink className="h-4 w-4 mr-2" />
                        Ver QR
                      </a>
                    </Button>
                  )}
                </div>
              </div>
            </CardHeader>
            <CardContent className="pt-4">
              <div className="grid grid-cols-3 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground">Código QR</p>
                  <div className="flex items-center gap-2">
                    <code className="text-xs bg-muted px-2 py-1 rounded">
                      {trazabilidad.codigoQr?.slice(0, 20)}...
                    </code>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-6 w-6"
                      onClick={() => copyToClipboard(trazabilidad.codigoQr)}
                    >
                      <Copy className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
                <div>
                  <p className="text-muted-foreground">Empresa</p>
                  <p className="font-medium">{trazabilidad.auditoria.empresaNombre}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Creación</p>
                  <p className="font-medium">{formatDateTime(trazabilidad.auditoria.fechaCreacionEtiqueta)}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Origen */}
          <SimpleAccordion title="Origen" icon={<MapPin className="h-5 w-5" />} defaultOpen>
            <div className="grid grid-cols-2 gap-6">
              <div className="space-y-4">
                <h4 className="font-semibold flex items-center gap-2">
                  <Building2 className="h-4 w-4" />
                  Finca
                </h4>
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div>
                    <p className="text-muted-foreground">Nombre</p>
                    <p className="font-medium">{trazabilidad.origen.fincaNombre}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Código</p>
                    <p className="font-mono">{trazabilidad.origen.fincaCodigo}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Ubicación</p>
                    <p>
                      {trazabilidad.origen.municipio}, {trazabilidad.origen.departamento}
                    </p>
                    <p className="text-muted-foreground">{trazabilidad.origen.pais}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Área Total</p>
                    <p>{trazabilidad.origen.areaTotal} ha</p>
                  </div>
                  {trazabilidad.origen.contactoResponsable && (
                    <div className="col-span-2">
                      <p className="text-muted-foreground">Contacto</p>
                      <p>{trazabilidad.origen.contactoResponsable}</p>
                      <p className="text-sm text-muted-foreground">
                        {trazabilidad.origen.telefonoContacto} | {trazabilidad.origen.emailContacto}
                      </p>
                    </div>
                  )}
                </div>
              </div>
              <div className="space-y-4">
                <h4 className="font-semibold flex items-center gap-2">
                  <Package className="h-4 w-4" />
                  Lote
                </h4>
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div>
                    <p className="text-muted-foreground">Nombre</p>
                    <p className="font-medium">{trazabilidad.origen.nombreLote}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Código</p>
                    <p className="font-mono">{trazabilidad.origen.codigoLote}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Tipo de Fruta</p>
                    <p>{trazabilidad.origen.tipoFruta}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Variedad</p>
                    <p>{trazabilidad.origen.variedad || '-'}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Área</p>
                    <p>{trazabilidad.origen.areaHectareas} ha</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Fecha Siembra</p>
                    <p>{formatDate(trazabilidad.origen.fechaSiembra)}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Estado</p>
                    <Badge variant="outline">{trazabilidad.origen.estadoLote}</Badge>
                  </div>
                </div>
              </div>
            </div>
          </SimpleAccordion>

          {/* Producción */}
          <SimpleAccordion title="Producción" icon={<Calendar className="h-5 w-5" />}>
            <div className="space-y-4">
              {trazabilidad.produccion.fechaCosecha && (
                <div className="grid grid-cols-4 gap-4 text-sm">
                  <div>
                    <p className="text-muted-foreground">Fecha Cosecha</p>
                    <p className="font-medium">{formatDate(trazabilidad.produccion.fechaCosecha)}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Cantidad</p>
                    <p>
                      {trazabilidad.produccion.cantidadCosechada}{' '}
                      {trazabilidad.produccion.unidadMedida}
                    </p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Estado Fruta</p>
                    <Badge variant="outline">{trazabilidad.produccion.estadoFruta}</Badge>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Responsable</p>
                    <p>{trazabilidad.produccion.responsableCosecha || '-'}</p>
                  </div>
                </div>
              )}

              {trazabilidad.produccion.actividades.length > 0 && (
                <>
                  <Separator />
                  <div>
                    <h4 className="font-semibold mb-3">
                      Actividades Agronómicas ({trazabilidad.produccion.totalActividades})
                    </h4>
                    <div className="space-y-2">
                      {trazabilidad.produccion.actividades.map((act) => (
                        <div
                          key={act.id}
                          className="flex items-center justify-between p-3 bg-muted/50 rounded"
                        >
                          <div>
                            <Badge variant="secondary">{act.tipoActividad}</Badge>
                            <span className="ml-2 text-sm">{formatDate(act.fechaActividad)}</span>
                          </div>
                          <div className="text-sm text-muted-foreground">
                            {act.responsable}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </div>
          </SimpleAccordion>

          {/* Empaque */}
          <SimpleAccordion title="Empaque" icon={<Factory className="h-5 w-5" />}>
            <div className="grid grid-cols-2 gap-6">
              <div className="space-y-4">
                <h4 className="font-semibold">Recepción</h4>
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div>
                    <p className="text-muted-foreground">Código</p>
                    <p className="font-mono">{trazabilidad.empaque.codigoRecepcion}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Fecha</p>
                    <p>{formatDate(trazabilidad.empaque.fechaRecepcion)}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Cantidad</p>
                    <p>{trazabilidad.empaque.cantidadRecibida}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Estado</p>
                    <Badge variant="outline">{trazabilidad.empaque.estadoRecepcion}</Badge>
                  </div>
                </div>
              </div>
              <div className="space-y-4">
                <h4 className="font-semibold">Clasificación</h4>
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div>
                    <p className="text-muted-foreground">Código</p>
                    <p className="font-mono">{trazabilidad.empaque.codigoClasificacion}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Fecha</p>
                    <p>{formatDate(trazabilidad.empaque.fechaClasificacion)}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Calidad</p>
                    <Badge variant="success">{trazabilidad.empaque.calidad}</Badge>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Calibre</p>
                    <Badge variant="outline">{trazabilidad.empaque.calibre || '-'}</Badge>
                  </div>
                </div>
              </div>
            </div>

            {trazabilidad.empaque.palletId && (
              <>
                <Separator className="my-4" />
                <div className="space-y-2">
                  <h4 className="font-semibold">Pallet</h4>
                  <div className="grid grid-cols-4 gap-3 text-sm">
                    <div>
                      <p className="text-muted-foreground">Código</p>
                      <p className="font-mono">{trazabilidad.empaque.codigoPallet}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Tipo</p>
                      <p>{trazabilidad.empaque.tipoPallet || '-'}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Cajas</p>
                      <p>{trazabilidad.empaque.numeroCajas}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Peso</p>
                      <p>
                        {trazabilidad.empaque.pesoNeto} kg neto / {trazabilidad.empaque.pesoBruto} kg
                        bruto
                      </p>
                    </div>
                  </div>
                </div>
              </>
            )}

            {trazabilidad.empaque.controlesCalidad.length > 0 && (
              <>
                <Separator className="my-4" />
                <div>
                  <h4 className="font-semibold mb-3">Controles de Calidad</h4>
                  <div className="space-y-2">
                    {trazabilidad.empaque.controlesCalidad.map((cc) => (
                      <div
                        key={cc.id}
                        className="flex items-center justify-between p-3 bg-muted/50 rounded"
                      >
                        <div className="flex items-center gap-3">
                          {cc.resultado === 'APROBADO' ? (
                            <CheckCircle className="h-5 w-5 text-green-600" />
                          ) : (
                            <XCircle className="h-5 w-5 text-red-600" />
                          )}
                          <div>
                            <Badge variant="secondary">{cc.tipoControl}</Badge>
                            <span className="ml-2 text-sm">{formatDate(cc.fechaControl)}</span>
                          </div>
                        </div>
                        <Badge
                          variant={cc.resultado === 'APROBADO' ? 'success' : 'destructive'}
                        >
                          {cc.resultado}
                        </Badge>
                      </div>
                    ))}
                  </div>
                </div>
              </>
            )}
          </SimpleAccordion>

          {/* Logística */}
          {trazabilidad.logistica && (
            <SimpleAccordion title="Logística" icon={<Truck className="h-5 w-5" />}>
              <div className="space-y-4">
                <div className="grid grid-cols-4 gap-4 text-sm">
                  <div>
                    <p className="text-muted-foreground">Código Envío</p>
                    <p className="font-mono font-medium">{trazabilidad.logistica.codigoEnvio}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Estado</p>
                    <Badge variant="default">{trazabilidad.logistica.estadoEnvio}</Badge>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Destino</p>
                    <p>
                      {trazabilidad.logistica.paisDestino} - {trazabilidad.logistica.puertoDestino}
                    </p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Transporte</p>
                    <p>{trazabilidad.logistica.tipoTransporte}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Contenedor</p>
                    <p className="font-mono">{trazabilidad.logistica.codigoContenedor || '-'}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Temperatura</p>
                    <p>
                      {trazabilidad.logistica.temperaturaContenedor
                        ? `${trazabilidad.logistica.temperaturaContenedor}°C`
                        : '-'}
                    </p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">B/L</p>
                    <p className="font-mono">{trazabilidad.logistica.numeroBL || '-'}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Incoterm</p>
                    <p>{trazabilidad.logistica.incoterm || '-'}</p>
                  </div>
                </div>

                {/* Hash de cierre */}
                {trazabilidad.logistica.cerrado && trazabilidad.logistica.hashCierre && (
                  <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
                    <div className="flex items-center gap-2 mb-2">
                      <Hash className="h-5 w-5 text-green-600" />
                      <span className="font-semibold text-green-800">Envío Cerrado - Inmutable</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <code className="text-xs bg-white px-2 py-1 rounded border flex-1 overflow-hidden">
                        {trazabilidad.logistica.hashCierre}
                      </code>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => copyToClipboard(trazabilidad.logistica!.hashCierre!)}
                      >
                        <Copy className="h-4 w-4" />
                      </Button>
                    </div>
                    <p className="text-xs text-green-700 mt-1">
                      Cerrado el {formatDateTime(trazabilidad.logistica.fechaCierre)} por{' '}
                      {trazabilidad.logistica.usuarioCierre}
                    </p>
                  </div>
                )}

                {/* Eventos */}
                {trazabilidad.logistica.eventos.length > 0 && (
                  <>
                    <Separator />
                    <div>
                      <h4 className="font-semibold mb-3">
                        Eventos ({trazabilidad.logistica.eventos.length})
                      </h4>
                      <div className="space-y-2">
                        {trazabilidad.logistica.eventos.map((evt) => (
                          <div
                            key={evt.id}
                            className="flex items-center justify-between p-3 bg-muted/50 rounded"
                          >
                            <div>
                              <Badge variant="secondary">{evt.tipoEvento}</Badge>
                              <span className="ml-2 text-sm">
                                {evt.ubicacion}
                                {evt.ciudad && `, ${evt.ciudad}`}
                              </span>
                            </div>
                            <span className="text-sm text-muted-foreground">
                              {formatDateTime(evt.fechaEvento)}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </>
                )}

                {/* Documentos */}
                {trazabilidad.logistica.documentos.length > 0 && (
                  <>
                    <Separator />
                    <div>
                      <h4 className="font-semibold mb-3">
                        Documentos ({trazabilidad.logistica.documentos.length})
                      </h4>
                      <div className="space-y-2">
                        {trazabilidad.logistica.documentos.map((doc) => (
                          <div
                            key={doc.id}
                            className="flex items-center justify-between p-3 bg-muted/50 rounded"
                          >
                            <div className="flex items-center gap-3">
                              <FileText className="h-5 w-5 text-muted-foreground" />
                              <div>
                                <p className="font-medium">{doc.tipoDocumento}</p>
                                <p className="text-sm text-muted-foreground">
                                  {doc.numeroDocumento} | {doc.entidadEmisora}
                                </p>
                              </div>
                            </div>
                            <Badge
                              variant={doc.estado === 'APROBADO' ? 'success' : 'secondary'}
                            >
                              {doc.estado}
                            </Badge>
                          </div>
                        ))}
                      </div>
                    </div>
                  </>
                )}
              </div>
            </SimpleAccordion>
          )}

          {/* Certificaciones */}
          {trazabilidad.certificaciones.length > 0 && (
            <SimpleAccordion title="Certificaciones" icon={<Award className="h-5 w-5" />}>
              <div className="space-y-3">
                {trazabilidad.certificaciones.map((cert) => (
                  <div
                    key={cert.id}
                    className="flex items-center justify-between p-3 bg-muted/50 rounded"
                  >
                    <div className="flex items-center gap-3">
                      <Award className="h-6 w-6 text-yellow-600" />
                      <div>
                        <p className="font-medium">{cert.tipoCertificacion}</p>
                        <p className="text-sm text-muted-foreground">
                          {cert.numeroCertificado} | {cert.entidadEmisora}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Vigencia: {formatDate(cert.fechaEmision)} -{' '}
                          {formatDate(cert.fechaVencimiento)}
                        </p>
                      </div>
                    </div>
                    <Badge variant={cert.estado === 'VIGENTE' ? 'success' : 'secondary'}>
                      {cert.estado}
                    </Badge>
                  </div>
                ))}
              </div>
            </SimpleAccordion>
          )}
        </div>
      )}

      {!trazabilidad && !loading && (
        <Card className="border-dashed">
          <CardContent className="py-12 flex flex-col items-center justify-center">
            <Search className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-muted-foreground text-center">
              Seleccione una etiqueta y presione "Consultar" para ver su trazabilidad completa
            </p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

export default TrazabilidadInternaPage;
