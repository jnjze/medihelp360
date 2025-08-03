# 🚀 MediHelp360 CI/CD Setup Guide

## 📋 Resumen

Este documento describe la configuración de un sistema CI/CD completo con Jenkins para el proyecto MediHelp360. El sistema incluye:

- **Jenkins** para automatización CI/CD
- **Docker Registry local** para almacenar imágenes
- **Pipeline automatizado** que se ejecuta en cada commit
- **Deployment automático** al servidor de producción
- **Health checks** y validaciones

## 🏗️ Arquitectura CI/CD

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Git Repo  │───▶│   Jenkins   │───▶│   Registry  │───▶│ Production  │
│  (GitHub)   │    │   Server    │    │  (Local)    │    │   Server    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
      │                    │                    │                    │
      │                    │                    │                    │
   Webhook              Pipeline            Images              Services
   Trigger             Execution           Storage             Update
```

## 🎯 Flujo de CI/CD

1. **Trigger**: Push a `main` o `develop`
2. **Build**: Construir imágenes Docker de todos los servicios
3. **Test**: Ejecutar tests unitarios e integración
4. **Push**: Subir imágenes al registry local
5. **Deploy**: Actualizar servicios en producción
6. **Verify**: Health checks y validaciones

## 📦 Componentes del Sistema

### Jenkins (Puerto 8090)
- **Imagen**: `jenkins/jenkins:2.426.2-lts`
- **Función**: Orquestación CI/CD
- **Volúmenes**: Jenkins home, Docker socket
- **Plugins**: Docker Pipeline, Blue Ocean, GitHub Integration

### Docker Registry (Puerto 5000)
- **Imagen**: `registry:2.8`
- **Función**: Almacén privado de imágenes Docker
- **UI**: Puerto 8091 para gestión visual
- **Configuración**: Almacenamiento local, CORS habilitado

### SonarQube (Puerto 9000) - Opcional
- **Imagen**: `sonarqube:10.3-community`
- **Función**: Análisis de calidad de código
- **Base de datos**: PostgreSQL dedicada

## 🚀 Instalación y Configuración

### Paso 1: Configurar Docker Registry

```bash
# Configurar Docker para registry inseguro
./scripts/configure-docker-registry.sh
```

**En macOS**, agregar a Docker Desktop Settings → Docker Engine:
```json
{
  "insecure-registries": [
    "localhost:5001",
    "127.0.0.1:5000"
  ]
}
```

### Paso 2: Iniciar Jenkins

```bash
# Ejecutar el script de configuración
./scripts/setup-jenkins.sh
```

Esto iniciará:
- ✅ Jenkins en http://localhost:8090
- ✅ Docker Registry en http://localhost:5001
- ✅ Registry UI en http://localhost:8091

### Paso 3: Configurar Jenkins

1. **Abrir Jenkins**: http://localhost:8090
2. **Desbloquear**: Usar la contraseña mostrada en el script
3. **Instalar plugins**:
   - Suggested plugins
   - Docker Pipeline
   - Pipeline: Stage View
   - Blue Ocean
   - GitHub Integration
   - Slack Notification (opcional)

### Paso 4: Crear Pipeline Job

1. **Nuevo Item** → **Pipeline**
2. **Nombre**: `medihelp360-pipeline`
3. **Pipeline**:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/tu-usuario/medihelp360.git`
   - Script Path: `Jenkinsfile`

### Paso 5: Configurar Webhook

En tu repositorio GitHub:

1. **Settings** → **Webhooks** → **Add webhook**
2. **Payload URL**: `http://tu-servidor:8090/github-webhook/`
3. **Content type**: `application/json`
4. **Events**: `Just the push event`

## 🔧 Configuración Avanzada

### Variables de Entorno Jenkins

```bash
# En Jenkins → Manage Jenkins → System Configuration
DOCKER_REGISTRY=localhost:5001
COMPOSE_PROJECT_NAME=medihelp360
SLACK_WEBHOOK_URL=https://hooks.slack.com/... (opcional)
```

### Credenciales Jenkins

1. **Docker Registry**: Configurar si usas autenticación
2. **GitHub**: Token para acceso a repositorio privado
3. **Slack**: Token para notificaciones

### Configuración de Seguridad

