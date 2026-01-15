import api from './api';
import type { Certificacion, CertificacionRequest, ApiResponse } from '@/types';

const BASE_URL = '/certificaciones';

export const certificacionService = {
  getByFinca: async (fincaId: number): Promise<Certificacion[]> => {
    const response = await api.get<ApiResponse<Certificacion[]>>(`${BASE_URL}/finca/${fincaId}`);
    return response.data.data;
  },

  getVigentesByFinca: async (fincaId: number): Promise<Certificacion[]> => {
    const response = await api.get<ApiResponse<Certificacion[]>>(`${BASE_URL}/finca/${fincaId}/vigentes`);
    return response.data.data;
  },

  getById: async (id: number): Promise<Certificacion> => {
    const response = await api.get<ApiResponse<Certificacion>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  getProximasAVencer: async (): Promise<Certificacion[]> => {
    const response = await api.get<ApiResponse<Certificacion[]>>(`${BASE_URL}/proximas-vencer`);
    return response.data.data;
  },

  create: async (data: CertificacionRequest): Promise<Certificacion> => {
    const response = await api.post<ApiResponse<Certificacion>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: CertificacionRequest): Promise<Certificacion> => {
    const response = await api.put<ApiResponse<Certificacion>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default certificacionService;
