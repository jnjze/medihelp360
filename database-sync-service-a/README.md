# Database Sync Service A

Este servicio se encarga de sincronizar datos de usuarios desde el User Management Service hacia una base de datos PostgreSQL específica. Consume eventos de Kafka y mantiene una réplica de los datos de usuarios con un esquema optimizado para consultas específicas.

## 🎯 Propósito

- **Sincronización de datos**: Mantiene una copia sincronizada de los datos de usuarios
- **Esquema optimizado**: Utiliza un esquema de base de datos específico para este servicio
- **Procesamiento de eventos**: Consume eventos de Kafka del User Management Service
- **Tolerancia a fallos**: Implementa retry y manejo de errores robusto

## 🏗️ Arquitectura

```
User Management Service → Kafka (user-events) → Database Sync Service A → PostgreSQL (sync_db_a)
```

## 📋 Características

- ✅ Consumo de eventos de Kafka
- ✅ Sincronización automática de usuarios
- ✅ Manejo de eventos desordenados
- ✅ Retry automático en caso de errores
- ✅ Métricas y monitoreo
- ✅ API REST para consultas
- ✅ Esquema de base de datos optimizado

## 🚀 Configuración

### Prerrequisitos

- Java 17+
- Maven 3.6+
- PostgreSQL (puerto 5433)
- Kafka (puerto 9092)

### Variables de Entorno

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/sync_db_a
SPRING_DATASOURCE_USERNAME=sync_user_a
SPRING_DATASOURCE_PASSWORD=sync_pass_a

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_CONSUMER_GROUP_ID=database-sync-service-a

# Servidor
SERVER_PORT=8082
```

### Configuración de Base de Datos

La base de datos se configura automáticamente usando Docker Compose:

```yaml
postgres-sync-a:
  image: postgres:15-alpine
  container_name: postgres-sync-a
  ports:
    - "5433:5432"
  environment:
    POSTGRES_DB: sync_db_a
    POSTGRES_USER: sync_user_a
    POSTGRES_PASSWORD: sync_pass_a
```

## 🗄️ Esquema de Base de Datos

### Tabla: sync_users

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | ID único del registro sincronizado |
| original_user_id | UUID | ID del usuario en el servicio principal |
| user_email | VARCHAR(255) | Email del usuario |
| user_name | VARCHAR(255) | Nombre del usuario |
| user_status | CHAR(1) | Estado: A=Active, I=Inactive, D=Disabled, P=Pending, U=Unknown |
| user_roles | TEXT | Roles del usuario (separados por comas) |
| is_active | BOOLEAN | Indica si el usuario está activo |
| synced_at | TIMESTAMP | Fecha/hora de sincronización |
| last_event_version | BIGINT | Versión del último evento procesado |
| created_at | TIMESTAMP | Fecha de creación del registro |
| updated_at | TIMESTAMP | Fecha de última actualización |

## 🔄 Eventos Procesados

### UserCreatedEvent
```json
{
  "eventType": "UserCreatedEvent",
  "userId": "uuid",
  "email": "user@example.com",
  "name": "User Name",
  "status": "ACTIVE",
  "roles": ["USER", "ADMIN"]
}
```

### UserUpdatedEvent
```json
{
  "eventType": "UserUpdatedEvent",
  "userId": "uuid",
  "email": "user@example.com",
  "name": "Updated Name",
  "status": "INACTIVE",
  "roles": ["USER"],
  "previousStatus": "ACTIVE"
}
```

## 🌐 API Endpoints

### Obtener usuarios sincronizados
```bash
GET /api/sync/users?page=0&size=20
```

### Obtener usuario por ID original
```bash
GET /api/sync/users/{originalUserId}
```

### Obtener usuarios por estado
```bash
GET /api/sync/users/status/{status}?page=0&size=20
```

### Obtener estadísticas
```bash
GET /api/sync/stats
```

### Health Check
```bash
GET /api/sync/health
```

## 🚀 Ejecución

### Desarrollo Local

1. **Iniciar dependencias**:
```bash
docker-compose up -d postgres-sync-a kafka zookeeper
```

2. **Ejecutar migraciones**:
```bash
mvn flyway:migrate
```

3. **Iniciar aplicación**:
```bash
mvn spring-boot:run
```

### Verificar funcionamiento

1. **Health check**:
```bash
curl http://localhost:8082/api/sync/health
```

2. **Estadísticas**:
```bash
curl http://localhost:8082/api/sync/stats
```

## 📊 Monitoreo

### Métricas disponibles
- `/actuator/health` - Estado del servicio
- `/actuator/metrics` - Métricas de la aplicación
- `/actuator/prometheus` - Métricas para Prometheus

### Logs importantes
- Eventos de Kafka procesados
- Errores de sincronización
- Estadísticas de rendimiento

## 🔧 Configuración Avanzada

### Retry Policy
```yaml
spring.retry:
  enabled: true

# En UserEventListener
@Retryable(
  value = {Exception.class},
  maxAttempts = 3,
  backoff = @Backoff(delay = 1000, multiplier = 2)
)
```

### Kafka Consumer
```yaml
spring.kafka.consumer:
  group-id: database-sync-service-a
  auto-offset-reset: earliest
  enable-auto-commit: false
  
spring.kafka.listener:
  ack-mode: manual_immediate
```

## 🐛 Troubleshooting

### Problemas comunes

1. **Error de conexión a PostgreSQL**:
   - Verificar que el contenedor esté ejecutándose: `docker ps`
   - Verificar conectividad: `docker exec postgres-sync-a pg_isready`

2. **Error de conexión a Kafka**:
   - Verificar que Kafka esté ejecutándose
   - Verificar el tópico: `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`

3. **Eventos no se procesan**:
   - Verificar logs del consumer
   - Verificar offset del consumer group
   - Verificar formato de los eventos

### Comandos útiles

```bash
# Ver logs del servicio
docker logs database-sync-service-a

# Verificar base de datos
docker exec postgres-sync-a psql -U sync_user_a -d sync_db_a -c "SELECT COUNT(*) FROM sync_users;"

# Verificar tópicos de Kafka
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Ver mensajes en el tópico
docker exec kafka kafka-console-consumer --topic user-events --bootstrap-server localhost:9092 --from-beginning
```

## 🔄 Escalabilidad

Este servicio puede escalarse horizontalmente:
- Múltiples instancias pueden consumir del mismo tópico
- Kafka maneja automáticamente la distribución de particiones
- Cada instancia mantiene su propia base de datos

## 📝 Notas de Desarrollo

- Los eventos se procesan de forma idempotente
- Se maneja la llegada desordenada de eventos
- Se implementa retry automático para errores transitorios
- Se mantiene compatibilidad con cambios en el esquema de eventos 