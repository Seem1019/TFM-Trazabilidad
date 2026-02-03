import { create } from 'zustand';
import type { User, LoginRequest } from '@/types';
import { authService } from '@/services/authService';
import { SESSION_EXPIRED_EVENT, resetSessionExpiredFlag } from '@/services/api';

interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  sessionExpired: boolean;
  login: (credentials: LoginRequest) => Promise<boolean>;
  logout: () => Promise<void>;
  checkAuth: () => void;
  clearError: () => void;
  setUser: (user: User) => void;
  handleSessionExpired: () => void;
  clearSessionExpired: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: authService.getStoredUser(),
  token: authService.getStoredToken(),
  refreshToken: authService.getStoredRefreshToken(),
  isAuthenticated: authService.isAuthenticated(),
  isLoading: false,
  error: null,
  sessionExpired: false,

  login: async (credentials: LoginRequest) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authService.login(credentials);
      if (response.success) {
        const { accessToken, refreshToken, user } = response.data;
        authService.setAuthData(accessToken, refreshToken, user);
        // Resetear el flag de sesión expirada para permitir nuevos dispatches
        resetSessionExpiredFlag();
        set({
          user,
          token: accessToken,
          refreshToken,
          isAuthenticated: true,
          isLoading: false,
          error: null,
          sessionExpired: false,
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

  logout: async () => {
    await authService.logout();
    set({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      error: null,
    });
  },

  checkAuth: () => {
    const token = authService.getStoredToken();
    const refreshToken = authService.getStoredRefreshToken();
    const user = authService.getStoredUser();
    set({
      token,
      refreshToken,
      user,
      isAuthenticated: !!token && !!user,
    });
  },

  clearError: () => {
    set({ error: null });
  },

  setUser: (user: User) => {
    const currentToken = authService.getStoredToken() || '';
    const currentRefreshToken = authService.getStoredRefreshToken() || '';
    authService.setAuthData(currentToken, currentRefreshToken, user);
    set({ user });
  },

  handleSessionExpired: () => {
    authService.clearAuthData();
    set({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      sessionExpired: true,
      error: 'Tu sesión ha expirado. Por favor, inicia sesión nuevamente.',
    });
  },

  clearSessionExpired: () => {
    set({ sessionExpired: false, error: null });
  },
}));

// Escuchar evento de sesión expirada
if (typeof window !== 'undefined') {
  window.addEventListener(SESSION_EXPIRED_EVENT, () => {
    useAuthStore.getState().handleSessionExpired();
  });
}

export default useAuthStore;
