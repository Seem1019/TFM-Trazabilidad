// ==================== RECEPCION PLANTA ====================
export type EstadoRecepcion = 'PENDIENTE' | 'ACEPTADA' | 'RECHAZADA' | 'CLASIFICADA';

export interface RecepcionPlantaRequest {
  loteId: number;
  codigoRecepcion: string;
  fechaRecepcion: string;
  horaRecepcion?: string;
  cantidadRecibida: number;
  unidadMedida: string;
  temperaturaFruta?: number;
  estadoInicial?: string;
  responsableRecepcion?: string;
  vehiculoTransporte?: string;
  conductor?: string;
  observaciones?: string;
}

export interface RecepcionPlanta extends Omit<RecepcionPlantaRequest, 'loteId'> {
  id: number;
  loteId: number;
  loteNombre: string;
  loteCodigoLote: string;
  fincaNombre: string;
  estadoRecepcion: EstadoRecepcion;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  totalClasificaciones: number;
}

// ==================== CLASIFICACION ====================
export type CalidadClasificacion = 'PREMIUM' | 'PRIMERA' | 'SEGUNDA' | 'TERCERA' | 'DESCARTE';

export interface ClasificacionRequest {
  recepcionId: number;
  codigoClasificacion: string;
  fechaClasificacion: string;
  calidad: string;
  cantidadClasificada: number;
  unidadMedida: string;
  calibre?: string;
  porcentajeMerma?: number;
  cantidadMerma?: number;
  motivoMerma?: string;
  responsableClasificacion?: string;
  observaciones?: string;
}

export interface Clasificacion extends Omit<ClasificacionRequest, 'recepcionId'> {
  id: number;
  recepcionId: number;
  recepcionCodigo: string;
  loteNombre: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  totalEtiquetas: number;
  totalControlesCalidad: number;
}

// ==================== ETIQUETA ====================
export type TipoEtiqueta = 'CAJA' | 'PALLET';
export type EstadoEtiqueta = 'ACTIVA' | 'INACTIVA' | 'USADA' | 'ANULADA';

export interface EtiquetaRequest {
  clasificacionId: number;
  codigoEtiqueta: string;
  tipoEtiqueta: TipoEtiqueta;
  cantidadContenida?: number;
  unidadMedida?: string;
  pesoNeto?: number;
  pesoBruto?: number;
  numeroCajas?: number;
  observaciones?: string;
}

export interface Etiqueta extends Omit<EtiquetaRequest, 'clasificacionId'> {
  id: number;
  clasificacionId: number;
  clasificacionCodigo: string;
  calidad: string;
  codigoQr: string;
  estadoEtiqueta: EstadoEtiqueta;
  urlQr: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  loteOrigen: string;
  fincaOrigen: string;
}

// ==================== PALLET ====================
export type EstadoPallet = 'PREPARADO' | 'LISTO_ENVIO' | 'ENVIADO' | 'ENTREGADO' | 'RECHAZADO';

export interface PalletRequest {
  codigoPallet: string;
  fechaPaletizado: string;
  tipoPallet?: string;
  numeroCajas: number;
  pesoNetoTotal?: number;
  pesoBrutoTotal?: number;
  alturaPallet?: number;
  tipoFruta?: string;
  calidad?: string;
  destino?: string;
  temperaturaAlmacenamiento?: number;
  responsablePaletizado?: string;
  observaciones?: string;
  etiquetasIds?: number[];
}

export interface Pallet extends Omit<PalletRequest, 'etiquetasIds'> {
  id: number;
  estadoPallet: EstadoPallet;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  totalEtiquetas: number;
  etiquetasCodigos: string[];
}

// ==================== CONTROL DE CALIDAD ====================
export type TipoControl = 'FISICO' | 'QUIMICO' | 'ORGANOLEPTICO' | 'MICROBIOLOGICO';
export type ResultadoControl = 'APROBADO' | 'RECHAZADO' | 'CONDICIONAL';

export interface ControlCalidadRequest {
  clasificacionId?: number;
  palletId?: number;
  codigoControl: string;
  fechaControl: string;
  tipoControl: string;
  parametroEvaluado?: string;
  valorMedido?: string;
  valorEsperado?: string;
  cumpleEspecificacion?: boolean;
  resultado: ResultadoControl;
  responsableControl: string;
  laboratorio?: string;
  numeroCertificado?: string;
  accionCorrectiva?: string;
  observaciones?: string;
}

export interface ControlCalidad extends Omit<ControlCalidadRequest, 'clasificacionId' | 'palletId'> {
  id: number;
  clasificacionId?: number;
  clasificacionCodigo?: string;
  palletId?: number;
  palletCodigo?: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

// ==================== CONSTANTES ====================
export const CALIDADES = ['PREMIUM', 'PRIMERA', 'SEGUNDA', 'TERCERA', 'DESCARTE'] as const;

export const CALIBRES = ['EXTRA_GRANDE', 'GRANDE', 'MEDIANO', 'PEQUENO'] as const;

export const TIPOS_CONTROL = [
  'FISICO',
  'QUIMICO',
  'ORGANOLEPTICO',
  'MICROBIOLOGICO',
] as const;

export const RESULTADOS_CONTROL = ['APROBADO', 'RECHAZADO', 'CONDICIONAL'] as const;

export const ESTADOS_RECEPCION = ['PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'CLASIFICADA'] as const;

export const ESTADOS_PALLET = [
  'PREPARADO',
  'LISTO_ENVIO',
  'ENVIADO',
  'ENTREGADO',
  'RECHAZADO',
] as const;

export const ESTADOS_ETIQUETA = ['ACTIVA', 'INACTIVA', 'USADA', 'ANULADA'] as const;

export const TIPOS_ETIQUETA = ['CAJA', 'PALLET'] as const;
