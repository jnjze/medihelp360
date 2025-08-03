# 🚀 Guía de Inicio en Servidor - MediHelp360

## 📋 **Checklist de Inicio Completo**

### **Fase 1: Preparación del Servidor** ✅

#### **1.1 Verificar Requisitos del Sistema**
```bash
# Verificar Docker
docker --version
docker-compose --version

# Verificar Java (para Maven builds)
java -version
mvn --version

# Verificar Git
git --version

# Verificar puertos disponibles
sudo netstat -tulpn | grep -E ':(8080|8081|8082|8083|8084|8180|8181|8182|8183|8184|8280|8281|8282|8283|8284|8500|2181|9092|5432|3306|27017)'
```

#### **1.2 Clonar el Repositorio**
```bash
# Clonar en el servidor
git clone https://github.com/TU_USUARIO/medihelp360.git
cd medihelp360

# Verificar que todos los archivos están presentes
ls -la
ls -la scripts/
```

---

### **Fase 2: Configurar CI/CD** 🔧

#### **2.1 Configurar Docker Registry Local**
```bash
# Configurar registry para servidor local
./scripts/configure-docker-registry.sh

# Verificar registry
curl http://localhost:5001/v2/
```

#### **2.2 Iniciar Jenkins y CI/CD**
```bash
# Iniciar infraestructura Jenkins
docker-compose -f docker-compose.jenkins.yml up -d

# Esperar que Jenkins inicie
./scripts/wait-for-health.sh

# Configurar Jenkins
./scripts/setup-jenkins.sh

# Obtener password inicial de Jenkins
docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword
```

#### **2.3 Acceder a Jenkins**
```bash
# Abrir en navegador:
echo "🌐 Jenkins: http://localhost:8090"
echo "📋 Registry UI: http://localhost:5001"
echo "📊 SonarQube: http://localhost:9000"
```

---

### **Fase 3: Crear Ramas de Trabajo** 🌿

#### **3.1 Configurar Estructura de Ramas**
```bash
# Crear branch develop
git checkout -b develop
git push origin develop

# Crear branch preprod
git checkout main
git checkout -b preprod
git push origin preprod

# Volver a main
git checkout main
```

#### **3.2 Verificar Polling en Jenkins**
```bash
# El Jenkins ya está configurado para polling cada 5 minutos
# Verificar configuración:
curl http://localhost:8090/job/medihelp360-pipeline/config.xml | grep pollSCM
```

---

### **Fase 4: Primer Deployment** 🚀

#### **4.1 Build Inicial de Imágenes**
```bash
# Construir todas las imágenes iniciales
echo "🏗️ Building initial images..."

# API Gateway
cd api-gateway
docker build -t localhost:5001/medihelp360-api-gateway:latest .
cd ..

# User Management Service
cd user-management-service
docker build -t localhost:5001/medihelp360-user-management-service:latest .
cd ..

# Database Sync Service A
cd database-sync-service-a
docker build -t localhost:5001/medihelp360-database-sync-service-a:latest .
cd ..

# Database Sync Service B
cd database-sync-service-b
docker build -t localhost:5001/medihelp360-database-sync-service-b:latest .
cd ..

# Database Sync Service C
cd database-sync-service-c
docker build -t localhost:5001/medihelp360-database-sync-service-c:latest .
cd ..

echo "✅ Initial images built"
```

#### **4.2 Push Imágenes al Registry**
```bash
# Push al registry local
docker push localhost:5001/medihelp360-api-gateway:latest
docker push localhost:5001/medihelp360-user-management-service:latest
docker push localhost:5001/medihelp360-database-sync-service-a:latest
docker push localhost:5001/medihelp360-database-sync-service-b:latest
docker push localhost:5001/medihelp360-database-sync-service-c:latest

echo "✅ Images pushed to registry"
```

#### **4.3 Deploy Ambiente de Producción**
```bash
# Iniciar infraestructura (bases de datos, Kafka, Consul)
echo "🗄️ Starting infrastructure..."
docker-compose -f docker-compose.secure.yml up -d zookeeper kafka consul-server postgres-sync-a mysql-sync-b mongo-sync-c

# Esperar que la infraestructura esté lista
sleep 30

# Deploy de aplicaciones
echo "🚀 Starting applications..."
./scripts/deploy.sh production

# Verificar deployment
./scripts/health-check.sh
```

---

### **Fase 5: Verificación y Tests** 🧪

#### **5.1 Verificar URLs de Producción**
```bash
# Health checks de servicios
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Management
curl http://localhost:8082/actuator/health  # Database Sync A
curl http://localhost:8083/actuator/health  # Database Sync B
curl http://localhost:8084/actuator/health  # Database Sync C

# Consul UI
echo "🗂️ Consul: http://localhost:8500"
```

#### **5.2 Test de Funcionalidad**
```bash
# Crear un usuario de prueba
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test-user",
    "email": "test@medihelp360.com",
    "firstName": "Test",
    "lastName": "User"
  }'

# Verificar usuario creado
curl http://localhost:8080/api/users

# Verificar logs de Kafka (eventos de usuario)
docker logs database-sync-service-a | grep "User event received"
```

