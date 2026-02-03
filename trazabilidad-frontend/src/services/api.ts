import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

/**
 * Interfaz para la respuesta de error de la API (patrón REPR)
 */
interface ApiErrorResponse {
  success: boolean;
  timestamp: string;
  path: string;
  requestId: string;
  message: string;
}

/**
 * Extrae el mensaje de error de una respuesta de la API
 * Si no puede extraerlo, devuelve un mensaje genérico
 */
export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiErrorResponse>;

    // Intentar obtener el mensaje de la respuesta de la API
    if (axiosError.response?.data?.message) {
      return axiosError.response.data.message;
    }

    // Si hay respuesta pero no mensaje específico, usar status text
    if (axiosError.response?.statusText) {
      return `Error: ${axiosError.response.statusText}`;
    }

    // Si es error de red
    if (axiosError.code === 'ERR_NETWORK') {
      return 'Error de conexión. Verifique su conexión a internet.';
    }

    if (axiosError.code === 'ECONNABORTED') {
      return 'La solicitud tardó demasiado. Intente nuevamente.';
    }
  }

  // Si es un Error normal
  if (error instanceof Error) {
    return error.message;
  }

  return 'Ha ocurrido un error inesperado';
}

// Claves de localStorage (deben coincidir con authService)
const TOKEN_KEY = 'token';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_KEY = 'user';

// Evento personalizado para notificar expiración de sesión
export const SESSION_EXPIRED_EVENT = 'session-expired';

// Flag para evitar múltiples dispatches del evento
let sessionExpiredDispatched = false;

export function dispatchSessionExpired(): void {
  if (!sessionExpiredDispatched) {
    sessionExpiredDispatched = true;
    console.log('[Auth] Session expired - dispatching event');
    window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT));
  }
}

// Llamar al hacer login exitoso para resetear el flag
export function resetSessionExpiredFlag(): void {
  sessionExpiredDispatched = false;
}

/**
 * Decodifica un JWT y verifica si está expirado
 */
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp * 1000; // Convertir a milisegundos
    // Considerar expirado si quedan menos de 30 segundos
    return Date.now() >= exp - 30000;
  } catch {
    return true; // Si no se puede decodificar, considerar expirado
  }
}

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Flag para evitar múltiples refresh simultáneos
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
};

// Request interceptor - añade token JWT y verifica expiración
api.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    // No aplicar a rutas de auth
    if (config.url?.includes('/auth/login') || config.url?.includes('/auth/refresh')) {
      return config;
    }

    const token = localStorage.getItem(TOKEN_KEY);

    if (token) {
      // Verificar si el token está próximo a expirar
      if (isTokenExpired(token)) {
        console.log('[Auth] Token expired or expiring soon, attempting refresh...');

        // Intentar refresh proactivo
        const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
        if (refreshToken && !isRefreshing) {
          isRefreshing = true;
          try {
            const response = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });
            const responseData = response.data.data || response.data;
            const { accessToken, refreshToken: newRefreshToken } = responseData;

            if (accessToken) {
              localStorage.setItem(TOKEN_KEY, accessToken);
              if (newRefreshToken) {
                localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);
              }
              config.headers.Authorization = `Bearer ${accessToken}`;
              console.log('[Auth] Token refreshed successfully (proactive)');
            }
          } catch (err) {
            console.log('[Auth] Proactive refresh failed, will retry on 401');
          } finally {
            isRefreshing = false;
          }
        } else if (isRefreshing) {
          // Si ya hay un refresh en progreso, esperar
          return new Promise((resolve) => {
            failedQueue.push({
              resolve: (newToken: string) => {
                config.headers.Authorization = `Bearer ${newToken}`;
                resolve(config);
              },
              reject: () => {
                resolve(config); // Dejar que el response interceptor maneje el error
              },
            });
          });
        }
      } else {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor - maneja errores de autenticación con refresh automático
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Si es error 401 y no es un retry
    if (error.response?.status === 401 && !originalRequest._retry) {
      // No intentar refresh si es la propia llamada de refresh o login
      if (originalRequest.url?.includes('/auth/refresh') || originalRequest.url?.includes('/auth/login')) {
        console.log('[Auth] Auth endpoint failed, clearing session');
        clearAuthAndRedirect();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Si ya hay un refresh en progreso, encolar esta petición
        return new Promise((resolve, reject) => {
          failedQueue.push({
            resolve: (token: string) => {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              resolve(api(originalRequest));
            },
            reject: (err: unknown) => {
              reject(err);
            },
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

      if (!refreshToken) {
        console.log('[Auth] No refresh token available');
        isRefreshing = false;
        clearAuthAndRedirect();
        return Promise.reject(error);
      }

      try {
        console.log('[Auth] Attempting token refresh...');
        // Llamar al endpoint de refresh
        const response = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });

        // El backend envuelve la respuesta en ApiResponse, acceder a .data.data
        const responseData = response.data.data || response.data;
        const { accessToken, refreshToken: newRefreshToken } = responseData;

        if (!accessToken) {
          throw new Error('No access token in refresh response');
        }

        console.log('[Auth] Token refreshed successfully');

        // Guardar nuevos tokens
        localStorage.setItem(TOKEN_KEY, accessToken);
        if (newRefreshToken) {
          localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);
        }

        // Procesar cola de peticiones pendientes
        processQueue(null, accessToken);

        // Reintentar la petición original con el nuevo token
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        console.log('[Auth] Token refresh failed:', refreshError);
        // Refresh falló, limpiar auth y redirigir
        processQueue(refreshError, null);
        clearAuthAndRedirect();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // Para errores 403, también verificar si es un problema de sesión
    if (error.response?.status === 403) {
      const responseData = error.response.data as { message?: string };
      if (responseData?.message?.toLowerCase().includes('token') ||
          responseData?.message?.toLowerCase().includes('session') ||
          responseData?.message?.toLowerCase().includes('expired')) {
        console.log('[Auth] 403 with auth-related message, clearing session');
        clearAuthAndRedirect();
      }
    }

    // Transformar el error para que contenga el mensaje de la API
    const apiMessage = extractErrorMessage(error);
    const enhancedError = new Error(apiMessage) as Error & {
      originalError: AxiosError;
      status?: number;
      code?: string;
    };
    enhancedError.originalError = error;
    enhancedError.status = error.response?.status;
    enhancedError.code = error.code;

    return Promise.reject(enhancedError);
  }
);

function clearAuthAndRedirect(): void {
  console.log('[Auth] Clearing auth data and redirecting to login');

  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);

  // Disparar evento de sesión expirada para que el store se actualice
  dispatchSessionExpired();

  // Redirigir solo si no estamos ya en login y si estamos en el navegador
  if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
    // Pequeño delay para permitir que el evento se procese
    setTimeout(() => {
      window.location.href = '/login?sessionExpired=true';
    }, 100);
  }
}

export default api;
