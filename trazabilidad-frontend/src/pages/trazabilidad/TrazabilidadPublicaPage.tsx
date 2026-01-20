import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Leaf,
  MapPin,
  Calendar,
  Truck,
  Award,
  CheckCircle,
  Loader2,
  AlertCircle,
  Factory,
  Ship,
  Plane,
} from 'lucide-react';
import { trazabilidadService, type TrazabilidadPublica } from '@/services/trazabilidadService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';

export function TrazabilidadPublicaPage() {
  const { uuid } = useParams<{ uuid: string }>();
  const [data, setData] = useState<TrazabilidadPublica | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!uuid) {
        setError('Código QR no proporcionado');
        setLoading(false);
        return;
      }

      try {
        const result = await trazabilidadService.getPublica(uuid);
        setData(result);
      } catch (err) {
        setError('No se encontró información para este código QR');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [uuid]);

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-';
    try {
      return format(new Date(dateStr), 'dd MMMM yyyy', { locale: es });
    } catch {
      return dateStr;
    }
  };

  const getTransporteIcon = (tipo?: string) => {
    switch (tipo) {
      case 'MARITIMO':
        return <Ship className="h-5 w-5" />;
      case 'AEREO':
        return <Plane className="h-5 w-5" />;
      default:
        return <Truck className="h-5 w-5" />;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-green-50 to-emerald-100 flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="h-12 w-12 animate-spin text-green-600" />
          <p className="text-lg text-green-800">Cargando información de trazabilidad...</p>
        </div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-red-50 to-orange-100 flex items-center justify-center p-4">
        <Card className="max-w-md">
          <CardContent className="pt-6">
            <div className="flex flex-col items-center text-center gap-4">
              <AlertCircle className="h-16 w-16 text-red-500" />
              <h2 className="text-2xl font-bold text-red-800">Código no encontrado</h2>
              <p className="text-muted-foreground">
                {error || 'No se pudo obtener la información de trazabilidad para este producto.'}
              </p>
              <p className="text-sm text-muted-foreground">
                Verifique que el código QR sea válido o contacte al proveedor.
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 to-emerald-100">
      {/* Header */}
      <header className="bg-green-700 text-white py-6 px-4 shadow-lg">
        <div className="max-w-3xl mx-auto">
          <div className="flex items-center gap-3 mb-2">
            <div className="bg-white/20 p-2 rounded-full">
              <Leaf className="h-8 w-8" />
            </div>
            <div>
              <h1 className="text-2xl font-bold">Trazabilidad del Producto</h1>
              <p className="text-green-100">Información verificada y transparente</p>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto p-4 space-y-6">
        {/* Información del Producto */}
        <Card className="border-green-200">
          <CardHeader className="bg-green-50 rounded-t-lg">
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-green-800">{data.tipoProducto}</CardTitle>
                <p className="text-sm text-muted-foreground">
                  Variedad: {data.variedad || 'No especificada'}
                </p>
              </div>
              <div className="text-right">
                <Badge variant="success" className="text-lg px-3 py-1">
                  {data.calidad}
                </Badge>
                <p className="text-xs text-muted-foreground mt-1">Código: {data.codigoEtiqueta}</p>
              </div>
            </div>
          </CardHeader>
        </Card>

        {/* Origen */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-green-700">
              <MapPin className="h-5 w-5" />
              Origen
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Finca</p>
                <p className="font-semibold">{data.origen.finca}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Ubicación</p>
                <p className="font-semibold">
                  {data.origen.municipio}, {data.origen.departamento}
                </p>
                <p className="text-sm text-muted-foreground">{data.origen.pais}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Lote</p>
                <p className="font-semibold">{data.origen.nombreLote}</p>
                <p className="text-xs text-muted-foreground">Código: {data.origen.codigoLote}</p>
              </div>
              {data.origen.areaHectareas && (
                <div>
                  <p className="text-sm text-muted-foreground">Área</p>
                  <p className="font-semibold">{data.origen.areaHectareas} hectáreas</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Producción */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-green-700">
              <Calendar className="h-5 w-5" />
              Producción
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              {data.origen.fechaSiembra && (
                <div>
                  <p className="text-sm text-muted-foreground">Fecha de Siembra</p>
                  <p className="font-semibold">{formatDate(data.origen.fechaSiembra)}</p>
                </div>
              )}
              {data.produccion.fechaCosecha && (
                <div>
                  <p className="text-sm text-muted-foreground">Fecha de Cosecha</p>
                  <p className="font-semibold">{formatDate(data.produccion.fechaCosecha)}</p>
                </div>
              )}
              {data.produccion.estadoFruta && (
                <div>
                  <p className="text-sm text-muted-foreground">Estado de la Fruta</p>
                  <Badge variant="outline">{data.produccion.estadoFruta}</Badge>
                </div>
              )}
            </div>

            {data.produccion.tiposActividades && data.produccion.tiposActividades.length > 0 && (
              <div>
                <p className="text-sm text-muted-foreground mb-2">Actividades Agronómicas</p>
                <div className="flex flex-wrap gap-2">
                  {data.produccion.tiposActividades.map((act, i) => (
                    <Badge key={i} variant="secondary">
                      {act.replace('_', ' ')}
                    </Badge>
                  ))}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {data.produccion.actividadesRegistradas} actividades registradas
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Empaque */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-green-700">
              <Factory className="h-5 w-5" />
              Empaque y Calidad
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              {data.empaque.fechaRecepcion && (
                <div>
                  <p className="text-sm text-muted-foreground">Recepción en Planta</p>
                  <p className="font-semibold">{formatDate(data.empaque.fechaRecepcion)}</p>
                </div>
              )}
              {data.empaque.fechaClasificacion && (
                <div>
                  <p className="text-sm text-muted-foreground">Clasificación</p>
                  <p className="font-semibold">{formatDate(data.empaque.fechaClasificacion)}</p>
                </div>
              )}
              <div>
                <p className="text-sm text-muted-foreground">Calidad Clasificada</p>
                <Badge variant="success">{data.empaque.calidadClasificada}</Badge>
              </div>
              {data.empaque.calibre && (
                <div>
                  <p className="text-sm text-muted-foreground">Calibre</p>
                  <Badge variant="outline">{data.empaque.calibre.replace('_', ' ')}</Badge>
                </div>
              )}
            </div>

            <Separator />

            <div className="flex items-center gap-3">
              {data.empaque.controlesCalidadAprobados ? (
                <>
                  <CheckCircle className="h-6 w-6 text-green-600" />
                  <div>
                    <p className="font-semibold text-green-700">Control de Calidad Aprobado</p>
                    <p className="text-sm text-muted-foreground">
                      El producto pasó los controles de calidad requeridos
                    </p>
                  </div>
                </>
              ) : (
                <>
                  <AlertCircle className="h-6 w-6 text-yellow-600" />
                  <div>
                    <p className="font-semibold text-yellow-700">Control de Calidad Pendiente</p>
                    <p className="text-sm text-muted-foreground">
                      Los controles de calidad están en proceso
                    </p>
                  </div>
                </>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Logística */}
        {data.logistica && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-green-700">
                {getTransporteIcon(data.logistica.tipoTransporte)}
                Logística y Transporte
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-muted-foreground">Estado del Envío</p>
                  <Badge variant="default">{data.logistica.estadoEnvio?.replace('_', ' ')}</Badge>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Destino</p>
                  <p className="font-semibold">{data.logistica.paisDestino}</p>
                  {data.logistica.puertoDestino && (
                    <p className="text-sm text-muted-foreground">
                      Puerto: {data.logistica.puertoDestino}
                    </p>
                  )}
                </div>
                {data.logistica.fechaSalidaEstimada && (
                  <div>
                    <p className="text-sm text-muted-foreground">Salida Estimada</p>
                    <p className="font-semibold">{formatDate(data.logistica.fechaSalidaEstimada)}</p>
                  </div>
                )}
                <div>
                  <p className="text-sm text-muted-foreground">Tipo de Transporte</p>
                  <p className="font-semibold">{data.logistica.tipoTransporte}</p>
                </div>
              </div>

              {/* Timeline de eventos */}
              {data.logistica.eventos && data.logistica.eventos.length > 0 && (
                <>
                  <Separator />
                  <div>
                    <p className="text-sm font-medium mb-3">Seguimiento del Envío</p>
                    <div className="space-y-3">
                      {data.logistica.eventos.map((evt, i) => (
                        <div key={i} className="flex gap-3">
                          <div className="flex flex-col items-center">
                            <div className="w-3 h-3 rounded-full bg-green-500" />
                            {i < data.logistica!.eventos.length - 1 && (
                              <div className="w-0.5 flex-1 bg-green-200 my-1" />
                            )}
                          </div>
                          <div className="flex-1 pb-3">
                            <p className="font-medium">{evt.tipoEvento?.replace('_', ' ')}</p>
                            <p className="text-sm text-muted-foreground">
                              {evt.ubicacion}
                              {evt.ciudad && `, ${evt.ciudad}`}
                              {evt.pais && ` - ${evt.pais}`}
                            </p>
                            <p className="text-xs text-muted-foreground">
                              {formatDate(evt.fechaEvento)}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </CardContent>
          </Card>
        )}

        {/* Certificaciones */}
        {data.certificaciones && data.certificaciones.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-green-700">
                <Award className="h-5 w-5" />
                Certificaciones
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-3">
                {data.certificaciones.map((cert, i) => (
                  <div
                    key={i}
                    className="flex items-center justify-between p-3 bg-green-50 rounded-lg"
                  >
                    <div className="flex items-center gap-3">
                      <Award className="h-8 w-8 text-green-600" />
                      <div>
                        <p className="font-semibold">{cert.tipoCertificacion}</p>
                        <p className="text-sm text-muted-foreground">{cert.entidadEmisora}</p>
                      </div>
                    </div>
                    <Badge variant={cert.estado === 'VIGENTE' ? 'success' : 'secondary'}>
                      {cert.estado}
                    </Badge>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Footer */}
        <div className="text-center py-6 text-sm text-muted-foreground">
          <p>Sistema de Trazabilidad Agroexportadora</p>
          <p className="text-xs mt-1">
            Información actualizada al momento de la consulta. Para más detalles, contacte al
            exportador.
          </p>
        </div>
      </main>
    </div>
  );
}

export default TrazabilidadPublicaPage;
