# API Gateway - MediHelp360

## 🎯 Descripción

El **API Gateway** es el punto de entrada único para todos los microservicios del ecosistema MediHelp360. Proporciona routing inteligente, circuit breakers, load balancing y monitoreo centralizado.

## 🏗️ Arquitectura

```
Cliente → API Gateway (8080) → Microservicios
                ├── User Management Service (8081)
                ├── Database Sync Service A (8082) - PostgreSQL
                ├── Database Sync Service B (8083) - MySQL
                └── Database Sync Service C (8084) - MongoDB
```

## 🚀 Características

- **Routing Inteligente**: Enrutamiento basado en paths hacia microservicios específicos
- **Circuit Breaker**: Protección contra fallos en cascada con Resilience4j
- **Load Balancing**: Distribución de carga entre instancias de servicios
- **CORS**: Configuración global para aplicaciones web
- **Monitoreo**: Métricas con Prometheus y health checks
- **Fallback**: Respuestas elegantes cuando los servicios no están disponibles

## 📋 Prerrequisitos

- Java 17+
- Maven 3.6+
- Microservicios ejecutándose en sus puertos respectivos

## 🔧 Configuración

### Puertos de Servicios
- **API Gateway**: 8080
- **User Management**: 8081
- **Sync Service A**: 8082
- **Sync Service B**: 8083
- **Sync Service C**: 8084

### Variables de Entorno
```bash
# Opcional: Configurar URLs de servicios
export USER_MANAGEMENT_URL=http://localhost:8081
export SYNC_SERVICE_A_URL=http://localhost:8082
export SYNC_SERVICE_B_URL=http://localhost:8083
export SYNC_SERVICE_C_URL=http://localhost:8084
```

## 🚀 Ejecución

### Compilar y Ejecutar
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar el API Gateway
mvn spring-boot:run
```

### Verificar Estado
```bash
# Health check del API Gateway
curl http://localhost:8080/actuator/health

# Ver rutas configuradas
curl http://localhost:8080/actuator/gateway/routes
```

## 🛣️ Rutas Disponibles

### User Management Service
```bash
# Crear usuario
POST http://localhost:8080/api/users

# Obtener usuarios
GET http://localhost:8080/api/users

# Obtener usuario por ID
GET http://localhost:8080/api/users/{id}

# Actualizar usuario
PUT http://localhost:8080/api/users/{id}

# Eliminar usuario
DELETE http://localhost:8080/api/users/{id}
```

### Database Sync Services
```bash
# Sync Service A (PostgreSQL)
GET http://localhost:8080/api/sync-a/users
GET http://localhost:8080/api/sync-a/users/stats

# Sync Service B (MySQL)
GET http://localhost:8080/api/sync-b/users
GET http://localhost:8080/api/sync-b/users/stats

# Sync Service C (MongoDB)
GET http://localhost:8080/api/sync-c/users
GET http://localhost:8080/api/sync-c/users/stats
```

### Health Checks
```bash
# Health check de servicios individuales
GET http://localhost:8080/health/user-management/health
GET http://localhost:8080/health/sync-a/health
GET http://localhost:8080/health/sync-b/health
GET http://localhost:8080/health/sync-c/health
```

## 🔄 Circuit Breaker

### Configuración
- **Sliding Window**: 10 llamadas
- **Failure Rate**: 50%
- **Wait Duration**: 5 segundos
- **Half Open Calls**: 3

### Estados
- **CLOSED**: Funcionamiento normal
- **OPEN**: Servicio no disponible, respuestas de fallback
- **HALF_OPEN**: Probando si el servicio se recuperó

### Monitoreo
```bash
# Ver estado de circuit breakers
curl http://localhost:8080/actuator/health
```

## 📊 Monitoreo y Métricas

### Endpoints de Actuator
```bash
# Health check completo
GET http://localhost:8080/actuator/health

# Métricas de Prometheus
GET http://localhost:8080/actuator/prometheus

# Información de la aplicación
GET http://localhost:8080/actuator/info

# Rutas del gateway
GET http://localhost:8080/actuator/gateway/routes
```

### Métricas Disponibles
- Latencia de requests
- Throughput por servicio
- Estado de circuit breakers
- Errores por endpoint
- Tiempo de respuesta

## 🚨 Manejo de Errores

### Fallback Responses
Cuando un servicio no está disponible, el API Gateway responde con:

```json
{
  "error": "Service Temporarily Unavailable",
  "message": "User Management Service is currently experiencing issues. Please try again later.",
  "service": "user-management-service",
  "timestamp": "2025-05-26T19:00:00",
  "status": 503
}
```

### Códigos de Error
- **503**: Servicio temporalmente no disponible
- **504**: Timeout del gateway
- **500**: Error interno del gateway

## 🔒 Seguridad

### CORS
- Configurado para permitir todos los orígenes en desarrollo
- Headers permitidos: `*`
- Métodos: `GET, POST, PUT, DELETE, OPTIONS`

### Próximas Implementaciones
- Autenticación JWT
- Rate limiting
- API key validation
- Request/response logging

## 🧪 Pruebas

### Prueba de Routing
```bash
# Probar routing a User Management
curl -X GET http://localhost:8080/api/users

# Probar routing a Sync Service C
curl -X GET http://localhost:8080/api/sync-c/users
```

### Prueba de Circuit Breaker
```bash
# Detener un servicio y probar fallback
# El circuit breaker se activará después de varios fallos
curl -X GET http://localhost:8080/api/users
```

### Prueba de Health Checks
```bash
# Verificar health de todos los servicios
curl http://localhost:8080/health/user-management/health
curl http://localhost:8080/health/sync-c/health
```

## 📝 Logs

### Configuración de Logging
- **Level**: INFO para Spring Cloud Gateway
- **Level**: DEBUG para com.medihelp360.gateway
- **Pattern**: Timestamp, thread, level, logger, message

### Logs Importantes
- Activación de circuit breakers
- Routing de requests
- Errores de conectividad
- Métricas de performance

## 🔧 Troubleshooting

### Problemas Comunes

1. **Servicio no responde**
   ```bash
   # Verificar que el servicio esté ejecutándose
   curl http://localhost:8081/actuator/health
   ```

2. **Circuit breaker activado**
   ```bash
   # Verificar estado del circuit breaker
   curl http://localhost:8080/actuator/health
   ```

3. **Routing no funciona**
   ```bash
   # Verificar rutas configuradas
   curl http://localhost:8080/actuator/gateway/routes
   ```

## 🚀 Próximos Pasos

1. **Implementar autenticación JWT**
2. **Agregar rate limiting**
3. **Configurar service discovery**
4. **Implementar request tracing**
5. **Agregar caching**

## 📞 Soporte

Para soporte técnico o preguntas sobre el API Gateway:
- Email: support@medihelp360.com
- Documentación: [MediHelp360 Docs](https://docs.medihelp360.com) 