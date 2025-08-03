# 🌿 Estrategia de Ramas y Ambientes - MediHelp360

## 🎯 **Configuración de Ramas**

### **Estructura de Branches**
```
main      ──► 🏭 PRODUCTION   (puertos 8080-8084)
   │
   ├─ preprod ──► 🧪 PRE-PROD  (puertos 8180-8184)
   │
   └─ develop ──► 💻 DEVELOPMENT (puertos 8280-8284)
        │
        └─ feature/* ──► 🌿 FEATURE BUILDS (sin deploy)
```

---

## 📊 **Ambientes y Configuración**

| Branch | Ambiente | Puertos | Compose File | Propósito |
|--------|----------|---------|--------------|-----------|
| `main` | **production** | 8080-8084 | `docker-compose.secure.yml` | 🏭 Producción |
| `preprod` | **preprod** | 8180-8184 | `docker-compose.preprod.yml` | 🧪 Pre-producción |
| `develop` | **development** | 8280-8284 | `docker-compose.dev.yml` | 💻 Desarrollo |
| `feature/*` | - | - | - | 🌿 Solo build |

---

## 🚀 **Flujo de Deploy Automático**

### **1️⃣ Development (develop → development)**
```bash
# Desarrollador hace push a develop
git checkout develop
git add .
git commit -m "Nueva funcionalidad"
git push origin develop

# Jenkins automáticamente (máx 5 min):
# ✅ Build de imágenes con tag "development-latest"
# ✅ Tests unitarios
# ✅ Deploy a ambiente development (puertos 8280-8284)
# ✅ Health checks relajados
```

### **2️⃣ Pre-Producción (preprod → preprod)**
```bash
# Merge a preprod para testing
git checkout preprod
git merge develop
git push origin preprod

# Jenkins automáticamente (máx 5 min):
# ✅ Build de imágenes con tag "preprod-latest"
# ✅ Tests unitarios + integration tests
# ✅ Deploy a ambiente preprod (puertos 8180-8184)
# ✅ Health checks completos
```

### **3️⃣ Producción (main → production)**
```bash
# Release a producción
git checkout main
git merge preprod
git push origin main

# Jenkins automáticamente (máx 5 min):
# ✅ Build de imágenes con tag "production-latest"
# ✅ Tests completos + integration tests
# ✅ Deploy a ambiente production (puertos 8080-8084)
# ✅ Health checks estrictos
# ✅ Verificación de Consul
```

---

## 🔧 **URLs de Acceso por Ambiente**

### **🏭 Production (main)**
- **API Gateway**: http://localhost:8080
- **User Management**: http://localhost:8081
- **Database Sync A**: http://localhost:8082
- **Database Sync B**: http://localhost:8083
- **Database Sync C**: http://localhost:8084

### **🧪 Pre-Production (preprod)**
- **API Gateway**: http://localhost:8180
- **User Management**: http://localhost:8181
- **Database Sync A**: http://localhost:8182
- **Database Sync B**: http://localhost:8183
- **Database Sync C**: http://localhost:8184

### **💻 Development (develop)**
- **API Gateway**: http://localhost:8280
- **User Management**: http://localhost:8281
- **Database Sync A**: http://localhost:8282
- **Database Sync B**: http://localhost:8283
- **Database Sync C**: http://localhost:8284

### **🔧 Servicios Comunes (todos los ambientes)**
- **Consul UI**: http://localhost:8500
- **Kafka UI**: http://localhost:8080 (si disponible)
- **Registry UI**: http://localhost:5001

---

## ⚙️ **Configuración de Pipeline**

### **Tests por Ambiente**
| Ambiente | Unit Tests | Integration Tests | Health Checks |
|----------|------------|-------------------|---------------|
| **development** | ✅ | ❌ | ⚠️ Relajados (timeout) |
| **preprod** | ✅ | ✅ | ✅ Completos |
| **production** | ✅ | ✅ | ✅ Estrictos + Consul |

### **Tags de Imágenes**
```bash
# Cada build genera dos tags:
${BUILD_NUMBER}-${GIT_COMMIT}     # Tag único por build
${ENVIRONMENT}-latest             # Tag latest por ambiente

# Ejemplos:
medihelp360-api-gateway:45-abc123f         # Build específico
medihelp360-api-gateway:production-latest  # Latest de producción
medihelp360-api-gateway:preprod-latest     # Latest de preprod
medihelp360-api-gateway:development-latest # Latest de desarrollo
```

---

## 🛠️ **Comandos Útiles**

