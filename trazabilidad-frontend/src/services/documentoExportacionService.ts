import api from './api';
import type { DocumentoExportacion, DocumentoExportacionRequest } from '@/types';

const BASE_URL = '/documentos-exportacion';

export const documentoExportacionService = {
  getByEnvio: async (envioId: number): Promise<DocumentoExportacion[]> => {
    const response = await api.get<DocumentoExportacion[]>(`${BASE_URL}/envio/${envioId}`);
    return response.data;
  },

  getById: async (id: number): Promise<DocumentoExportacion> => {
    const response = await api.get<DocumentoExportacion>(`${BASE_URL}/${id}`);
    return response.data;
  },

  create: async (data: DocumentoExportacionRequest): Promise<DocumentoExportacion> => {
    const response = await api.post<DocumentoExportacion>(BASE_URL, data);
    return response.data;
  },

  update: async (id: number, data: DocumentoExportacionRequest): Promise<DocumentoExportacion> => {
    const response = await api.put<DocumentoExportacion>(`${BASE_URL}/${id}`, data);
    return response.data;
  },

  cambiarEstado: async (id: number, estado: string): Promise<DocumentoExportacion> => {
    const response = await api.patch<DocumentoExportacion>(`${BASE_URL}/${id}/estado`, { estado });
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default documentoExportacionService;
