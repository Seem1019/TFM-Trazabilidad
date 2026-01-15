export interface User {
  id: number;
  email: string;
  nombre: string;
  apellido: string;
  rol: TipoRol;
  empresaId: number;
  empresaNombre?: string;
  activo: boolean;
}

export type TipoRol =
  | 'ADMIN_SISTEMA'
  | 'ADMIN_EMPRESA'
  | 'PRODUCTOR'
  | 'OPERADOR_PLANTA'
  | 'OPERADOR_LOGISTICA'
  | 'AUDITOR';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    token: string;
    user: User;
  };
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordResetConfirm {
  token: string;
  newPassword: string;
}