### **Deploy Manual por Ambiente**
```bash
# Desarrollo
./scripts/manual-deploy.sh . develop development

# Pre-producción
./scripts/manual-deploy.sh . preprod preprod

# Producción
./scripts/manual-deploy.sh . main production

# Auto-detectar ambiente desde branch
./scripts/manual-deploy.sh . develop auto  # → development
./scripts/manual-deploy.sh . preprod auto  # → preprod
./scripts/manual-deploy.sh . main auto     # → production
```

### **Ver Estado de Ambientes**
```bash
# Estado de producción
./scripts/manual-deploy.sh status production

# Estado de preprod
./scripts/manual-deploy.sh status preprod

# Estado de desarrollo
./scripts/manual-deploy.sh status development
```

### **Rollback por Ambiente**
```bash
# Rollback en producción
./scripts/manual-deploy.sh rollback production

# Rollback en preprod
./scripts/manual-deploy.sh rollback preprod

# Rollback en desarrollo
./scripts/manual-deploy.sh rollback development
```

### **Deploy Directo con Scripts**
```bash
# Deploy específico por ambiente
./scripts/deploy.sh production
./scripts/deploy.sh preprod
./scripts/deploy.sh development
```

---

## 🔄 **Workflow Recomendado**

### **Para Desarrollo Diario**
```bash
# 1. Crear feature branch
git checkout develop
git pull origin develop
git checkout -b feature/nueva-funcionalidad

# 2. Desarrollar y commitear
git add .
git commit -m "feat: nueva funcionalidad"
git push origin feature/nueva-funcionalidad

# 3. Merge a develop (via PR o directo)
git checkout develop
git merge feature/nueva-funcionalidad
git push origin develop
# → Auto-deploy a development environment

# 4. Testing en preprod
git checkout preprod
git merge develop
git push origin preprod
# → Auto-deploy a preprod environment

# 5. Release a producción
git checkout main
git merge preprod
git push origin main
# → Auto-deploy a production environment
```

### **Para Hotfixes**
```bash
# 1. Hotfix desde main
git checkout main
git checkout -b hotfix/critical-bug

# 2. Fix y commit
git add .
git commit -m "hotfix: critical bug fix"

# 3. Merge a main (producción)
git checkout main
git merge hotfix/critical-bug
git push origin main
# → Auto-deploy a production

# 4. Backport a develop y preprod
git checkout develop
git merge main
git push origin develop

git checkout preprod
git merge main
git push origin preprod
```

---

## 📋 **Resumen de Configuración**

### **✅ ¿Qué está configurado?**
- ✅ **Jenkins Pipeline** con detección automática de branches
- ✅ **Git Polling** cada 5 minutos en todas las ramas
- ✅ **Build automático** con tags específicos por ambiente
- ✅ **Deploy automático** a ambiente correspondiente
- ✅ **Health checks** específicos por ambiente
- ✅ **Manual deploy** con soporte multi-ambiente
- ✅ **Rollback** independiente por ambiente
- ✅ **Registry local** con tags por ambiente

### **🎯 Próximos Pasos**
1. **Crear branches**: `preprod` y `develop` si no existen
2. **Configurar archivos compose**: Se auto-generan en el primer deploy
3. **Testear workflow**: Hacer commits a cada branch
4. **Monitorear**: Usar health checks y Consul UI

---

## 🚀 **Start Quick**

### **Crear Branches**
```bash
# Crear branch develop
git checkout -b develop
git push origin develop

# Crear branch preprod  
git checkout main
git checkout -b preprod
git push origin preprod
```

### **Primer Deploy**
```bash
# Deploy a desarrollo
git checkout develop
echo "# Development test" >> README.md
git add README.md
git commit -m "test: development deploy"
git push origin develop
# Esperar 5 minutos → auto-deploy a development

# Deploy a preprod
git checkout preprod
git merge develop
git push origin preprod
# Esperar 5 minutos → auto-deploy a preprod

# Deploy a producción
git checkout main
git merge preprod
git push origin main
# Esperar 5 minutos → auto-deploy a production
```

### **Verificar Deploys**
```bash
# Verificar todos los ambientes
curl http://localhost:8080/actuator/health  # Production
curl http://localhost:8180/actuator/health  # Preprod  
curl http://localhost:8280/actuator/health  # Development
```

---

## 🎉 **¡Tu CI/CD Multi-Ambiente está Listo!**

**Características:**
- ✅ **3 ambientes** completamente separados
- ✅ **Deploy automático** basado en branches
- ✅ **Zero-downtime** en todos los ambientes
- ✅ **Rollback independiente** por ambiente
- ✅ **Health monitoring** específico por ambiente
- ✅ **Registry local** con versionado por ambiente

**¡Solo haz commits y en máximo 5 minutos se desplegará automáticamente al ambiente correspondiente!** 🚀 