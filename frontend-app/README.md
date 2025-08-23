# 🏥 MediHelp360 Frontend

> **Frontend moderno para sistema médico MediHelp360 usando Vite + React + Minimals Template**

## 🚀 **Nueva Arquitectura Frontend**

Esta es una **migración completa** del frontend original (Create React App) a una solución más moderna y profesional:

### **📊 Tecnologías Principales:**
- ⚡ **Vite** - Build tool ultra-rápido 
- ⚛️ **React 19** - Framework de UI
- 🎨 **Material-UI v7** - Componentes profesionales
- 📝 **TypeScript/JavaScript** - Tipado y lógica
- 🎯 **Minimals Template** - Plantilla premium adaptada

### **🎨 Características del UI:**
- **Diseño profesional médico** con colores apropiados
- **Sidebar colapsible** con navegación intuitiva
- **Header moderno** con breadcrumbs y controles
- **Responsive design** optimizado para móviles
- **Tema oscuro/claro** configurable
- **Animaciones fluidas** con Framer Motion
- **Componentes avanzados** (DataGrid, DatePickers, etc.)

## 📁 **Estructura del Proyecto**

```
frontend-app/
├── public/                     # Assets estáticos y recursos
│   ├── assets/                 # Íconos, ilustraciones, imágenes
│   └── logo/                   # Logos de MediHelp360
├── src/
│   ├── sections/               # Páginas principales organizadas por feature
│   │   ├── dashboard/          # Dashboard principal
│   │   ├── users/              # Gestión de usuarios
│   │   ├── api/                # Servicios de API
│   │   └── ...
│   ├── components/             # Componentes reutilizables
│   │   ├── iconify/            # Sistema de íconos
│   │   ├── nav-section/        # Navegación avanzada
│   │   ├── settings/           # Configuraciones de tema
│   │   └── ...
│   ├── layouts/                # Layouts y navegación
│   │   ├── dashboard/          # Layout principal con sidebar
│   │   └── components/         # Componentes del layout
│   ├── theme/                  # Sistema de temas avanzado
│   │   ├── core/               # Configuración base
│   │   └── with-settings/      # Temas dinámicos
│   ├── auth/                   # Sistema de autenticación (preparado)
│   └── routes/                 # Configuración de rutas
├── Dockerfile                  # Build multi-stage optimizado
├── nginx.conf                  # Configuración de Nginx
└── vite.config.js             # Configuración de Vite
```

## 🌟 **Páginas Implementadas**

### **📊 Dashboard Principal**
- **Estadísticas del sistema** en tiempo real
- **Tarjetas de métricas** con íconos médicos
- **Estado de servicios** con indicadores visuales
- **Información del ambiente** (desarrollo/producción)

### **👥 Gestión de Usuarios**
- **Tabla avanzada** con paginación y filtros
- **Modal de creación** con validación
- **Acciones por usuario** (editar, eliminar)
- **Estados visuales** con chips de colores

### **🔐 Gestión de Roles** (preparado)
- Estructura lista para implementar
- CRUD de roles y permisos

### **📈 Estado del Sistema** (preparado)
- Monitoreo de microservicios
- Métricas de salud en tiempo real

## 🔧 **Variables de Entorno**

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080    # URL del API Gateway
VITE_ENV=development                       # Ambiente (development/production)
VITE_APP_VERSION=1.0.0                    # Versión de la aplicación
VITE_DEBUG=true                           # Activar logs de debug
```

## 🚀 **Desarrollo Local**

### **Inicio Rápido:**
```bash
# Instalar dependencias
npm install

# Desarrollo con hot reload (puerto 4040)
npm run dev

# Build para producción
npm run build

# Preview del build
npm run start
```

### **URLs de Desarrollo:**
- **Frontend**: http://localhost:4040
- **API Gateway**: http://localhost:8080

## 🐳 **Docker & Producción**

### **Build Docker:**
```bash
# Build de la imagen
docker build -t medihelp360-frontend .

