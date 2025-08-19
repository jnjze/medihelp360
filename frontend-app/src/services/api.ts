import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';

// Configuración base de la API
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
const API_TIMEOUT = parseInt(process.env.REACT_APP_API_TIMEOUT || '5000');

// Crear instancia de Axios
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Interceptor para requests - agregar token de autenticación
apiClient.interceptors.request.use(
  (config) => {
    // Agregar token si existe
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Agregar headers de identificación
    config.headers['X-Frontend-Request'] = 'medihelp360-react';
    config.headers['X-Frontend-Version'] = process.env.REACT_APP_VERSION || '1.0.0';

    if (process.env.REACT_APP_DEBUG === 'true') {
      console.log('🚀 API Request:', {
        url: config.url,
        method: config.method,
        headers: config.headers,
        data: config.data,
      });
    }

    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Interceptor para responses - manejo de errores globales
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    if (process.env.REACT_APP_DEBUG === 'true') {
      console.log('✅ API Response:', {
        url: response.config.url,
        status: response.status,
        data: response.data,
      });
    }
    return response;
  },
  (error: AxiosError) => {
    console.error('❌ Response Error:', {
      url: error.config?.url,
      status: error.response?.status,
      message: error.message,
      data: error.response?.data,
    });

    // Manejo de errores específicos
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // Sin permisos
      console.warn('🚫 Access denied');
    } else if (error.response?.status && error.response.status >= 500) {
      // Error del servidor
      console.error('🔥 Server error');
    }

    return Promise.reject(error);
  }
);

// API Endpoints
export const api = {
  // Health check
  health: () => apiClient.get('/actuator/health'),

  // Users
  users: {
    getAll: () => apiClient.get('/api/users'),
    getById: (id: number) => apiClient.get(`/api/users/${id}`),
    create: (userData: any) => apiClient.post('/api/users', userData),
    update: (id: number, userData: any) => apiClient.put(`/api/users/${id}`, userData),
    delete: (id: number) => apiClient.delete(`/api/users/${id}`),
  },

  // Roles
  roles: {
    getAll: () => apiClient.get('/api/roles'),
    create: (roleData: any) => apiClient.post('/api/roles', roleData),
  },

  // Sync Services (para monitoreo)
  sync: {
    serviceA: {
      health: () => apiClient.get('/sync-a/actuator/health'),
      status: () => apiClient.get('/sync-a/api/sync/status'),
    },
    serviceB: {
      health: () => apiClient.get('/sync-b/actuator/health'),
      status: () => apiClient.get('/sync-b/api/sync/status'),
    },
    serviceC: {
      health: () => apiClient.get('/sync-c/actuator/health'),
      status: () => apiClient.get('/sync-c/api/sync/status'),
    },
  },

  // Autenticación (para futuro)
  auth: {
    login: (credentials: { email: string; password: string }) =>
      apiClient.post('/api/auth/login', credentials),
    logout: () => apiClient.post('/api/auth/logout'),
    refresh: () => apiClient.post('/api/auth/refresh'),
  },
};

export default apiClient;
