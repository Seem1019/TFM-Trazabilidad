import api from './api';
import type { EventoLogistico, EventoLogisticoRequest, ApiResponse } from '@/types';

const BASE_URL = '/eventos-logisticos';

export const eventoLogisticoService = {
  getByEnvio: async (envioId: number): Promise<EventoLogistico[]> => {
    const response = await api.get<ApiResponse<EventoLogistico[]>>(`${BASE_URL}/envio/${envioId}`);
    return response.data.data;
  },

  getById: async (id: number): Promise<EventoLogistico> => {
    const response = await api.get<ApiResponse<EventoLogistico>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  create: async (data: EventoLogisticoRequest): Promise<EventoLogistico> => {
    const response = await api.post<ApiResponse<EventoLogistico>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: EventoLogisticoRequest): Promise<EventoLogistico> => {
    const response = await api.put<ApiResponse<EventoLogistico>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default eventoLogisticoService;
