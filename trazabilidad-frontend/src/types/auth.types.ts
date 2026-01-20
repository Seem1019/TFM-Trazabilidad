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
  | 'ADMIN'
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

// Tipos para gestión de usuarios (Admin)
export interface UserResponse {
  id: number;
  email: string;
  nombre: string;
  apellido?: string;
  telefono?: string;
  rol: TipoRol;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  ultimoAcceso?: string;
  empresaId: number;
  empresaNombre?: string;
}

export interface UserRequest {
  email: string;
  password?: string;
  nombre: string;
  apellido?: string;
  telefono?: string;
  rol: TipoRol;
  activo?: boolean;
}

export const ROLES_LABELS: Record<TipoRol, string> = {
  ADMIN: 'Administrador',
  PRODUCTOR: 'Productor',
  OPERADOR_PLANTA: 'Operador de Planta',
  OPERADOR_LOGISTICA: 'Operador de Logística',
  AUDITOR: 'Auditor',
};

export const ROLES_LIST: { value: TipoRol; label: string }[] = [
  { value: 'ADMIN', label: 'Administrador' },
  { value: 'PRODUCTOR', label: 'Productor' },
  { value: 'OPERADOR_PLANTA', label: 'Operador de Planta' },
  { value: 'OPERADOR_LOGISTICA', label: 'Operador de Logística' },
  { value: 'AUDITOR', label: 'Auditor' },
];
