import api from './api';
import type { RecepcionPlanta, RecepcionPlantaRequest, ApiResponse } from '@/types';

const BASE_URL = '/recepciones';

export const recepcionService = {
  getAll: async (): Promise<RecepcionPlanta[]> => {
    const response = await api.get<ApiResponse<RecepcionPlanta[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<RecepcionPlanta> => {
    const response = await api.get<ApiResponse<RecepcionPlanta>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getByLote: async (loteId: number): Promise<RecepcionPlanta[]> => {
    const response = await api.get<ApiResponse<RecepcionPlanta[]>>(`${BASE_URL}/lote/${loteId}`);
    return response.data.data;
  },

  getByEstado: async (estado: string): Promise<RecepcionPlanta[]> => {
    const response = await api.get<ApiResponse<RecepcionPlanta[]>>(`${BASE_URL}/estado/${estado}`);
    return response.data.data;
  },

  getByRangoFechas: async (desde: string, hasta: string): Promise<RecepcionPlanta[]> => {
    const response = await api.get<ApiResponse<RecepcionPlanta[]>>(
      `${BASE_URL}/rango-fechas?desde=${desde}&hasta=${hasta}`
    );
    return response.data.data;
  },

  create: async (data: RecepcionPlantaRequest): Promise<RecepcionPlanta> => {
    const response = await api.post<ApiResponse<RecepcionPlanta>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: RecepcionPlantaRequest): Promise<RecepcionPlanta> => {
    const response = await api.put<ApiResponse<RecepcionPlanta>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  cambiarEstado: async (id: number, estado: string): Promise<RecepcionPlanta> => {
    const response = await api.patch<ApiResponse<RecepcionPlanta>>(
      `${BASE_URL}/${id}/estado?estado=${estado}`
    );
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default recepcionService;
