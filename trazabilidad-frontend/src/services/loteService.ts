import api from './api';
import type { Lote, LoteRequest, ApiResponse } from '@/types';

const BASE_URL = '/lotes';

export const loteService = {
  getAll: async (): Promise<Lote[]> => {
    const response = await api.get<ApiResponse<Lote[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<Lote> => {
    const response = await api.get<ApiResponse<Lote>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getByFinca: async (fincaId: number): Promise<Lote[]> => {
    const response = await api.get<ApiResponse<Lote[]>>(`${BASE_URL}/finca/${fincaId}`);
    return response.data.data;
  },

  create: async (data: LoteRequest): Promise<Lote> => {
    const response = await api.post<ApiResponse<Lote>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: LoteRequest): Promise<Lote> => {
    const response = await api.put<ApiResponse<Lote>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },

  toggleActivo: async (id: number): Promise<Lote> => {
    const response = await api.patch<ApiResponse<Lote>>(`${BASE_URL}/${id}/toggle-activo`);
    return response.data.data;
  },
};

export default loteService;
