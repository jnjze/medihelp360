# 🎨 Frontend MediHelp360

Frontend React moderno para el ecosistema de microservicios MediHelp360.

## 🚀 **Características**

- ✅ **React 18** con TypeScript
- ✅ **Material-UI** para interfaz moderna
- ✅ **React Router** para navegación SPA
- ✅ **Axios** para comunicación con API Gateway
- ✅ **Docker** con Nginx para producción
- ✅ **Hot reload** en desarrollo
- ✅ **Multi-environment** configuration

## 🏗️ **Arquitectura**

```
┌─────────────────┐    ┌──────────────────┐    ┌────────────────────┐
│   Users/Admin   │───▶│   API Gateway    │───▶│  Microservices     │
│   (Browser)     │    │  (Port 8080)     │    │  (Internal Only)   │
└─────────────────┘    └──────────────────┘    └────────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │ Frontend Service │
                       │ (React + Nginx)  │
                       │ (Internal Port)  │
                       └──────────────────┘
```

## 📁 **Estructura del Proyecto**

```
frontend-app/
├── public/                 # Archivos públicos
├── src/
│   ├── components/         # Componentes reutilizables
│   │   ├── Header.tsx
│   │   ├── Sidebar.tsx
│   │   └── NotificationProvider.tsx
│   ├── pages/             # Páginas principales
│   │   ├── Dashboard.tsx
│   │   ├── Users.tsx
│   │   ├── Roles.tsx
│   │   └── SystemStatus.tsx
│   ├── services/          # Servicios de API
│   │   └── api.ts
│   ├── types/             # Definiciones TypeScript
│   │   └── index.ts
│   └── utils/             # Utilidades
├── .env.development       # Variables de desarrollo
├── .env.production        # Variables de producción
├── Dockerfile            # Imagen Docker con Nginx
├── nginx.conf           # Configuración de Nginx
└── package.json         # Dependencias y scripts
```

## 🎯 **Páginas Disponibles**

### **📊 Dashboard**
- Estadísticas generales del sistema
- Estado de microservicios
- Información de configuración

### **👥 Usuarios**
- Listado de usuarios
- Crear/editar usuarios
- Gestión de estados (ACTIVE, INACTIVE, etc.)

### **🔐 Roles**
- Gestión de roles de sistema
- Crear nuevos roles
- Asignación de permisos

### **📡 Estado del Sistema**
- Monitoreo en tiempo real de servicios
- Health checks automáticos
- Métricas de rendimiento

## 🔧 **Variables de Entorno**

### **Desarrollo (.env.development)**
```bash
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_ENVIRONMENT=development
REACT_APP_DEBUG=true
```

### **Producción (.env.production)**
```bash
REACT_APP_API_BASE_URL=http://api-gateway:8080
REACT_APP_ENVIRONMENT=production
REACT_APP_DEBUG=false
```

## 🚀 **Desarrollo Local**

### **Prerrequisitos**
- Node.js 18+
- npm 9+

### **Instalación**
```bash
cd frontend-app
npm install
```

### **Ejecutar en desarrollo**
```bash
npm start
```
Abre [http://localhost:4040](http://localhost:4040)

**Nota:** El frontend usa el puerto 4040 tanto en desarrollo local como en Docker.

### **Build para producción**
```bash
npm run build
```

## 🐳 **Docker**

### **Build imagen**
```bash
docker build -t medihelp360-frontend .
```

### **Ejecutar contenedor**
```bash
docker run -p 3000:3000 medihelp360-frontend
```

### **Con variables de entorno**
```bash
docker run -p 3000:3000 \
  -e REACT_APP_API_BASE_URL=http://api-gateway:8080 \
  -e REACT_APP_ENVIRONMENT=production \
  medihelp360-frontend
```

## 🔗 **Integración con API Gateway**

El frontend se comunica con todos los microservicios a través del API Gateway:

### **Endpoints principales**
- `GET /api/users` - Listar usuarios
- `POST /api/users` - Crear usuario
- `PUT /api/users/{id}` - Actualizar usuario
- `DELETE /api/users/{id}` - Eliminar usuario
- `GET /api/roles` - Listar roles
- `POST /api/roles` - Crear rol

### **Health Checks**
- `GET /health/user-management/health` - Estado User Management
- `GET /health/sync-a/health` - Estado Sync Service A
- `GET /health/sync-b/health` - Estado Sync Service B
- `GET /health/sync-c/health` - Estado Sync Service C

## 🔒 **Seguridad**

### **Headers automáticos**
```typescript
// Todos los requests incluyen:
{
  'X-Frontend-Request': 'medihelp360-react',
  'X-Frontend-Version': '1.0.0',
  'Authorization': 'Bearer {token}' // Si disponible
}
```

### **CORS y CSP**
- Configurado en `nginx.conf`
- Headers de seguridad automáticos
- Política de contenido segura

## 📊 **Características de UI**

### **Tema personalizado**
- Colores médicos profesionales
- Azul primario: `#1976d2`
- Verde salud: `#2e7d32`
- Tipografía Roboto

### **Responsive Design**
- Mobile-first approach
- Sidebar colapsable
- Tablas adaptativas

### **Notificaciones**
- Sistema centralizado de notificaciones
- Tipos: success, error, warning, info
- Auto-hide configurable

## ⚡ **Rendimiento**

### **Optimizaciones**
- Lazy loading de componentes
- Compresión gzip (Nginx)
- Caché de assets estáticos
- Bundle splitting automático

### **Métricas**
- Health check endpoint: `/health`
- Tiempo de respuesta monitoreado
- Estado de conexión con API

## 🧪 **Testing**

```bash
# Ejecutar tests
npm test

# Coverage
npm run test:coverage

# E2E (si configurado)
npm run test:e2e
```

## 🔄 **CI/CD Integration**

El frontend se integra automáticamente con el pipeline de Jenkins:

1. **Build stage**: Compila React app
2. **Docker stage**: Crea imagen optimizada
3. **Deploy stage**: Actualiza en docker-compose
4. **Health check**: Verifica funcionamiento

## 📝 **Logs y Debugging**

### **Desarrollo**
```bash
# Logs de React
npm start

# Logs de Docker
docker logs frontend-service
```

### **Producción**
```bash
# Logs de Nginx
docker exec frontend-service tail -f /var/log/nginx/access.log
docker exec frontend-service tail -f /var/log/nginx/error.log
```

## 🤝 **Contribución**

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abrir Pull Request

## 📞 **Soporte**

- **Email**: support@medihelp360.com
- **Documentación**: Ver README principal del proyecto
- **Issues**: GitHub Issues del repositorio

---

**MediHelp360 Frontend** - Sistema de gestión médica moderno y escalable 🏥