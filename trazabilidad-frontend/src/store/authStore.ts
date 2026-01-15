import { create } from 'zustand';
import type { User, LoginRequest } from '@/types';
import { authService } from '@/services/authService';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginRequest) => Promise<boolean>;
  logout: () => void;
  checkAuth: () => void;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: authService.getStoredUser(),
  token: authService.getStoredToken(),
  isAuthenticated: authService.isAuthenticated(),
  isLoading: false,
  error: null,

  login: async (credentials: LoginRequest) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authService.login(credentials);
      if (response.success) {
        const { token, user } = response.data;
        authService.setAuthData(token, user);
        set({
          user,
          token,
          isAuthenticated: true,
          isLoading: false,
          error: null,
        });
        return true;
      } else {
        set({
          isLoading: false,
          error: response.message || 'Error al iniciar sesión',
        });
        return false;
      }
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'Error al iniciar sesión';
      set({
        isLoading: false,
        error: message,
      });
      return false;
    }
  },

  logout: () => {
    authService.logout();
    set({
      user: null,
      token: null,
      isAuthenticated: false,
      error: null,
    });
  },

  checkAuth: () => {
    const token = authService.getStoredToken();
    const user = authService.getStoredUser();
    set({
      token,
      user,
      isAuthenticated: !!token && !!user,
    });
  },

  clearError: () => {
    set({ error: null });
  },
}));

export default useAuthStore;
