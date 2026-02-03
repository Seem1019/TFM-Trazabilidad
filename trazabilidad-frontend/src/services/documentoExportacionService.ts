import api from './api';
import type { DocumentoExportacion, DocumentoExportacionRequest, ApiResponse } from '@/types';

const BASE_URL = '/documentos-exportacion';

export const documentoExportacionService = {
  getByEnvio: async (envioId: number): Promise<DocumentoExportacion[]> => {
    const response = await api.get<ApiResponse<DocumentoExportacion[]>>(`${BASE_URL}/envio/${envioId}`);
    return response.data.data;
  },

  getById: async (id: number): Promise<DocumentoExportacion> => {
    const response = await api.get<ApiResponse<DocumentoExportacion>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  create: async (data: DocumentoExportacionRequest): Promise<DocumentoExportacion> => {
    const response = await api.post<ApiResponse<DocumentoExportacion>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: DocumentoExportacionRequest): Promise<DocumentoExportacion> => {
    const response = await api.put<ApiResponse<DocumentoExportacion>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  cambiarEstado: async (id: number, estado: string): Promise<DocumentoExportacion> => {
    const response = await api.patch<ApiResponse<DocumentoExportacion>>(`${BASE_URL}/${id}/estado`, { estado });
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default documentoExportacionService;
