// ==================== ENVIO ====================
export type EstadoEnvio =
  | 'CREADO'
  | 'EN_PREPARACION'
  | 'LISTO_ENVIO'
  | 'EN_TRANSITO'
  | 'EN_PUERTO_ORIGEN'
  | 'EN_PUERTO_DESTINO'
  | 'ENTREGADO'
  | 'CERRADO'
  | 'CANCELADO';

export type TipoTransporte = 'MARITIMO' | 'AEREO' | 'TERRESTRE';

export type Incoterm = 'FOB' | 'CIF' | 'EXW' | 'FCA' | 'CPT' | 'CIP' | 'DAP' | 'DPU' | 'DDP';

export interface EnvioRequest {
  codigoEnvio: string;
  fechaCreacion: string;
  fechaSalidaEstimada?: string;
  exportador?: string;
  paisDestino: string;
  puertoDestino?: string;
  ciudadDestino?: string;
  tipoTransporte: TipoTransporte;
  codigoContenedor?: string;
  tipoContenedor?: string;
  temperaturaContenedor?: number;
  transportista?: string;
  numeroBooking?: string;
  numeroBL?: string;
  observaciones?: string;
  clienteImportador?: string;
  incoterm?: Incoterm;
  palletsIds?: number[];
}

export interface Envio extends Omit<EnvioRequest, 'palletsIds'> {
  id: number;
  usuarioId: number;
  usuarioNombre: string;
  empresaId: number;
  empresaNombre: string;
  fechaSalidaReal?: string;
  estado: EstadoEnvio;
  pesoNetoTotal?: number;
  pesoBrutoTotal?: number;
  numeroPallets?: number;
  numeroCajas?: number;
  hashCierre?: string;
  fechaCierre?: string;
  usuarioCierreNombre?: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  numeroEventos: number;
  numeroDocumentos: number;
}

// ==================== EVENTO LOGISTICO ====================
export type TipoEvento =
  | 'CARGA'
  | 'SALIDA_PLANTA'
  | 'ARRIBO_PUERTO'
  | 'CONSOLIDACION'
  | 'DESPACHO'
  | 'ARRIBO_DESTINO';

export interface EventoLogisticoRequest {
  envioId: number;
  codigoEvento?: string;
  tipoEvento: TipoEvento;
  fechaEvento: string;
  horaEvento: string;
  ubicacion: string;
  ciudad?: string;
  pais?: string;
  latitud?: number;
  longitud?: number;
  responsable: string;
  organizacion?: string;
  temperaturaRegistrada?: number;
  humedadRegistrada?: number;
  vehiculo?: string;
  conductor?: string;
  numeroPrecinto?: string;
  observaciones?: string;
  urlEvidencia?: string;
  incidencia?: boolean;
  detalleIncidencia?: string;
}

export interface EventoLogistico extends Omit<EventoLogisticoRequest, 'envioId'> {
  id: number;
  envioId: number;
  codigoEnvio: string;
  fechaHoraEvento: string;
  activo: boolean;
  createdAt: string;
}

// ==================== DOCUMENTO EXPORTACION ====================
export type TipoDocumento =
  | 'PACKING_LIST'
  | 'CERTIFICADO_FITOSANITARIO'
  | 'FACTURA_COMERCIAL'
  | 'BL'
  | 'CERTIFICADO_ORIGEN'
  | 'LISTA_EMPAQUE'
  | 'OTRO';

export type EstadoDocumento = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO' | 'VENCIDO';

export type Moneda = 'USD' | 'EUR' | 'COP' | 'GBP' | 'JPY';

export interface DocumentoExportacionRequest {
  envioId: number;
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  fechaEmision: string;
  fechaVencimiento?: string;
  entidadEmisora?: string;
  funcionarioEmisor?: string;
  urlArchivo?: string;
  tipoArchivo?: string;
  tamanoArchivo?: number;
  hashArchivo?: string;
  descripcion?: string;
  valorDeclarado?: number;
  moneda?: Moneda;
  obligatorio?: boolean;
}

export interface DocumentoExportacion extends Omit<DocumentoExportacionRequest, 'envioId'> {
  id: number;
  envioId: number;
  codigoEnvio: string;
  estaVencido: boolean;
  tieneArchivo: boolean;
  estado: EstadoDocumento;
  estaAprobado: boolean;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

// ==================== CONSTANTES ====================
export const ESTADOS_ENVIO: EstadoEnvio[] = [
  'CREADO',
  'EN_PREPARACION',
  'LISTO_ENVIO',
  'EN_TRANSITO',
  'EN_PUERTO_ORIGEN',
  'EN_PUERTO_DESTINO',
  'ENTREGADO',
  'CERRADO',
  'CANCELADO',
];

export const TIPOS_TRANSPORTE: TipoTransporte[] = ['MARITIMO', 'AEREO', 'TERRESTRE'];

export const INCOTERMS: Incoterm[] = ['FOB', 'CIF', 'EXW', 'FCA', 'CPT', 'CIP', 'DAP', 'DPU', 'DDP'];

export const TIPOS_EVENTO: TipoEvento[] = [
  'CARGA',
  'SALIDA_PLANTA',
  'ARRIBO_PUERTO',
  'CONSOLIDACION',
  'DESPACHO',
  'ARRIBO_DESTINO',
];

export const TIPOS_DOCUMENTO: TipoDocumento[] = [
  'PACKING_LIST',
  'CERTIFICADO_FITOSANITARIO',
  'FACTURA_COMERCIAL',
  'BL',
  'CERTIFICADO_ORIGEN',
  'LISTA_EMPAQUE',
  'OTRO',
];

export const ESTADOS_DOCUMENTO: EstadoDocumento[] = ['PENDIENTE', 'APROBADO', 'RECHAZADO', 'VENCIDO'];

export const MONEDAS: Moneda[] = ['USD', 'EUR', 'COP', 'GBP', 'JPY'];

export const TIPOS_CONTENEDOR = [
  '20ft Standard',
  '40ft Standard',
  '40ft High Cube',
  '20ft Reefer',
  '40ft Reefer',
] as const;

// Labels para mostrar en UI
export const TIPO_EVENTO_LABELS: Record<TipoEvento, string> = {
  CARGA: 'Carga',
  SALIDA_PLANTA: 'Salida de Planta',
  ARRIBO_PUERTO: 'Arribo a Puerto',
  CONSOLIDACION: 'Consolidación',
  DESPACHO: 'Despacho',
  ARRIBO_DESTINO: 'Arribo a Destino',
};

export const TIPO_DOCUMENTO_LABELS: Record<TipoDocumento, string> = {
  PACKING_LIST: 'Packing List',
  CERTIFICADO_FITOSANITARIO: 'Certificado Fitosanitario',
  FACTURA_COMERCIAL: 'Factura Comercial',
  BL: 'Bill of Lading (B/L)',
  CERTIFICADO_ORIGEN: 'Certificado de Origen',
  LISTA_EMPAQUE: 'Lista de Empaque',
  OTRO: 'Otro',
};

export const ESTADO_ENVIO_LABELS: Record<EstadoEnvio, string> = {
  CREADO: 'Creado',
  EN_PREPARACION: 'En Preparación',
  LISTO_ENVIO: 'Listo para Envío',
  EN_TRANSITO: 'En Tránsito',
  EN_PUERTO_ORIGEN: 'En Puerto Origen',
  EN_PUERTO_DESTINO: 'En Puerto Destino',
  ENTREGADO: 'Entregado',
  CERRADO: 'Cerrado',
  CANCELADO: 'Cancelado',
};
