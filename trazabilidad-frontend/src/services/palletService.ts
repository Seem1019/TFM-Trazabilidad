import api from './api';
import type { Pallet, PalletRequest, ApiResponse } from '@/types';

const BASE_URL = '/pallets';

export const palletService = {
  getAll: async (): Promise<Pallet[]> => {
    const response = await api.get<ApiResponse<Pallet[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<Pallet> => {
    const response = await api.get<ApiResponse<Pallet>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getByEstado: async (estado: string): Promise<Pallet[]> => {
    const response = await api.get<ApiResponse<Pallet[]>>(`${BASE_URL}/estado/${estado}`);
    return response.data.data;
  },

  getByDestino: async (destino: string): Promise<Pallet[]> => {
    const response = await api.get<ApiResponse<Pallet[]>>(`${BASE_URL}/destino?destino=${destino}`);
    return response.data.data;
  },

  getByTipoFruta: async (tipoFruta: string): Promise<Pallet[]> => {
    const response = await api.get<ApiResponse<Pallet[]>>(`${BASE_URL}/tipo-fruta/${tipoFruta}`);
    return response.data.data;
  },

  getListosEnvio: async (): Promise<Pallet[]> => {
    const response = await api.get<ApiResponse<Pallet[]>>(`${BASE_URL}/listos-envio`);
    return response.data.data;
  },

  create: async (data: PalletRequest): Promise<Pallet> => {
    const response = await api.post<ApiResponse<Pallet>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: PalletRequest): Promise<Pallet> => {
    const response = await api.put<ApiResponse<Pallet>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  cambiarEstado: async (id: number, estado: string): Promise<Pallet> => {
    const response = await api.patch<ApiResponse<Pallet>>(
      `${BASE_URL}/${id}/estado?estado=${estado}`
    );
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default palletService;
