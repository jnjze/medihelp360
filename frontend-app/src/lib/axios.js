import axios from 'axios';

import { CONFIG } from 'src/global-config';

// ----------------------------------------------------------------------

const axiosInstance = axios.create({
  baseURL: CONFIG.serverUrl,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
  // Configuración para CORS
  withCredentials: false,
  timeout: 10000,
});

// Interceptor de request para debugging y manejo de CORS
axiosInstance.interceptors.request.use((config) => {
  // Log para debugging de CORS
  const fullURL = config.baseURL ? `${config.baseURL}${config.url}` : config.url;
  console.log('🚀 API Request:', {
    method: config.method?.toUpperCase(),
    url: config.url,
    baseURL: config.baseURL || 'relative (proxy)',
    fullURL,
    headers: config.headers,
    isProxy: !config.baseURL,
  });

  // Agregar token si existe
  const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  // Agregar headers adicionales para CORS
  config.headers['X-Requested-With'] = 'XMLHttpRequest';
  
  return config;
}, (error) => {
  console.error('❌ Request interceptor error:', error);
  return Promise.reject(error);
});

axiosInstance.interceptors.response.use(
  (response) => {
    // Log de respuesta exitosa
    console.log('✅ API Response:', {
      status: response.status,
      url: response.config.url,
      data: response.data,
    });
    return response;
  },
  (error) => {
    // Log detallado de errores para debugging
    console.error('❌ API Error:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      method: error.config?.method,
      data: error.response?.data,
      headers: error.response?.headers,
      corsError: error.message?.includes('CORS') || error.message?.includes('blocked'),
    });

    // Manejar errores de CORS específicamente
    if (error.message?.includes('CORS') || error.message?.includes('blocked')) {
      const corsError = new Error('CORS Error: Request blocked by browser. Check proxy configuration.');
      corsError.isCorsError = true;
      corsError.originalError = error;
      return Promise.reject(corsError);
    }

    // Manejar errores estructurados del backend
    if (error?.response?.data) {
      const errorData = error.response.data;
      
      // Si es un ErrorResponse estructurado del backend
      if (errorData.error && errorData.message) {
        const structuredError = new Error(errorData.message);
        structuredError.errorData = errorData;
        structuredError.status = errorData.status;
        structuredError.errorCode = errorData.errorCode;
        structuredError.validationErrors = errorData.validationErrors;
        structuredError.suggestion = errorData.suggestion;
        
        console.error('Structured API error:', errorData);
        return Promise.reject(structuredError);
      }
    }
    
    // Fallback para errores no estructurados
    const message = error?.response?.data?.message || error?.message || 'Something went wrong!';
    console.error('Axios error:', message);
    return Promise.reject(new Error(message));
  }
);

export default axiosInstance;

// ----------------------------------------------------------------------

export const fetcher = async (args) => {
  try {
    const [url, config] = Array.isArray(args) ? args : [args, {}];

    const res = await axiosInstance.get(url, config);

    return res.data;
  } catch (error) {
    console.error('Fetcher failed:', error);
    throw error;
  }
};

// ----------------------------------------------------------------------

export const endpoints = {
  // MediHelp360 API endpoints
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    logout: '/auth/logout',
    validate: '/auth/validate',
    refresh: '/auth/refresh',
  },
  users: {
    list: '/users',
    details: '/users',
    create: '/users',
    update: '/users',
    delete: '/users',
  },
  roles: {
    list: '/roles',
    details: '/roles',
    create: '/roles',
    update: '/roles',
    delete: '/roles',
  },
  // Legacy endpoints (mantener para compatibilidad)
  chat: '/api/chat',
  kanban: '/api/kanban',
  calendar: '/api/calendar',
  mail: {
    list: '/api/mail/list',
    details: '/api/mail/details',
    labels: '/api/mail/labels',
  },
  post: {
    list: '/api/post/list',
    details: '/api/post/details',
    latest: '/api/post/post/latest',
    search: '/api/post/search',
  },
  product: {
    list: '/api/product/list',
    details: '/api/product/details',
    search: '/api/product/search',
  },
};
