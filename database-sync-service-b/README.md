# Database Sync Service B

Servicio de sincronización de base de datos B para MediHelp360. Este servicio consume eventos de usuarios de Kafka del servicio A y los almacena en MySQL para consultas optimizadas.

## 🏗️ Arquitectura

- **Framework**: Spring Boot 3.2.0
- **Base de datos**: MySQL
- **Mensajería**: Apache Kafka
- **Puerto**: 8082

## 📋 Funcionalidades

### Consumo de Eventos Kafka
- Consume eventos de usuarios desde Kafka
- Procesamiento asíncrono con acknowledgment manual
- Reintentos automáticos en caso de error
- Manejo de eventos CREATE, UPDATE y DELETE

### Almacenamiento en MySQL
- Entidades JPA optimizadas para consultas rápidas
- Índices automáticos para campos frecuentemente consultados
- Metadatos de sincronización para auditoría
- Soft delete para mantener historial

### APIs REST
- Endpoints para consultar datos de usuarios sincronizados
- Búsqueda por texto completo
- Filtros avanzados y paginación
- Estadísticas y métricas

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Apache Kafka 2.8+

### Configuración

1. **MySQL**: Asegúrate de que MySQL esté ejecutándose en `localhost:3306`
2. **Kafka**: Asegúrate de que Kafka esté ejecutándose en `localhost:9092`
3. **Base de datos**: Crear la base de datos `sync_db_b` y el usuario `sync_user_b`

```sql
CREATE DATABASE sync_db_b;
CREATE USER 'sync_user_b'@'localhost' IDENTIFIED BY 'sync_pass_b';
GRANT ALL PRIVILEGES ON sync_db_b.* TO 'sync_user_b'@'localhost';
FLUSH PRIVILEGES;
```

### Ejecución

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run

# O ejecutar el JAR
mvn clean package
java -jar target/database-sync-service-b-1.0.0.jar
```

## 📡 APIs Disponibles

### Usuarios Sincronizados

```bash
# Obtener todos los usuarios (paginado)
GET /api/v1/users?page=0&size=20

# Buscar usuario por ID original
GET /api/v1/users/original/{originalId}

# Buscar usuario por email
GET /api/v1/users/email/{email}

# Buscar usuarios por rol
GET /api/v1/users/role/{role}

# Buscar usuarios activos
GET /api/v1/users/active

# Búsqueda por texto
GET /api/v1/users/search?query=juan

# Estadísticas de usuarios
GET /api/v1/users/stats
```

### Monitoreo

```bash
# Health check
GET /api/v1/actuator/health

# Métricas
GET /api/v1/actuator/metrics

# Prometheus metrics
GET /api/v1/actuator/prometheus
```

## 🔧 Configuración

### Variables de Entorno

```bash
# MySQL
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/sync_db_b
SPRING_DATASOURCE_USERNAME=sync_user_b
SPRING_DATASOURCE_PASSWORD=sync_pass_b

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_CONSUMER_GROUP_ID=sync-service-b-group

# Logging
LOGGING_LEVEL_COM_EXAMPLE_SYNC=DEBUG
```

### Tópicos de Kafka

El servicio consume el siguiente tópico:
- `user-events`: Eventos de usuarios

## 📊 Estructura de Datos

### Entidad MySQL

#### UserEntity
```json
{
  "id": 1,
  "originalId": 123,
  "username": "doctor.smith",
  "email": "doctor.smith@medihelp360.com",
  "firstName": "John",
  "lastName": "Smith",
  "role": "DOCTOR",
  "active": true,
  "department": "Cardiology",
  "specialization": "Cardiologist",
  "syncStatus": "SYNCED",
  "syncCreatedAt": "2024-01-01T10:00:00",
  "syncUpdatedAt": "2024-01-01T10:00:00"
}
```

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Ejecutar tests con perfil específico
mvn test -Dspring.profiles.active=test
```

## 📈 Monitoreo y Métricas

### Métricas Disponibles
- Eventos de usuarios procesados por tipo
- Tiempo de procesamiento
- Errores de sincronización
- Estado de conexiones (MySQL, Kafka)

### Logs
Los logs se almacenan en:
- Consola: Formato simple para desarrollo
- Archivo: `logs/database-sync-service-b.log`

## 🔍 Troubleshooting

### Problemas Comunes

1. **Error de conexión a MySQL**
   ```bash
   # Verificar que MySQL esté ejecutándose
   mysql -u sync_user_b -p sync_db_b
   ```

2. **Error de conexión a Kafka**
   ```bash
   # Verificar que Kafka esté ejecutándose
   kafka-topics.sh --bootstrap-server localhost:9092 --list
   ```

3. **Eventos no se procesan**
   - Verificar que el tópico `user-events` exista
   - Revisar logs para errores de deserialización
   - Verificar configuración de consumer group

### Logs Útiles

```bash
# Ver logs en tiempo real
tail -f logs/database-sync-service-b.log

# Filtrar logs de Kafka
grep "Kafka" logs/database-sync-service-b.log

# Filtrar logs de MySQL
grep "MySQL\|JPA" logs/database-sync-service-b.log
```

## 🔄 Sincronización

### Flujo de Datos

1. **Recepción**: Eventos de usuarios llegan desde Kafka
2. **Validación**: Se valida la estructura del evento
3. **Transformación**: Se mapea a entidad JPA
4. **Almacenamiento**: Se guarda en MySQL
5. **Acknowledgment**: Se confirma el procesamiento

### Estados de Sincronización

- `SYNCED`: Entidad sincronizada correctamente
- `PENDING`: Sincronización en proceso
- `ERROR`: Error en la sincronización
- `DELETED`: Entidad marcada como eliminada

## 🛠️ Desarrollo

### Estructura del Proyecto

```
src/main/java/com/example/sync/
├── config/          # Configuraciones (Kafka, JPA)
├── controller/      # Controladores REST
├── entity/          # Entidades JPA
├── event/          # Clases de eventos Kafka
├── listener/       # Listeners de Kafka
├── repository/     # Repositorios JPA
└── service/        # Servicios de negocio
```

### Esquema de Base de Datos

La tabla `users_sync` se crea automáticamente con los siguientes campos:
- `id`: Primary key auto-incremental
- `original_id`: ID del usuario en el servicio principal (único)
- `username`, `email`, `first_name`, `last_name`: Datos del usuario
- `role`, `active`, `department`, `specialization`: Información adicional
- `sync_created_at`, `sync_updated_at`: Timestamps de sincronización
- `sync_status`: Estado de la sincronización
- Índices automáticos para optimizar consultas

## 📝 Licencia

Este proyecto es parte del sistema MediHelp360. 