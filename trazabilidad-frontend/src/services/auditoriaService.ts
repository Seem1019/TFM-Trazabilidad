import api from './api';

export interface AuditoriaEvento {
  id: number;
  usuarioId: number;
  usuarioNombre: string;
  usuarioEmail: string;
  tipoEntidad: string;
  entidadId: number;
  codigoEntidad: string;
  tipoOperacion: string;
  descripcionOperacion: string;
  datosAnteriores: string | null;
  datosNuevos: string | null;
  camposModificados: string | null;
  hashEvento: string;
  hashAnterior: string | null;
  enCadena: boolean;
  integridadVerificada: boolean | null;
  ipOrigen: string | null;
  userAgent: string | null;
  empresaId: number;
  empresaNombre: string;
  modulo: string;
  nivelCriticidad: string;
  esCritico: boolean;
  fechaEvento: string;
}

export interface UserActivity {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: string;
  activo: boolean;
  ultimoAcceso: string | null;
  intentosFallidos: number;
  bloqueadoHasta: string | null;
  bloqueado: boolean;
  sesionesActivas: number;
}

export interface AuditoriaFiltros {
  modulo?: string;
  tipoOperacion?: string;
  nivelCriticidad?: string;
  usuarioId?: number;
  desde?: string;
  hasta?: string;
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}

export const auditoriaService = {
  listar: async (filtros?: AuditoriaFiltros): Promise<AuditoriaEvento[]> => {
    const params = new URLSearchParams();
    if (filtros?.modulo) params.append('modulo', filtros.modulo);
    if (filtros?.tipoOperacion) params.append('tipoOperacion', filtros.tipoOperacion);
    if (filtros?.nivelCriticidad) params.append('nivelCriticidad', filtros.nivelCriticidad);
    if (filtros?.usuarioId) params.append('usuarioId', String(filtros.usuarioId));
    if (filtros?.desde) params.append('desde', filtros.desde);
    if (filtros?.hasta) params.append('hasta', filtros.hasta);

    const query = params.toString();
    const url = `/auditoria${query ? `?${query}` : ''}`;
    const response = await api.get<AuditoriaEvento[]>(url);
    return response.data;
  },

  obtenerActividadUsuarios: async (): Promise<UserActivity[]> => {
    const response = await api.get<ApiResponse<UserActivity[]>>('/users/actividad');
    return response.data.data;
  },
};