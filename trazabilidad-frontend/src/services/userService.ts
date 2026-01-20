import api from './api';
import type { UserResponse, UserRequest } from '@/types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}

const BASE_URL = '/users';

export const userService = {
  getAll: async (): Promise<UserResponse[]> => {
    const response = await api.get<ApiResponse<UserResponse[]>>(BASE_URL);
    return response.data.data;
  },

  getAllIncludingInactive: async (): Promise<UserResponse[]> => {
    const response = await api.get<ApiResponse<UserResponse[]>>(`${BASE_URL}/all`);
    return response.data.data;
  },

  getById: async (id: number): Promise<UserResponse> => {
    const response = await api.get<ApiResponse<UserResponse>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  create: async (data: UserRequest): Promise<UserResponse> => {
    const response = await api.post<ApiResponse<UserResponse>>(BASE_URL, data);
    return response.data.data;
  },

  update: async (id: number, data: UserRequest): Promise<UserResponse> => {
    const response = await api.put<ApiResponse<UserResponse>>(`${BASE_URL}/${id}`, data);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE_URL}/${id}`);
  },

  cambiarEstado: async (id: number, activo: boolean): Promise<UserResponse> => {
    const response = await api.patch<ApiResponse<UserResponse>>(`${BASE_URL}/${id}/estado`, { activo });
    return response.data.data;
  },
};

export default userService;
