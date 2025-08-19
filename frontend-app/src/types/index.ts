// Tipos principales de la aplicación

export interface User {
  id: number;
  email: string;
  name: string;
  roles: Role[];
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: number;
  name: string;
  description: string;
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  PENDING = 'PENDING',
  SUSPENDED = 'SUSPENDED',
}

export interface CreateUserRequest {
  email: string;
  name: string;
  roleIds: number[];
  status: UserStatus;
}

export interface UpdateUserRequest {
  email?: string;
  name?: string;
  roleIds?: number[];
  status?: UserStatus;
}

export interface CreateRoleRequest {
  name: string;
  description: string;
}

// Tipos para el estado de sincronización
export interface SyncStatus {
  serviceName: string;
  status: 'HEALTHY' | 'UNHEALTHY' | 'UNKNOWN';
  lastSyncTime: string;
  recordsProcessed: number;
  errors: number;
}

// Tipos para respuestas de API
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// Tipos para formularios
export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'email' | 'select' | 'multiselect' | 'date';
  required: boolean;
  options?: { value: any; label: string }[];
  validation?: any;
}

// Tipos para navegación
export interface NavItem {
  path: string;
  label: string;
  icon?: string;
  children?: NavItem[];
  roles?: string[];
}

// Tipos para dashboard
export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  totalRoles: number;
  syncServices: SyncStatus[];
}

// Tipos para notificaciones
export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  timestamp: Date;
  read: boolean;
}

// Tipos para contexto de autenticación
export interface AuthContext {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  loading: boolean;
}

// Tipos para configuración de la app
export interface AppConfig {
  apiBaseUrl: string;
  environment: string;
  version: string;
  debug: boolean;
}
