# Database Sync Service C

Servicio de sincronización de base de datos que consume eventos de usuarios desde Kafka y los almacena en MongoDB para consultas optimizadas.

## Descripción

Este servicio es parte del ecosistema MediHelp360 y se encarga de:

- Consumir eventos de usuarios desde Kafka (topic: `user-events`)
- Procesar eventos de creación, actualización y eliminación de usuarios
- Almacenar los datos sincronizados en MongoDB
- Proporcionar APIs REST para consultar los datos sincronizados
- Mantener un historial de sincronización y estado de los datos

## Arquitectura

```
Kafka (user-events) → Service C → MongoDB
                                     ↓
                              REST API Endpoints
```

## Tecnologías

- **Spring Boot 3.2.0**
- **Spring Data MongoDB**
- **Spring Kafka**
- **MongoDB** - Base de datos NoSQL para almacenamiento
- **Apache Kafka** - Sistema de mensajería
- **Lombok** - Reducción de código boilerplate
- **Jackson** - Serialización/deserialización JSON

## Configuración

### Prerrequisitos

1. **MongoDB** ejecutándose en `localhost:27017`
2. **Apache Kafka** ejecutándose en `localhost:9092`
3. **Java 17+**
4. **Maven 3.6+**

### Variables de Entorno

El servicio utiliza las siguientes configuraciones (definidas en `application-dev.yml`):

```yaml
server:
  port: 8084

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/medihelp360_sync
      database: medihelp360_sync

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: database-sync-service-c
```

## Instalación y Ejecución

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd database-sync-service-c
```

### 2. Instalar dependencias
```bash
mvn clean install
```

### 3. Ejecutar el servicio
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

El servicio estará disponible en: `http://localhost:8084`

## API Endpoints

### Usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/users` | Obtener todos los usuarios (paginado) |
| GET | `/users/original/{originalId}` | Buscar usuario por ID original |
| GET | `/users/userid/{userId}` | Buscar usuario por userId |
| GET | `/users/email/{email}` | Buscar usuario por email |
| GET | `/users/username/{username}` | Buscar usuario por username |
| GET | `/users/role/{role}` | Buscar usuarios por rol |
| GET | `/users/active` | Obtener usuarios activos |
| GET | `/users/department/{department}` | Buscar usuarios por departamento |
| GET | `/users/specialization/{specialization}` | Buscar usuarios por especialización |
| GET | `/users/search?query={text}` | Búsqueda de texto libre |
| GET | `/users/filter?role={role}&active={true/false}` | Filtrar usuarios |
| GET | `/users/stats` | Estadísticas de usuarios |
| GET | `/users/sync/after?dateTime={ISO_DATE}` | Usuarios sincronizados después de una fecha |
| GET | `/users/sync/status/{status}` | Usuarios por estado de sincronización |

### Ejemplos de Uso

#### Obtener todos los usuarios (paginado)
```bash
curl "http://localhost:8084/users?page=0&size=10&sortBy=syncUpdatedAt&sortDir=desc"
```

#### Buscar usuario por email
```bash
curl "http://localhost:8084/users/email/juan.perez@medihelp360.com"
```

#### Obtener estadísticas
```bash
curl "http://localhost:8084/users/stats"
```

#### Búsqueda de texto
```bash
curl "http://localhost:8084/users/search?query=Juan&page=0&size=10"
```

#### Filtrar usuarios activos por rol
```bash
curl "http://localhost:8084/users/filter?role=DOCTOR&active=true&page=0&size=10"
```

## Modelo de Datos

### UserDocument (MongoDB)

```json
{
  "_id": "ObjectId",
  "originalId": 123,
  "userId": "user-uuid",
  "email": "user@example.com",
  "username": "username",
  "firstName": "Juan",
  "lastName": "Pérez",
  "fullName": "Juan Pérez",
  "role": "DOCTOR",
  "active": true,
  "department": "Cardiología",
  "specialization": "Cardiología Intervencionista",
  "phoneNumber": "+1234567890",
  "version": 1,
  "source": "user-service",
  "syncStatus": "SYNCED",
  "syncCreatedAt": "2024-01-01T10:00:00",
  "syncUpdatedAt": "2024-01-01T10:00:00",
  "metadata": "{\"eventId\":\"event-123\"}"
}
```

### Estados de Sincronización

- `SYNCED` - Datos sincronizados correctamente
- `PENDING` - Sincronización pendiente
- `ERROR` - Error en la sincronización
- `DELETED` - Usuario marcado como eliminado

## Eventos Kafka

### Formato de Eventos

El servicio consume eventos del topic `user-events` con el siguiente formato:

```json
{
  "eventId": "event-123",
  "eventType": "UserCreatedEvent",
  "aggregateId": "user-123",
  "timestamp": "2024-01-01T10:00:00Z",
  "metadata": {
    "source": "user-service",
    "version": "1.0"
  },
  "userId": "user-uuid",
  "email": "user@example.com",
  "name": "Juan Pérez",
  "roles": ["DOCTOR"],
  "status": "ACTIVE"
}
```

### Tipos de Eventos Soportados

- `UserCreatedEvent` - Usuario creado
- `UserUpdatedEvent` - Usuario actualizado
- `UserDeletedEvent` - Usuario eliminado

## Monitoreo y Salud

### Health Check
```bash
curl http://localhost:8084/actuator/health
```

### Métricas
```bash
curl http://localhost:8084/actuator/metrics
```

### Prometheus
```bash
curl http://localhost:8084/actuator/prometheus
```

## Logs

Los logs se almacenan en:
- **Consola**: Formato simplificado para desarrollo
- **Archivo**: `logs/database-sync-service-c.log`

### Niveles de Log

- `com.example.sync`: DEBUG
- `org.springframework.kafka`: INFO
- `org.springframework.data.mongodb`: INFO

## Desarrollo

### Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/example/sync/
│   │   ├── DatabaseSyncServiceCApplication.java
│   │   ├── config/
│   │   │   ├── KafkaConfig.java
│   │   │   └── MongoConfig.java
│   │   ├── controller/
│   │   │   └── UserSyncController.java
│   │   ├── document/
│   │   │   └── UserDocument.java
│   │   ├── event/
│   │   │   └── UserEvent.java
│   │   ├── listener/
│   │   │   └── UserEventListener.java
│   │   ├── repository/
│   │   │   └── UserDocumentRepository.java
│   │   └── service/
│   │       └── UserSyncService.java
│   └── resources/
│       ├── application.yml
│       └── application-dev.yml
```

### Comandos Útiles

```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Crear JAR
mvn clean package

# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Ver logs en tiempo real
tail -f logs/database-sync-service-c.log
```

## Troubleshooting

### Problemas Comunes

1. **MongoDB no conecta**
   - Verificar que MongoDB esté ejecutándose
   - Comprobar la URI de conexión en `application-dev.yml`

2. **Kafka no conecta**
   - Verificar que Kafka esté ejecutándose
   - Comprobar la configuración de bootstrap-servers

3. **Puerto en uso**
   - Cambiar el puerto en `application-dev.yml`
   - Verificar que el puerto 8084 esté disponible

### Verificar Servicios

```bash
# Verificar MongoDB
mongosh --eval "db.adminCommand('ismaster')"

# Verificar Kafka
kafka-topics.sh --list --bootstrap-server localhost:9092

# Verificar puerto del servicio
netstat -an | grep 8084
```

## Contribución

1. Fork el proyecto
2. Crear una rama para la feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit los cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles. 