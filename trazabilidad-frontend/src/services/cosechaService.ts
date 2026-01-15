import api from './api';
import type { Cosecha, CosechaRequest, ApiResponse } from '@/types';

const BASE_URL = '/cosechas';

export const cosechaService = {
  getAll: async (): Promise<Cosecha[]> => {
    const response = await api.get<ApiResponse<Cosecha[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<Cosecha> => {
    const response = await api.get<ApiResponse<Cosecha>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getByLote: async (loteId: number): Promise<Cosecha[]> => {
    const response = await api.get<ApiResponse<Cosecha[]>>(`${BASE_URL}/lote/${loteId}`);
    return response.data.data;
  },

  create: async (data: CosechaRequest): Promise<Cosecha> => {
    const response = await api.post<ApiResponse<Cosecha>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: CosechaRequest): Promise<Cosecha> => {
    const response = await api.put<ApiResponse<Cosecha>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default cosechaService;
