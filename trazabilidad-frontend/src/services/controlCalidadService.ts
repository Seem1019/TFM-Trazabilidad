import api from './api';
import type { ControlCalidad, ControlCalidadRequest, ApiResponse } from '@/types';

const BASE_URL = '/controles-calidad';

export const controlCalidadService = {
  getAll: async (): Promise<ControlCalidad[]> => {
    const response = await api.get<ApiResponse<ControlCalidad[]>>(BASE_URL);
    return response.data.data;
  },

  getById: async (id: number): Promise<ControlCalidad> => {
    const response = await api.get<ApiResponse<ControlCalidad>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getByClasificacion: async (clasificacionId: number): Promise<ControlCalidad[]> => {
    const response = await api.get<ApiResponse<ControlCalidad[]>>(
      `${BASE_URL}/clasificacion/${clasificacionId}`
    );
    return response.data.data;
  },

  getByPallet: async (palletId: number): Promise<ControlCalidad[]> => {
    const response = await api.get<ApiResponse<ControlCalidad[]>>(`${BASE_URL}/pallet/${palletId}`);
    return response.data.data;
  },

  getByTipo: async (tipo: string): Promise<ControlCalidad[]> => {
    const response = await api.get<ApiResponse<ControlCalidad[]>>(`${BASE_URL}/tipo/${tipo}`);
    return response.data.data;
  },

  getByResultado: async (resultado: string): Promise<ControlCalidad[]> => {
    const response = await api.get<ApiResponse<ControlCalidad[]>>(
      `${BASE_URL}/resultado/${resultado}`
    );
    return response.data.data;
  },

  getByRangoFechas: async (desde: string, hasta: string): Promise<ControlCalidad[]> => {
    const response = await api.get<ApiResponse<ControlCalidad[]>>(
      `${BASE_URL}/rango-fechas?desde=${desde}&hasta=${hasta}`
    );
    return response.data.data;
  },

  create: async (data: ControlCalidadRequest): Promise<ControlCalidad> => {
    const response = await api.post<ApiResponse<ControlCalidad>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: ControlCalidadRequest): Promise<ControlCalidad> => {
    const response = await api.put<ApiResponse<ControlCalidad>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default controlCalidadService;
