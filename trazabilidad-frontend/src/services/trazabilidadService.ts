import api from './api';

// Tipos para trazabilidad pública
export interface TrazabilidadPublica {
  codigoEtiqueta: string;
  tipoProducto: string;
  calidad: string;
  variedad: string;
  origen: OrigenInfo;
  produccion: ProduccionInfo;
  empaque: EmpaqueInfo;
  logistica?: LogisticaInfo;
  certificaciones: CertificacionPublica[];
}

export interface OrigenInfo {
  finca: string;
  municipio: string;
  departamento: string;
  pais: string;
  codigoLote: string;
  nombreLote: string;
  areaHectareas?: number;
  fechaSiembra?: string;
}

export interface ProduccionInfo {
  fechaCosecha?: string;
  estadoFruta?: string;
  actividadesRegistradas: number;
  tiposActividades: string[];
}

export interface EmpaqueInfo {
  fechaRecepcion?: string;
  fechaClasificacion?: string;
  calidadClasificada: string;
  calibre?: string;
  controlesCalidadAprobados: boolean;
}

export interface LogisticaInfo {
  estadoEnvio: string;
  paisDestino: string;
  puertoDestino?: string;
  fechaSalidaEstimada?: string;
  tipoTransporte: string;
  eventos: EventoLogisticoPublico[];
}

export interface EventoLogisticoPublico {
  tipoEvento: string;
  fechaEvento: string;
  ubicacion: string;
  ciudad?: string;
  pais?: string;
}

export interface CertificacionPublica {
  tipoCertificacion: string;
  entidadEmisora: string;
  estado: string;
}

// Tipos para trazabilidad completa (interna)
export interface TrazabilidadCompleta {
  etiquetaId: number;
  codigoEtiqueta: string;
  codigoQr: string;
  tipoEtiqueta: string;
  estadoEtiqueta: string;
  urlQr: string;
  origen: OrigenInfoCompleta;
  produccion: ProduccionInfoCompleta;
  empaque: EmpaqueInfoCompleta;
  logistica?: LogisticaInfoCompleta;
  certificaciones: CertificacionCompleta[];
  auditoria: AuditoriaInfo;
}

export interface OrigenInfoCompleta {
  fincaId: number;
  fincaNombre: string;
  fincaCodigo: string;
  municipio: string;
  departamento: string;
  pais: string;
  areaTotal?: number;
  contactoResponsable?: string;
  telefonoContacto?: string;
  emailContacto?: string;
  loteId: number;
  codigoLote: string;
  nombreLote: string;
  tipoFruta: string;
  variedad: string;
  areaHectareas?: number;
  fechaSiembra?: string;
  estadoLote: string;
}

export interface ProduccionInfoCompleta {
  cosechaId?: number;
  codigoCosecha?: string;
  fechaCosecha?: string;
  cantidadCosechada?: number;
  unidadMedida?: string;
  estadoFruta?: string;
  responsableCosecha?: string;
  actividades: ActividadAgronomicaInfo[];
  totalActividades: number;
}

export interface ActividadAgronomicaInfo {
  id: number;
  tipoActividad: string;
  fechaActividad: string;
  descripcion?: string;
  responsable?: string;
  productosAplicados?: string;
}

export interface EmpaqueInfoCompleta {
  recepcionId: number;
  codigoRecepcion: string;
  fechaRecepcion: string;
  cantidadRecibida: number;
  estadoRecepcion: string;
  responsableRecepcion?: string;
  clasificacionId: number;
  codigoClasificacion: string;
  fechaClasificacion: string;
  calidad: string;
  calibre?: string;
  cantidadClasificada: number;
  responsableClasificacion?: string;
  controlesCalidad: ControlCalidadInfo[];
  palletId?: number;
  codigoPallet?: string;
  tipoPallet?: string;
  numeroCajas?: number;
  pesoNeto?: number;
  pesoBruto?: number;
  estadoPallet?: string;
}

export interface ControlCalidadInfo {
  id: number;
  fechaControl: string;
  tipoControl: string;
  resultado: string;
  observaciones?: string;
  inspector?: string;
}

export interface LogisticaInfoCompleta {
  envioId: number;
  codigoEnvio: string;
  fechaCreacion: string;
  fechaSalidaEstimada?: string;
  fechaSalidaReal?: string;
  estadoEnvio: string;
  exportador?: string;
  paisDestino: string;
  puertoDestino?: string;
  ciudadDestino?: string;
  tipoTransporte: string;
  transportista?: string;
  codigoContenedor?: string;
  tipoContenedor?: string;
  temperaturaContenedor?: number;
  numeroBooking?: string;
  numeroBL?: string;
  totalPallets?: number;
  totalCajas?: number;
  pesoNetoTotal?: number;
  pesoBrutoTotal?: number;
  clienteImportador?: string;
  incoterm?: string;
  eventos: EventoLogisticoInfo[];
  documentos: DocumentoInfo[];
  cerrado: boolean;
  hashCierre?: string;
  fechaCierre?: string;
  usuarioCierre?: string;
}

export interface EventoLogisticoInfo {
  id: number;
  tipoEvento: string;
  fechaEvento: string;
  ubicacion: string;
  ciudad?: string;
  pais?: string;
  descripcion?: string;
}

export interface DocumentoInfo {
  id: number;
  tipoDocumento: string;
  numeroDocumento: string;
  fechaEmision: string;
  entidadEmisora?: string;
  estado: string;
}

export interface CertificacionCompleta {
  id: number;
  tipoCertificacion: string;
  numeroCertificado?: string;
  entidadEmisora: string;
  fechaEmision?: string;
  fechaVencimiento?: string;
  estado: string;
}

export interface AuditoriaInfo {
  fechaCreacionEtiqueta: string;
  fechaUltimaActualizacion: string;
  creadoPor: string;
  empresaId: number;
  empresaNombre: string;
  totalEventosAuditoria: number;
}

export const trazabilidadService = {
  // Consulta pública (sin autenticación)
  getPublica: async (codigoQr: string): Promise<TrazabilidadPublica> => {
    const response = await api.get<{ data: TrazabilidadPublica }>(
      `/etiquetas/public/qr/${codigoQr}`
    );
    return response.data.data;
  },

  // Consulta completa interna (requiere autenticación)
  getCompleta: async (etiquetaId: number): Promise<TrazabilidadCompleta> => {
    const response = await api.get<{ data: TrazabilidadCompleta }>(
      `/etiquetas/${etiquetaId}/trazabilidad`
    );
    return response.data.data;
  },
};

export default trazabilidadService;
