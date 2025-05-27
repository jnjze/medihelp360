# User Management Service

Microservicio principal para la gestión de usuarios y roles en la arquitectura de microservicios.

## 🎯 Funcionalidades

- ✅ **CRUD completo de usuarios**
- ✅ **Gestión de roles**
- ✅ **Búsqueda y filtrado avanzado**
- ✅ **Paginación**
- ✅ **Estadísticas de usuarios**
- ✅ **Publicación de eventos en Kafka**
- ✅ **Validación de datos**
- ✅ **Manejo de errores**
- ✅ **Monitoreo con Actuator**

## 🚀 Endpoints Disponibles

### Usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/users` | Crear usuario |
| GET | `/api/users/{id}` | Obtener usuario por ID |
| GET | `/api/users` | Listar usuarios (paginado) |
| GET | `/api/users/search` | Buscar usuarios |
| GET | `/api/users/status/{status}` | Usuarios por estado |
| GET | `/api/users/role/{roleName}` | Usuarios por rol |
| GET | `/api/users/stats` | Estadísticas de usuarios |
| PUT | `/api/users/{id}` | Actualizar usuario |
| PATCH | `/api/users/{id}/status` | Cambiar estado |
| DELETE | `/api/users/{id}` | Deshabilitar usuario |

### Roles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/roles` | Crear rol |
| GET | `/api/roles/{id}` | Obtener rol por ID |
| GET | `/api/roles/name/{name}` | Obtener rol por nombre |
| GET | `/api/roles` | Listar todos los roles |
| DELETE | `/api/roles/{id}` | Eliminar rol |

## 📊 Eventos Kafka

El servicio publica eventos en el tópico `user-events`:

- **UserCreatedEvent**: Cuando se crea un usuario
- **UserUpdatedEvent**: Cuando se actualiza un usuario

## 🔧 Configuración

### Variables de Entorno

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/users_db
SPRING_DATASOURCE_USERNAME=users_user
SPRING_DATASOURCE_PASSWORD=users_pass

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Puerto del servicio
SERVER_PORT=8081
```

## 🏃‍♂️ Ejecutar el Servicio

### Prerrequisitos
- Java 17+
- PostgreSQL
- Kafka

### Comandos

```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run

# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report
```

## 📝 Ejemplos de Uso

### Crear Usuario

```bash
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

### Buscar Usuarios

```bash
curl "http://localhost:8081/api/users/search?name=Juan&page=0&size=10"
```

### Obtener Estadísticas

```bash
curl http://localhost:8081/api/users/stats
```

## 📈 Monitoreo

- **Health Check**: `http://localhost:8081/actuator/health`
- **Métricas**: `http://localhost:8081/actuator/metrics`
- **Prometheus**: `http://localhost:8081/actuator/prometheus`

## 🏗️ Arquitectura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controller    │───▶│     Service     │───▶│   Repository    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  Kafka Producer │
                       └─────────────────┘
```

## 🔒 Seguridad

- Encriptación de contraseñas con BCrypt
- Validación de entrada en todos los endpoints
- Manejo seguro de excepciones
- Configuración básica de Spring Security

## 📦 Dependencias Principales

- Spring Boot 3.2.0
- Spring Data JPA
- Spring Kafka
- PostgreSQL Driver
- Spring Security
- Lombok
- MapStruct 