import api from './api';
import type { Envio, EnvioRequest, ApiResponse } from '@/types';

const BASE_URL = '/envios';

export const envioService = {
  getAll: async (): Promise<Envio[]> => {
    const response = await api.get<ApiResponse<Envio[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<Envio> => {
    const response = await api.get<ApiResponse<Envio>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getByEstado: async (estado: string): Promise<Envio[]> => {
    const response = await api.get<ApiResponse<Envio[]>>(`${BASE_URL}/estado/${estado}`);
    return response.data.data;
  },

  create: async (data: EnvioRequest): Promise<Envio> => {
    const response = await api.post<ApiResponse<Envio>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: EnvioRequest): Promise<Envio> => {
    const response = await api.put<ApiResponse<Envio>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  asignarPallets: async (id: number, palletsIds: number[]): Promise<Envio> => {
    const response = await api.post<ApiResponse<Envio>>(`${BASE_URL}/${id}/pallets`, { palletsIds });
    return response.data.data;
  },

  cambiarEstado: async (id: number, estado: string): Promise<Envio> => {
    const response = await api.patch<ApiResponse<Envio>>(`${BASE_URL}/${id}/estado`, { estado });
    return response.data.data;
  },

  cerrar: async (id: number): Promise<Envio> => {
    const response = await api.post<ApiResponse<Envio>>(`${BASE_URL}/${id}/cerrar`);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default envioService;
