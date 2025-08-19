import axios from 'axios';

// Configuración base de la API
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const API_TIMEOUT = parseInt(import.meta.env.VITE_API_TIMEOUT || '5000');

// Crear instancia de Axios
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Interceptor para requests
apiClient.interceptors.request.use(
  (config) => {
    // Agregar token si existe
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Headers de identificación
    config.headers['X-Frontend-Request'] = 'medihelp360-vite-react';
    config.headers['X-Frontend-Version'] = import.meta.env.VITE_APP_VERSION || '1.0.0';

    if (import.meta.env.VITE_DEBUG === 'true') {
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

// Interceptor para responses
apiClient.interceptors.response.use(
  (response) => {
    if (import.meta.env.VITE_DEBUG === 'true') {
      console.log('✅ API Response:', {
        url: response.config.url,
        status: response.status,
        data: response.data,
      });
    }
    return response;
  },
  (error) => {
    console.error('❌ Response Error:', {
      url: error.config?.url,
      status: error.response?.status,
      message: error.message,
      data: error.response?.data,
    });

    // Manejo de errores específicos
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/auth/jwt/sign-in';
    } else if (error.response?.status === 403) {
      console.warn('🚫 Access denied');
    } else if (error.response?.status && error.response.status >= 500) {
      console.error('🔥 Server error');
    }

    return Promise.reject(error);
  }
);

// API Service
export const apiService = {
  // Health check
  health: () => apiClient.get('/actuator/health'),

  // Users
  users: {
    getAll: () => apiClient.get('/api/users'),
    getById: (id) => apiClient.get(`/api/users/${id}`),
    create: (userData) => apiClient.post('/api/users', userData),
    update: (id, userData) => apiClient.put(`/api/users/${id}`, userData),
    delete: (id) => apiClient.delete(`/api/users/${id}`),
  },

  // Roles
  roles: {
    getAll: () => apiClient.get('/api/roles'),
    create: (roleData) => apiClient.post('/api/roles', roleData),
  },

  // Sync Services
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

  // System Status
  system: {
    status: () => apiClient.get('/actuator/health'),
    metrics: () => apiClient.get('/actuator/metrics'),
  },
};

export default apiClient;