# Ejecutar contenedor
docker run -p 4040:3000 medihelp360-frontend
```

### **Variables Docker:**
```dockerfile
ARG VITE_API_BASE_URL=http://api-gateway:8080
ARG VITE_ENV=production
ARG VITE_APP_VERSION=1.0.0
```

## 🔌 **Integración con Backend**

### **API Service:**
```javascript
// Configuración automática
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'X-Frontend-Request': 'medihelp360-vite-react',
    'X-Frontend-Version': '1.0.0'
  }
});

// Servicios disponibles
apiService.users.getAll()           // GET /api/users
apiService.roles.getAll()           // GET /api/roles
apiService.sync.serviceA.health()   // GET /sync-a/actuator/health
```

### **Autenticación:**
- Sistema JWT preparado
- Guards de rutas implementados
- Context de autenticación global

## 🎨 **Temas y Personalización**

### **Configuración de Temas:**
```javascript
// Presets disponibles
const themePresets = [
  'default',    // Azul médico
  'medical',    // Verde médico
  'orange',     // Naranja cálido
  'cyan',       // Cyan fresco
];

// Configuración dinámica
settings.setField('colorPreset', 'medical');
settings.setField('navLayout', 'vertical');
settings.setField('themeMode', 'dark');
```

### **Colores Médicos:**
- **Primary**: Azul médico profesional
- **Secondary**: Verde médico calmante  
- **Success**: Verde salud
- **Warning**: Amarillo atención
- **Error**: Rojo emergencia

## 📱 **Responsive Design**

- **Desktop**: Sidebar completa con navegación
- **Tablet**: Sidebar colapsible
- **Mobile**: Drawer navegación
- **Breakpoints**: xs, sm, md, lg, xl

## 🔍 **Características Avanzadas**

### **Sistema de Navegación:**
- **Breadcrumbs automáticos**
- **Búsqueda global** en el header
- **Shortcuts de teclado**
- **Navegación por roles**

### **Componentes Médicos:**
- **Tablas de pacientes** con filtros avanzados
- **Formularios médicos** con validación
- **Calendarios de citas** (preparado)
- **Gráficos de métricas** (preparado)

### **Performance:**
- **Code splitting** automático con Vite
- **Lazy loading** de rutas y componentes
- **Optimización de imágenes** WebP
- **Tree shaking** automático

## 📈 **Próximas Funcionalidades**

- [ ] **Sistema de citas médicas**
- [ ] **Gestión de expedientes**
- [ ] **Chat en tiempo real**
- [ ] **Notificaciones push**
- [ ] **Dashboard de métricas médicas**
- [ ] **Módulo de facturación**
- [ ] **Reportes avanzados**
- [ ] **Integración con dispositivos médicos**

## 🛠️ **Scripts Disponibles**

```bash
npm run dev          # Servidor desarrollo con HMR
npm run build        # Build optimizado para producción
npm run start        # Preview del build
npm run lint         # Linting con ESLint
npm run lint:fix     # Fix automático de errores
npm run fm:check     # Verificar formato Prettier
npm run fm:fix       # Fix formato automático
npm run fix:all      # Lint + formato en uno
npm run clean        # Limpiar node_modules y build
```

## 🔧 **Herramientas de Desarrollo**

- **ESLint** - Linting de código
- **Prettier** - Formato de código
- **Vite** - Build tool y dev server
- **Axios** - Cliente HTTP
- **React Hook Form** - Gestión de formularios
- **Yup/Zod** - Validación de esquemas

---

## 🚨 **Migración desde CRA**

### **Principales Cambios:**
1. **Vite en lugar de Webpack** - Build 10x más rápido
2. **Plantilla profesional** - UI/UX mejorado drasticamente
3. **Arquitectura modular** - Organización por features
4. **TypeScript opcional** - JavaScript moderno con JSX
5. **Puerto 4040** - Evita conflictos con Grafana (3000)

### **Backup:**
El frontend anterior se respaldó en `frontend-app-cra-backup/`

---

**¡El nuevo frontend de MediHelp360 está listo para revolucionar la experiencia médica digital! 🏥✨**