```bash
# Agregar Jenkins user al grupo docker (Linux)
sudo usermod -aG docker jenkins

# Configurar firewall para Jenkins
sudo ufw allow 8090/tcp
sudo ufw allow 5000/tcp
```

## 📊 Pipeline Stages

### 1. Checkout
```groovy
checkout scm
```

### 2. Build Images (Paralelo)
```groovy
parallel {
    stage('Build API Gateway') { ... }
    stage('Build User Management') { ... }
    stage('Build Database Sync A/B/C') { ... }
}
```

### 3. Run Tests (Paralelo)
```groovy
parallel {
    stage('Test User Management') { ... }
    stage('Test Database Sync A') { ... }
    stage('Health Check Services') { ... }
}
```

### 4. Push to Registry
```groovy
sh "docker push ${DOCKER_REGISTRY}/image:${BUILD_TAG}"
```

### 5. Deploy (main branch)
```groovy
sh "./scripts/deploy.sh production"
sh "./scripts/wait-for-health.sh"
```

## 🧪 Testing y Validación

### Health Checks Automáticos
```bash
# Ejecutar health checks manualmente
./scripts/health-check.sh

# Esperar por servicios healthy
./scripts/wait-for-health.sh
```

### Rollback Strategy
```bash
# En caso de fallo, rollback a versión anterior
docker-compose -f docker-compose.secure.yml down
docker-compose -f docker-compose.secure.yml up -d
```

## 📱 Monitoreo y Alertas

### Registry UI
- **URL**: http://localhost:8091
- **Función**: Ver imágenes, tags, eliminar versiones antiguas

### Jenkins Blue Ocean
- **URL**: http://localhost:8090/blue
- **Función**: Vista visual del pipeline, logs detallados

### SonarQube (Opcional)
- **URL**: http://localhost:9000
- **Función**: Análisis de calidad de código
- **Integración**: Automática en pipeline

## 🔒 Consideraciones de Seguridad

### Registry Security
```yaml
# Para producción, habilitar autenticación
auth:
  htpasswd:
    realm: basic-realm
    path: /etc/docker/registry/htpasswd
```

### Jenkins Security
1. **Habilitar CSRF protection**
2. **Configurar Role-based Authorization**
3. **Usar credenciales seguras**
4. **Configurar SSL/TLS**

### Network Security
```bash
# Aislar Jenkins en subnet privada
networks:
  jenkins-network:
    driver: bridge
    internal: true  # Solo tráfico interno
```

## 🚨 Troubleshooting

### Problema: Jenkins no puede conectar a Docker
```bash
# Verificar permisos
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Verificar socket
ls -la /var/run/docker.sock
```

### Problema: Registry push failed
```bash
# Verificar configuración insecure registry
docker info | grep -i insecure

# Reiniciar Docker
sudo systemctl restart docker
```

### Problema: Services no healthy después de deploy
```bash
# Verificar logs
docker-compose -f docker-compose.secure.yml logs api-gateway

# Ejecutar health check manual
./scripts/health-check.sh
```

## 📚 Scripts Disponibles

| Script | Función |
|--------|---------|
| `setup-jenkins.sh` | Configurar Jenkins y Registry |
| `configure-docker-registry.sh` | Configurar Docker para registry local |
| `deploy.sh` | Deployment a producción/staging |
| `health-check.sh` | Verificar salud de servicios |
| `wait-for-health.sh` | Esperar por servicios healthy |

## 🎯 Próximos Pasos

1. **SSL/TLS**: Configurar certificados para Jenkins y Registry
2. **Backup**: Implementar backup automático de Jenkins y Registry
3. **Monitoring**: Integrar con Prometheus/Grafana
4. **Multi-environment**: Configurar staging/production separados
5. **Auto-scaling**: Implementar escalado automático

## 📞 Soporte

Para problemas o dudas:
1. Revisar logs: `docker-compose -f docker-compose.jenkins.yml logs`
2. Verificar health checks: `./scripts/health-check.sh`
3. Consultar documentación oficial de Jenkins/Docker

---

## 🎉 ¡Listo!

Tu sistema CI/CD está configurado. Cada push a `main` o `develop` ejecutará automáticamente:

✅ Build de todas las imágenes  
✅ Tests unitarios e integración  
✅ Push a registry local  
✅ Deployment a producción  
✅ Health checks y validación  

**¡El deployment automático está funcionando!** 🚀 