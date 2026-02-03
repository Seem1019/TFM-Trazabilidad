import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { authService } from '../authService';

// Mock api module
vi.mock('../api', () => ({
  default: {
    post: vi.fn(() => Promise.resolve({ data: {} })),
    get: vi.fn(() => Promise.resolve({ data: {} })),
  },
}));

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      store = {};
    }),
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('authService', () => {
  beforeEach(() => {
    localStorageMock.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    localStorageMock.clear();
  });

  describe('logout', () => {
    it('should remove token, refreshToken and user from localStorage', async () => {
      localStorageMock.setItem('token', 'test-token');
      localStorageMock.setItem('refreshToken', 'test-refresh-token');
      localStorageMock.setItem('user', JSON.stringify({ id: 1, name: 'Test' }));

      await authService.logout();

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('refreshToken');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('user');
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when token exists', () => {
      localStorageMock.getItem.mockReturnValueOnce('valid-token');

      expect(authService.isAuthenticated()).toBe(true);
    });

    it('should return false when token does not exist', () => {
      localStorageMock.getItem.mockReturnValueOnce(null);

      expect(authService.isAuthenticated()).toBe(false);
    });
  });

  describe('getStoredUser', () => {
    it('should return parsed user when valid JSON exists', () => {
      const user = { id: 1, email: 'test@test.com', nombre: 'Test' };
      localStorageMock.getItem.mockReturnValueOnce(JSON.stringify(user));

      expect(authService.getStoredUser()).toEqual(user);
    });

    it('should return null when no user exists', () => {
      localStorageMock.getItem.mockReturnValueOnce(null);

      expect(authService.getStoredUser()).toBe(null);
    });

    it('should return null when stored value is invalid JSON', () => {
      localStorageMock.getItem.mockReturnValueOnce('invalid-json{');

      expect(authService.getStoredUser()).toBe(null);
    });
  });

  describe('getStoredToken', () => {
    it('should return token when it exists', () => {
      localStorageMock.getItem.mockReturnValueOnce('my-token');

      expect(authService.getStoredToken()).toBe('my-token');
    });

    it('should return null when token does not exist', () => {
      localStorageMock.getItem.mockReturnValueOnce(null);

      expect(authService.getStoredToken()).toBe(null);
    });
  });

  describe('setAuthData', () => {
    it('should store token, refreshToken and user in localStorage', () => {
      const accessToken = 'new-access-token';
      const refreshToken = 'new-refresh-token';
      const user = { id: 2, email: 'user@test.com', nombre: 'User' };

      authService.setAuthData(accessToken, refreshToken, user as any);

      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', accessToken);
      expect(localStorageMock.setItem).toHaveBeenCalledWith('refreshToken', refreshToken);
      expect(localStorageMock.setItem).toHaveBeenCalledWith('user', JSON.stringify(user));
    });
  });

  describe('getStoredRefreshToken', () => {
    it('should return refreshToken when it exists', () => {
      localStorageMock.getItem.mockReturnValueOnce('my-refresh-token');

      expect(authService.getStoredRefreshToken()).toBe('my-refresh-token');
    });

    it('should return null when refreshToken does not exist', () => {
      localStorageMock.getItem.mockReturnValueOnce(null);

      expect(authService.getStoredRefreshToken()).toBe(null);
    });
  });

  describe('updateTokens', () => {
    it('should update both tokens in localStorage', () => {
      const accessToken = 'updated-access-token';
      const refreshToken = 'updated-refresh-token';

      authService.updateTokens(accessToken, refreshToken);

      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', accessToken);
      expect(localStorageMock.setItem).toHaveBeenCalledWith('refreshToken', refreshToken);
    });
  });

  describe('clearAuthData', () => {
    it('should remove all auth data from localStorage', () => {
      localStorageMock.setItem('token', 'test-token');
      localStorageMock.setItem('refreshToken', 'test-refresh-token');
      localStorageMock.setItem('user', JSON.stringify({ id: 1 }));

      authService.clearAuthData();

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('refreshToken');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('user');
    });
  });
});
