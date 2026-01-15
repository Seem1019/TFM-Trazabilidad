// ==================== FINCA ====================
export interface FincaRequest {
  codigoFinca: string;
  nombre: string;
  ubicacion?: string;
  municipio?: string;
  departamento?: string;
  pais?: string;
  areaHectareas?: number;
  propietario?: string;
  encargado?: string;
  telefono?: string;
  email?: string;
  latitud?: number;
  longitud?: number;
  observaciones?: string;
}

export interface Finca extends FincaRequest {
  id: number;
  empresaId: number;
  empresaNombre: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  totalLotes: number;
  totalCertificacionesVigentes: number;
}

// ==================== LOTE ====================
export interface LoteRequest {
  fincaId: number;
  codigoLote: string;
  nombre: string;
  tipoFruta: string;
  variedad?: string;
  areaHectareas?: number;
  fechaSiembra?: string;
  fechaPrimeraCosechaEstimada?: string;
  densidadSiembra?: number;
  ubicacionInterna?: string;
  observaciones?: string;
}

export interface Lote extends Omit<LoteRequest, 'fincaId'> {
  id: number;
  fincaId: number;
  fincaNombre: string;
  estadoLote: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  edadEnDias: number;
  listoParaCosechar: boolean;
  totalCosechado: number;
  totalActividades: number;
}

// ==================== COSECHA ====================
export interface CosechaRequest {
  loteId: number;
  fechaCosecha: string;
  cantidadCosechada: number;
  unidadMedida: string;
  calidadInicial?: string;
  estadoFruta?: string;
  responsableCosecha?: string;
  numeroTrabajadores?: number;
  horaInicio?: string;
  horaFin?: string;
  temperaturaAmbiente?: number;
  observaciones?: string;
}

export interface Cosecha extends Omit<CosechaRequest, 'loteId'> {
  id: number;
  loteId: number;
  loteNombre: string;
  loteCodigoLote: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  rendimientoPorHectarea: number;
  reciente: boolean;
}

// ==================== CERTIFICACION ====================
export type EstadoCertificacion = 'VIGENTE' | 'VENCIDA' | 'SUSPENDIDA';

export interface CertificacionRequest {
  fincaId: number;
  tipoCertificacion: string;
  entidadEmisora: string;
  numeroCertificado?: string;
  fechaEmision: string;
  fechaVencimiento: string;
  estado?: string;
  alcance?: string;
  urlDocumento?: string;
  observaciones?: string;
}

export interface Certificacion extends Omit<CertificacionRequest, 'fincaId' | 'estado'> {
  id: number;
  fincaId: number;
  fincaNombre: string;
  estado: EstadoCertificacion;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  vigente: boolean;
  diasParaVencer: number;
}

// ==================== ACTIVIDAD AGRONOMICA ====================
export type TipoActividad = 'FERTILIZACION' | 'FUMIGACION' | 'RIEGO' | 'PODA' | 'DESHIERBE' | 'OTRO';

export interface ActividadAgronomicarRequest {
  loteId: number;
  tipoActividad: TipoActividad;
  fechaActividad: string;
  descripcion?: string;
  productosUtilizados?: string;
  dosificacion?: string;
  responsable?: string;
  observaciones?: string;
}

export interface ActividadAgronomica extends Omit<ActividadAgronomicarRequest, 'loteId'> {
  id: number;
  loteId: number;
  loteNombre: string;
  loteCodigoLote: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

// ==================== TIPOS DE FRUTA (Enum del backend) ====================
export const TIPOS_FRUTA = [
  'MANGO',
  'UCHUVA',
  'GULUPA',
  'PITAHAYA',
  'GRANADILLA',
  'AGUACATE',
  'LIMON_TAHITI',
  'BANANO',
  'PLATANO',
  'MARACUYA',
  'PAPAYA',
  'PINA',
  'OTRO',
] as const;

export type TipoFruta = (typeof TIPOS_FRUTA)[number];
