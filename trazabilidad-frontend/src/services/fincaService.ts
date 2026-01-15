import api from './api';
import type { Finca, FincaRequest, ApiResponse } from '@/types';

const BASE_URL = '/fincas';

export const fincaService = {
  getAll: async (): Promise<Finca[]> => {
    const response = await api.get<ApiResponse<Finca[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<Finca> => {
    const response = await api.get<ApiResponse<Finca>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  create: async (data: FincaRequest): Promise<Finca> => {
    const response = await api.post<ApiResponse<Finca>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: FincaRequest): Promise<Finca> => {
    const response = await api.put<ApiResponse<Finca>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },

  toggleActivo: async (id: number): Promise<Finca> => {
    const response = await api.patch<ApiResponse<Finca>>(`${BASE_URL}/${id}/toggle-activo`);
    return response.data.data;
  },
};

export default fincaService;
