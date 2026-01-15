import api from './api';
import type { Clasificacion, ClasificacionRequest, ApiResponse } from '@/types';

const BASE_URL = '/clasificaciones';

export const clasificacionService = {
  getAll: async (): Promise<Clasificacion[]> => {
    const response = await api.get<ApiResponse<Clasificacion[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<Clasificacion> => {
    const response = await api.get<ApiResponse<Clasificacion>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getByRecepcion: async (recepcionId: number): Promise<Clasificacion[]> => {
    const response = await api.get<ApiResponse<Clasificacion[]>>(
      `${BASE_URL}/recepcion/${recepcionId}`
    );
    return response.data.data;
  },

  getByCalidad: async (calidad: string): Promise<Clasificacion[]> => {
    const response = await api.get<ApiResponse<Clasificacion[]>>(`${BASE_URL}/calidad/${calidad}`);
    return response.data.data;
  },

  create: async (data: ClasificacionRequest): Promise<Clasificacion> => {
    const response = await api.post<ApiResponse<Clasificacion>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: ClasificacionRequest): Promise<Clasificacion> => {
    const response = await api.put<ApiResponse<Clasificacion>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default clasificacionService;
