import api from './api';
import type { Etiqueta, EtiquetaRequest, ApiResponse } from '@/types';

const BASE_URL = '/etiquetas';

export const etiquetaService = {
  getAll: async (): Promise<Etiqueta[]> => {
    const response = await api.get<ApiResponse<Etiqueta[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<Etiqueta> => {
    const response = await api.get<ApiResponse<Etiqueta>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getByClasificacion: async (clasificacionId: number): Promise<Etiqueta[]> => {
    const response = await api.get<ApiResponse<Etiqueta[]>>(
      `${BASE_URL}/clasificacion/${clasificacionId}`
    );
    return response.data.data;
  },

  getByEstado: async (estado: string): Promise<Etiqueta[]> => {
    const response = await api.get<ApiResponse<Etiqueta[]>>(`${BASE_URL}/estado/${estado}`);
    return response.data.data;
  },

  getByTipo: async (tipo: string): Promise<Etiqueta[]> => {
    const response = await api.get<ApiResponse<Etiqueta[]>>(`${BASE_URL}/tipo/${tipo}`);
    return response.data.data;
  },

  create: async (data: EtiquetaRequest): Promise<Etiqueta> => {
    const response = await api.post<ApiResponse<Etiqueta>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: EtiquetaRequest): Promise<Etiqueta> => {
    const response = await api.put<ApiResponse<Etiqueta>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  cambiarEstado: async (id: number, estado: string): Promise<Etiqueta> => {
    const response = await api.patch<ApiResponse<Etiqueta>>(
      `${BASE_URL}/${id}/estado?estado=${estado}`
    );
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },

  // Trazabilidad interna (requiere autenticación)
  getTrazabilidad: async (id: number): Promise<unknown> => {
    const response = await api.get<ApiResponse<unknown>>(`${BASE_URL}/${id}/trazabilidad`);
    return response.data.data;
  },
};

export default etiquetaService;