---

### **Fase 6: Configurar Ambientes Adicionales** 🏗️

#### **6.1 Deploy Ambiente de Desarrollo**
```bash
# Deploy a development (puertos 8280-8284)
./scripts/deploy.sh development

# Verificar development
curl http://localhost:8280/actuator/health
```

#### **6.2 Deploy Ambiente Pre-Producción**
```bash
# Deploy a preprod (puertos 8180-8184)
./scripts/deploy.sh preprod

# Verificar preprod
curl http://localhost:8180/actuator/health
```

---

### **Fase 7: Test del CI/CD Completo** 🔄

#### **7.1 Test Development Workflow**
```bash
# Hacer cambio en development
git checkout develop
echo "# Development test $(date)" >> README.md
git add README.md
git commit -m "test: development auto-deploy"
git push origin develop

echo "⏰ Esperando 5 minutos para auto-deploy..."
# Jenkins detectará y desplegará automáticamente
```

#### **7.2 Test Pre-Production Workflow**
```bash
# Promote a preprod
git checkout preprod
git merge develop
git push origin preprod

echo "⏰ Esperando 5 minutos para auto-deploy a preprod..."
```

#### **7.3 Test Production Workflow**
```bash
# Promote a production
git checkout main
git merge preprod
git push origin main

echo "⏰ Esperando 5 minutos para auto-deploy a production..."
```

---

## 🎯 **Comando de Inicio Rápido**

### **Script de Inicio Automático**
```bash
#!/bin/bash
# Quick Start Script

echo "🚀 MediHelp360 Quick Start"
echo "=========================="

# 1. Verificar requisitos
echo "1️⃣ Checking requirements..."
docker --version || { echo "❌ Docker not found"; exit 1; }
docker-compose --version || { echo "❌ Docker Compose not found"; exit 1; }

# 2. Configurar registry
echo "2️⃣ Configuring Docker registry..."
./scripts/configure-docker-registry.sh

# 3. Iniciar Jenkins
echo "3️⃣ Starting Jenkins..."
docker-compose -f docker-compose.jenkins.yml up -d

# 4. Construir imágenes
echo "4️⃣ Building images..."
./scripts/manual-deploy.sh . main production force

# 5. Crear ramas
echo "5️⃣ Creating branches..."
git checkout -b develop 2>/dev/null || git checkout develop
git push origin develop 2>/dev/null || true

git checkout main
git checkout -b preprod 2>/dev/null || git checkout preprod  
git push origin preprod 2>/dev/null || true

git checkout main

# 6. Verificar deployment
echo "6️⃣ Verifying deployment..."
sleep 30
./scripts/health-check.sh

echo ""
echo "🎉 MediHelp360 Quick Start Completed!"
echo ""
echo "📊 Access URLs:"
echo "   🏭 Production API: http://localhost:8080"
echo "   🧪 Preprod API:    http://localhost:8180"
echo "   💻 Development API: http://localhost:8280"
echo "   🔧 Jenkins:        http://localhost:8090"
echo "   🗂️ Consul:         http://localhost:8500"
echo ""
echo "📋 Next Steps:"
echo "   1. Configure Jenkins job (see setup-jenkins.sh output)"
echo "   2. Make commits to test auto-deployment"
echo "   3. Monitor health with: ./scripts/health-check.sh"
```

---

## 📚 **Comandos de Monitoreo**

### **Ver Estado General**
```bash
# Estado de todos los contenedores
docker ps -a

# Estado por ambiente
./scripts/manual-deploy.sh status production
./scripts/manual-deploy.sh status preprod
./scripts/manual-deploy.sh status development

# Health check completo
./scripts/health-check.sh

# Logs de servicios
docker logs api-gateway
docker logs user-management-service
docker logs database-sync-service-a
```

### **Solución de Problemas**
```bash
# Reiniciar un ambiente específico
./scripts/deploy.sh production

# Rollback si algo falla
./scripts/manual-deploy.sh rollback production

# Ver logs de Jenkins
docker logs jenkins-medihelp360

# Limpiar imágenes viejas
docker system prune -f
```

---

## 🎉 **¡Tu Sistema Está Listo!**

### **URLs de Acceso**
- 🏭 **Production**: http://localhost:8080
- 🧪 **Pre-Production**: http://localhost:8180  
- 💻 **Development**: http://localhost:8280
- 🔧 **Jenkins**: http://localhost:8090
- 🗂️ **Consul**: http://localhost:8500
- 📦 **Registry UI**: http://localhost:5001

### **Workflow Automático**
1. **Haz commit** a cualquier rama (`main`, `preprod`, `develop`)
2. **Espera 5 minutos** - Jenkins detecta cambios automáticamente
3. **Verifica deployment** - Health checks automáticos
4. **Monitorea** - Consul UI y logs disponibles

**¡Tu CI/CD multi-ambiente está funcionando completamente!** 🚀 