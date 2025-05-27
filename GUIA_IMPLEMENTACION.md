# Guía de Implementación - Arquitectura de Microservicios

## 🎯 Objetivo

Implementar un sistema escalable de gestión de usuarios y roles que sincronice automáticamente entre múltiples bases de datos usando patrones de microservicios y event-driven architecture.

## 📋 Prerrequisitos

- Java 17+
- Maven 3.6+
- Docker Desktop
- Git
- 8GB RAM mínimo
- Puertos disponibles: 8080-8090, 3000, 5432-5433, 3306, 27017, 9090, 16686

## 🚀 Instalación Paso a Paso

### Paso 1: Clonar e Inicializar

```bash
# Si no tienes el código, crea el directorio del proyecto
mkdir user-microservices-architecture
cd user-microservices-architecture

# Hacer ejecutables los scripts
chmod +x scripts/*.sh
```

### Paso 2: Iniciar Infraestructura

```bash
# Iniciar todos los servicios de infraestructura
./scripts/start-infrastructure.sh
```

Este script iniciará:
- 🗃️ Kafka + Zookeeper
- 🐘 PostgreSQL (Users DB)
- 🐘 PostgreSQL (Sync DB A)
- 🐬 MySQL (Sync DB B)  
- 🍃 MongoDB (Sync DB C)
- 📊 Grafana + Prometheus
- 🔍 Jaeger
- 🎛️ Kafka UI

### Paso 3: Compilar Microservicios

```bash
# Compilar el User Management Service
cd user-management-service
mvn clean install -DskipTests

# Compilar el Database Sync Service A
cd ../database-sync-service-a
mvn clean install -DskipTests

# Volver al directorio raíz
cd ..
```

### Paso 4: Configurar Bases de Datos

Crear las tablas necesarias:

```sql
-- Para PostgreSQL Users DB (puerto 5432)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id),
    role_id UUID REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Para PostgreSQL Sync DB A (puerto 5433)
CREATE TABLE sync_users (
    id BIGSERIAL PRIMARY KEY,
    original_user_id UUID UNIQUE NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_status CHAR(1) NOT NULL,
    user_roles TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_event_version BIGINT NOT NULL
);
```

### Paso 5: Iniciar Microservicios

```bash
# Terminal 1: User Management Service
cd user-management-service
mvn spring-boot:run

# Terminal 2: Database Sync Service A
cd database-sync-service-a
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

## 🧪 Pruebas de la Arquitectura

### 1. Verificar Estado de Servicios

```bash
# Health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

### 2. Crear Usuario (Trigger de Sincronización)

```bash
# Crear un usuario
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan.perez@example.com",
    "name": "Juan Pérez",
    "password": "securepassword123",
    "roles": [
      {
        "name": "USER",
        "description": "Usuario básico"
      }
    ]
  }'
```

### 3. Verificar Sincronización

```bash
# Verificar en base principal
psql -h localhost -p 5432 -U users_user -d users_db \
  -c "SELECT * FROM users;"

# Verificar en base sincronizada A
psql -h localhost -p 5433 -U sync_user_a -d sync_db_a \
  -c "SELECT * FROM sync_users;"
```

### 4. Actualizar Usuario

```bash
# Obtener ID del usuario creado y actualizar
curl -X PUT http://localhost:8081/api/users/{user-id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Carlos Pérez",
    "status": "INACTIVE"
  }'
```

### 5. Monitorear Eventos en Kafka

Ir a Kafka UI: http://localhost:8090
- Ver tópico `user-events`
- Verificar que los eventos se publican correctamente

## 📊 Monitoreo

### Grafana (http://localhost:3000)
- Usuario: admin / admin
- Importar dashboards para métricas de microservicios
- Monitorear throughput, latencia, errores

### Prometheus (http://localhost:9090)
- Métricas de aplicación
- Métricas de JVM
- Métricas de Kafka

### Jaeger (http://localhost:16686)
- Tracing distribuido
- Análisis de latencia end-to-end

## 🔧 Configuraciones Importantes

### application.yml (User Management Service)

```yaml
spring:
  application:
    name: user-management-service
  datasource:
    url: jdbc:postgresql://localhost:5432/users_db
    username: users_user
    password: users_pass
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: 8081

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### application.yml (Database Sync Service A)

```yaml
spring:
  application:
    name: database-sync-service-a
  datasource:
    url: jdbc:postgresql://localhost:5433/sync_db_a
    username: sync_user_a
    password: sync_pass_a
  jpa:
    hibernate:
      ddl-auto: validate
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: database-sync-service-a
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

server:
  port: 8082
```

## 🔄 Escalabilidad - Agregar Nueva Base de Datos

### 1. Crear Nuevo Microservicio

```bash
# Copiar estructura del Database Sync Service A
cp -r database-sync-service-a database-sync-service-d
cd database-sync-service-d

# Actualizar configuraciones:
# - Cambiar puerto a 8085
# - Cambiar group-id de Kafka
# - Configurar nueva base de datos
```

### 2. Implementar Transformaciones Específicas

```java
@Service
public class UserSyncServiceD {
    
    private String mapStatusForSystemD(String originalStatus) {
        // Mapeo específico para el sistema D
        switch (originalStatus) {
            case "ACTIVE": return "ACTIVO";
            case "INACTIVE": return "INACTIVO";  
            case "DISABLED": return "DESHABILITADO";
            default: return "DESCONOCIDO";
        }
    }
    
    private void transformToSystemDFormat(UserEvent event) {
        // Lógica específica de transformación
    }
}
```

### 3. Agregar a Docker Compose

```yaml
# Agregar nueva base de datos al docker-compose.yml
postgres-sync-d:
  image: postgres:15-alpine
  ports:
    - "5434:5432"
  environment:
    POSTGRES_DB: sync_db_d
    POSTGRES_USER: sync_user_d
    POSTGRES_PASSWORD: sync_pass_d
```

## 🚨 Manejo de Errores y Recuperación

### Dead Letter Queue
- Eventos que fallan se envían a `user-sync-errors`
- Implementar procesamiento manual para casos complejos

### Circuit Breaker
- Protección contra fallos en cascada
- Configurado en cada Database Sync Service

### Retry Logic
- Reintentos automáticos con backoff exponencial
- Máximo 3 intentos por evento

## 🔒 Consideraciones de Seguridad

1. **Autenticación JWT** en API Gateway
2. **Cifrado de comunicación** entre servicios
3. **Validación de entrada** en todos los endpoints
4. **Auditoría completa** de cambios de usuario
5. **Secrets management** con Vault

## 📈 Optimizaciones de Performance

1. **Connection pooling** en bases de datos
2. **Batch processing** para actualizaciones masivas
3. **Caching** con Redis para consultas frecuentes
4. **Particionamiento** de tópicos Kafka por región
5. **Índices optimizados** en tablas de sincronización

## 🔄 Próximos Pasos

1. Implementar API Gateway con Spring Cloud Gateway
2. Agregar segundo y tercer Database Sync Service
3. Implementar autenticación y autorización
4. Configurar CI/CD pipeline
5. Desplegar en Kubernetes
6. Implementar disaster recovery 