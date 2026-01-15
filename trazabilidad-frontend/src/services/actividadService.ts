import api from './api';
import type { ActividadAgronomica, ActividadAgronomicarRequest, ApiResponse } from '@/types';

const BASE_URL = '/actividades';

export const actividadService = {
  getByLote: async (loteId: number): Promise<ActividadAgronomica[]> => {
    const response = await api.get<ApiResponse<ActividadAgronomica[]>>(`${BASE_URL}/lote/${loteId}`);
    return response.data.data;
  },

  getByLoteAndTipo: async (loteId: number, tipo: string): Promise<ActividadAgronomica[]> => {
    const response = await api.get<ApiResponse<ActividadAgronomica[]>>(`${BASE_URL}/lote/${loteId}/tipo/${tipo}`);
    return response.data.data;
  },

  getById: async (id: number): Promise<ActividadAgronomica> => {
    const response = await api.get<ApiResponse<ActividadAgronomica>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getRecientes: async (loteId: number): Promise<ActividadAgronomica[]> => {
    const response = await api.get<ApiResponse<ActividadAgronomica[]>>(`${BASE_URL}/lote/${loteId}/recientes`);
    return response.data.data;
  },

  create: async (data: ActividadAgronomicarRequest): Promise<ActividadAgronomica> => {
    const response = await api.post<ApiResponse<ActividadAgronomica>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: ActividadAgronomicarRequest): Promise<ActividadAgronomica> => {
    const response = await api.put<ApiResponse<ActividadAgronomica>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default actividadService;
