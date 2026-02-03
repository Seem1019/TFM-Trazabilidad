import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAuthStore } from '../authStore';
import { authService } from '@/services/authService';

// Mock authService
vi.mock('@/services/authService', () => ({
  authService: {
    getStoredUser: vi.fn(() => null),
    getStoredToken: vi.fn(() => null),
    getStoredRefreshToken: vi.fn(() => null),
    isAuthenticated: vi.fn(() => false),
    setAuthData: vi.fn(),
    logout: vi.fn(() => Promise.resolve()),
    login: vi.fn(),
  },
}));

const mockedAuthService = vi.mocked(authService);

describe('authStore', () => {
  beforeEach(() => {
    // Reset store before each test
    useAuthStore.setState({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    });
    vi.clearAllMocks();
  });

  it('should have initial state', () => {
    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
    expect(state.refreshToken).toBeNull();
    expect(state.isAuthenticated).toBe(false);
    expect(state.isLoading).toBe(false);
    expect(state.error).toBeNull();
  });

  it('should clear error', () => {
    useAuthStore.setState({ error: 'Test error' });

    const { clearError } = useAuthStore.getState();
    clearError();

    const state = useAuthStore.getState();
    expect(state.error).toBeNull();
  });

  it('should logout and clear state', async () => {
    useAuthStore.setState({
      user: { id: 1, email: 'test@test.com', nombre: 'Test', apellido: 'User', rol: 'ADMIN', empresaId: 1, activo: true },
      token: 'test-token',
      refreshToken: 'test-refresh-token',
      isAuthenticated: true,
    });

    const { logout } = useAuthStore.getState();
    await logout();

    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
    expect(state.refreshToken).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });

  it('should set user', () => {
    const { setUser } = useAuthStore.getState();
    const testUser = {
      id: 1,
      email: 'test@test.com',
      nombre: 'Test',
      apellido: 'User',
      rol: 'ADMIN' as const,
      empresaId: 1,
      activo: true,
    };

    setUser(testUser);

    const state = useAuthStore.getState();
    expect(state.user).toEqual(testUser);
  });

  describe('login', () => {
    it('should login successfully', async () => {
      const mockUser = {
        id: 1,
        email: 'test@test.com',
        nombre: 'Test',
        apellido: 'User',
        rol: 'ADMIN' as const,
        empresaId: 1,
        activo: true,
      };
      const mockAccessToken = 'jwt-access-token';
      const mockRefreshToken = 'jwt-refresh-token';

      mockedAuthService.login.mockResolvedValueOnce({
        success: true,
        data: {
          user: mockUser,
          accessToken: mockAccessToken,
          refreshToken: mockRefreshToken,
          tokenType: 'Bearer',
          expiresIn: 900,
        },
        message: 'Success',
      });

      const { login } = useAuthStore.getState();
      const result = await login({ email: 'test@test.com', password: 'password' });

      expect(result).toBe(true);
      const state = useAuthStore.getState();
      expect(state.user).toEqual(mockUser);
      expect(state.token).toBe(mockAccessToken);
      expect(state.refreshToken).toBe(mockRefreshToken);
      expect(state.isAuthenticated).toBe(true);
      expect(state.isLoading).toBe(false);
      expect(mockedAuthService.setAuthData).toHaveBeenCalledWith(mockAccessToken, mockRefreshToken, mockUser);
    });

    it('should handle login failure with success=false', async () => {
      mockedAuthService.login.mockResolvedValueOnce({
        success: false,
        data: null as any,
        message: 'Invalid credentials',
      });

      const { login } = useAuthStore.getState();
      const result = await login({ email: 'test@test.com', password: 'wrong' });

      expect(result).toBe(false);
      const state = useAuthStore.getState();
      expect(state.user).toBeNull();
      expect(state.isAuthenticated).toBe(false);
      expect(state.error).toBe('Invalid credentials');
    });

    it('should handle login error with default message', async () => {
      mockedAuthService.login.mockResolvedValueOnce({
        success: false,
        data: null as any,
        message: '',
      });

      const { login } = useAuthStore.getState();
      const result = await login({ email: 'test@test.com', password: 'wrong' });

      expect(result).toBe(false);
      const state = useAuthStore.getState();
      expect(state.error).toBe('Error al iniciar sesión');
    });

    it('should handle login exception', async () => {
      mockedAuthService.login.mockRejectedValueOnce(new Error('Network error'));

      const { login } = useAuthStore.getState();
      const result = await login({ email: 'test@test.com', password: 'password' });

      expect(result).toBe(false);
      const state = useAuthStore.getState();
      expect(state.error).toBe('Network error');
      expect(state.isLoading).toBe(false);
    });

    it('should handle non-Error exception', async () => {
      mockedAuthService.login.mockRejectedValueOnce('string error');

      const { login } = useAuthStore.getState();
      const result = await login({ email: 'test@test.com', password: 'password' });

      expect(result).toBe(false);
      const state = useAuthStore.getState();
      expect(state.error).toBe('Error al iniciar sesión');
    });

    it('should set loading state during login', async () => {
      let resolveLogin: (value: any) => void;
      const loginPromise = new Promise((resolve) => {
        resolveLogin = resolve;
      });
      mockedAuthService.login.mockReturnValueOnce(loginPromise as any);

      const { login } = useAuthStore.getState();
      const loginResultPromise = login({ email: 'test@test.com', password: 'password' });

      // Check loading state is true during login
      expect(useAuthStore.getState().isLoading).toBe(true);

      resolveLogin!({
        success: true,
        data: {
          user: { id: 1 },
          accessToken: 'token',
          refreshToken: 'refresh-token',
        },
        message: 'Success',
      });

      await loginResultPromise;
      expect(useAuthStore.getState().isLoading).toBe(false);
    });
  });

  describe('checkAuth', () => {
    it('should set authenticated when token and user exist', () => {
      const mockUser = { id: 1, email: 'test@test.com', nombre: 'Test' };
      mockedAuthService.getStoredToken.mockReturnValueOnce('valid-token');
      mockedAuthService.getStoredRefreshToken.mockReturnValueOnce('valid-refresh-token');
      mockedAuthService.getStoredUser.mockReturnValueOnce(mockUser as any);

      const { checkAuth } = useAuthStore.getState();
      checkAuth();

      const state = useAuthStore.getState();
      expect(state.token).toBe('valid-token');
      expect(state.refreshToken).toBe('valid-refresh-token');
      expect(state.user).toEqual(mockUser);
      expect(state.isAuthenticated).toBe(true);
    });

    it('should set not authenticated when token is missing', () => {
      mockedAuthService.getStoredToken.mockReturnValueOnce(null);
      mockedAuthService.getStoredRefreshToken.mockReturnValueOnce(null);
      mockedAuthService.getStoredUser.mockReturnValueOnce({ id: 1 } as any);

      const { checkAuth } = useAuthStore.getState();
      checkAuth();

      const state = useAuthStore.getState();
      expect(state.isAuthenticated).toBe(false);
    });

    it('should set not authenticated when user is missing', () => {
      mockedAuthService.getStoredToken.mockReturnValueOnce('valid-token');
      mockedAuthService.getStoredRefreshToken.mockReturnValueOnce('valid-refresh-token');
      mockedAuthService.getStoredUser.mockReturnValueOnce(null);

      const { checkAuth } = useAuthStore.getState();
      checkAuth();

      const state = useAuthStore.getState();
      expect(state.isAuthenticated).toBe(false);
    });
  });
});
