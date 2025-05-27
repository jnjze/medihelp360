# Arquitectura de Microservicios - Gestión de Usuarios y Roles

## Visión General

Esta arquitectura implementa un sistema escalable de gestión de usuarios y roles utilizando Spring Boot con sincronización automática entre múltiples bases de datos mediante un enfoque event-driven.

## Arquitectura

```
┌─────────────────┐
│   API Gateway   │
│   (Spring Boot) │
└─────────┬───────┘
          │
┌─────────▼───────┐      ┌──────────────────┐
│  User Service   │─────▶│   Event Bus      │
│  (Spring Boot)  │      │   (Kafka/RabbitMQ)│
└─────────────────┘      └─────────┬────────┘
                                   │
                   ┌───────────────┼───────────────┐
                   │               │               │
         ┌─────────▼──────┐ ┌──────▼──────┐ ┌─────▼──────┐
         │ DB Sync Service │ │ DB Sync     │ │ DB Sync    │
         │ Database A      │ │ Service B   │ │ Service C  │
         │ (Spring Boot)   │ │(Spring Boot)│ │(Spring Boot)│
         └─────────────────┘ └─────────────┘ └────────────┘
                   │               │               │
         ┌─────────▼──────┐ ┌──────▼──────┐ ┌─────▼──────┐
         │  Database A    │ │ Database B  │ │ Database C │
         │  (PostgreSQL)  │ │ (MySQL)     │ │ (MongoDB)  │
         └────────────────┘ └─────────────┘ └────────────┘
```

## Componentes

### 1. API Gateway
- **Responsabilidad**: Punto de entrada único, routing, autenticación, rate limiting
- **Tecnología**: Spring Boot + Spring Cloud Gateway
- **Puerto**: 8080

### 2. User Management Service
- **Responsabilidad**: Lógica de negocio principal para usuarios y roles
- **Funcionalidades**: 
  - CRUD de usuarios
  - Gestión de roles
  - Validaciones de negocio
  - Publicación de eventos
- **Puerto**: 8081
- **Base de datos**: PostgreSQL (principal)

### 3. Database Sync Services
- **Responsabilidad**: Sincronización específica para cada base de datos externa
- **Funcionalidades**:
  - Escuchar eventos de cambios de usuario
  - Transformar datos según el esquema de destino
  - Manejar errores y reintentos
  - Mantener logs de sincronización
- **Puertos**: 8082, 8083, 8084

### 4. Event Bus
- **Tecnología**: Apache Kafka (recomendado) o RabbitMQ
- **Eventos**:
  - `UserCreatedEvent`
  - `UserUpdatedEvent`
  - `UserDisabledEvent`
  - `RoleAssignedEvent`

## Patrones Implementados

### 1. Event Sourcing
- Todos los cambios se registran como eventos inmutables
- Permite auditoría completa y replay de eventos

### 2. CQRS (Command Query Responsibility Segregation)
- Separación entre operaciones de escritura y lectura
- Optimización independiente de cada lado

### 3. Saga Pattern
- Coordinación de transacciones distribuidas
- Compensación automática en caso de fallos

### 4. Circuit Breaker
- Protección contra fallos en cascada
- Recuperación automática de servicios

## Estructura de Eventos

```json
{
  "eventId": "uuid",
  "eventType": "UserCreatedEvent",
  "aggregateId": "userId",
  "version": 1,
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "name": "Juan Pérez",
    "roles": ["USER"],
    "status": "ACTIVE"
  },
  "metadata": {
    "correlationId": "uuid",
    "causationId": "uuid",
    "userId": "admin-uuid"
  }
}
```

## Escalabilidad

### Agregar Nuevas Bases de Datos
1. Crear nuevo microservicio de sincronización
2. Configurar consumer para los eventos relevantes
3. Implementar transformaciones específicas
4. Configurar health checks y monitoreo

### Sharding Horizontal
- Particionamiento por región geográfica
- Particionamiento por tipo de usuario
- Load balancing inteligente

## Monitoreo y Observabilidad

- **Logs centralizados**: ELK Stack
- **Métricas**: Prometheus + Grafana
- **Tracing distribuido**: Jaeger
- **Health checks**: Spring Boot Actuator

## Seguridad

### Gateway-Only Access (Default)
By default, all microservices are configured to **block direct access** and only allow requests through the API Gateway:

- ✅ `http://localhost:8080/api/users` (through API Gateway)
- ❌ `http://localhost:8081/api/users` (direct access BLOCKED)
- ✅ `http://localhost:8081/api/actuator/health` (health checks allowed)

### Security Configuration
```yaml
# application.yml (all services)
security:
  development:
    allow-direct-access: false  # 🔒 SECURE by default
  gateway:
    required-header:
      name: "X-Gateway-Request"
      value: "medihelp360-gateway"
```

### Toggle Security Mode
```bash
# Check current security configuration
./toggle-security.sh status

# Enable secure mode (default - block direct access)
./toggle-security.sh secure

# Enable debug mode (allow direct access for debugging)
./toggle-security.sh debug

# Test security configuration
./test-gateway-security.sh
```

### Security Features
- **🔒 Secure by Default**: Direct access blocked on all services
- **🏥 Health Checks**: Always allowed for monitoring
- **🛡️ Gateway Headers**: Automatic identification headers
- **📊 Security Logging**: Complete audit trail
- **🎯 Centralized Control**: All requests through API Gateway

## Próximos Pasos

1. Implementar User Management Service
2. Configurar Event Bus (Kafka)
3. Crear primer Database Sync Service
4. Implementar API Gateway
5. Configurar monitoreo y logging
6. Pruebas de integración
7. Despliegue en contenedores (Docker/Kubernetes)

# MediHelp360 - Healthcare Microservices Ecosystem

## 🏥 Overview

MediHelp360 is a comprehensive healthcare management platform built with a microservices architecture. The system provides secure user management, real-time database synchronization across multiple database technologies, and intelligent API routing with service discovery.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │    │   Web Portal    │    │  Mobile Apps    │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │      API Gateway         │
                    │   (Port 8080)           │
                    │   - Load Balancing      │
                    │   - Circuit Breaker     │
                    │   - Service Discovery   │
                    └─────────────┬─────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │        Consul           │
                    │   (Port 8500)          │
                    │   - Service Registry    │
                    │   - Health Checks      │
                    │   - Configuration      │
                    └─────────────┬─────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                       │                        │
┌───────▼───────┐    ┌──────────▼──────────┐    ┌────────▼────────┐
│ User Mgmt     │    │  Database Sync      │    │  Database Sync  │
│ Service       │    │  Service A          │    │  Service B      │
│ (Port 8081)   │    │  (Port 8082)        │    │  (Port 8083)    │
│ - JWT Auth    │    │  - PostgreSQL       │    │  - MySQL        │
│ - User CRUD   │    │  - Kafka Consumer   │    │  - Kafka Consumer│
└───────────────┘    └─────────────────────┘    └─────────────────┘
                                                          │
                                                 ┌────────▼────────┐
                                                 │  Database Sync  │
                                                 │  Service C      │
                                                 │  (Port 8084)    │
                                                 │  - MongoDB      │
                                                 │  - Kafka Consumer│
                                                 └─────────────────┘
```

## 🚀 Features

### Core Services
- **API Gateway**: Intelligent routing, load balancing, circuit breaker patterns
- **User Management**: Secure authentication, authorization, and user lifecycle management
- **Database Synchronization**: Real-time data sync across PostgreSQL, MySQL, and MongoDB
- **Service Discovery**: Dynamic service registration and discovery with Consul

### Technical Features
- **Microservices Architecture**: Loosely coupled, independently deployable services
- **Service Discovery**: Automatic service registration and health monitoring
- **Circuit Breaker**: Fault tolerance and graceful degradation
- **Load Balancing**: Intelligent request distribution
- **Health Monitoring**: Comprehensive health checks and metrics
- **Event-Driven**: Kafka-based asynchronous communication
- **Multi-Database**: Support for PostgreSQL, MySQL, and MongoDB

## 📋 Prerequisites

- **Java**: 17 or higher
- **Maven**: 3.6 or higher
- **Docker**: For running Consul and databases
- **Docker Compose**: For orchestrating containers

## 🛠️ Quick Start

### 1. Start Service Discovery (Consul)

```bash
# Start Consul using Docker Compose
./start-consul.sh

# Or manually:
docker-compose -f docker-compose.consul.yml up -d
```

### 2. Start All Services

```bash
# Terminal 1 - User Management Service
cd user-management-service
mvn spring-boot:run

# Terminal 2 - Database Sync Service A (PostgreSQL)
cd database-sync-service-a
mvn spring-boot:run

# Terminal 3 - Database Sync Service B (MySQL)
cd database-sync-service-b
mvn spring-boot:run

# Terminal 4 - Database Sync Service C (MongoDB)
cd database-sync-service-c
mvn spring-boot:run

# Terminal 5 - API Gateway
cd api-gateway
mvn spring-boot:run
```

### 3. Test Service Discovery

```bash
# Run comprehensive service discovery tests
./test-service-discovery.sh
```

## 🔍 Service Discovery

### Consul Configuration

All services are configured to register with Consul for dynamic service discovery:

- **Consul UI**: http://localhost:8500
- **Service Registration**: Automatic on startup
- **Health Checks**: Every 10 seconds
- **Load Balancing**: Client-side with Spring Cloud LoadBalancer

### Service Endpoints

| Service | Port | Health Check | Consul Name |
|---------|------|--------------|-------------|
| API Gateway | 8080 | `/actuator/health` | `api-gateway` |
| User Management | 8081 | `/actuator/health` | `user-management-service` |
| DB Sync A (PostgreSQL) | 8082 | `/actuator/health` | `database-sync-service-a` |
| DB Sync B (MySQL) | 8083 | `/actuator/health` | `database-sync-service-b` |
| DB Sync C (MongoDB) | 8084 | `/actuator/health` | `database-sync-service-c` |

## 🌐 API Routes

All requests go through the API Gateway at `http://localhost:8080`:

### User Management
- `GET /api/users` - List all users
- `POST /api/users` - Create new user
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Database Sync Services
- `GET /api/sync-a/health` - PostgreSQL sync service health
- `GET /api/sync-b/health` - MySQL sync service health
- `GET /api/sync-c/health` - MongoDB sync service health

## 🔧 Configuration

### Environment Variables

```bash
# Consul Configuration
CONSUL_HOST=localhost
CONSUL_PORT=8500

# Service URLs (automatically discovered via Consul)
USER_MANAGEMENT_SERVICE_URL=lb://user-management-service
DATABASE_SYNC_A_URL=lb://database-sync-service-a
DATABASE_SYNC_B_URL=lb://database-sync-service-b
DATABASE_SYNC_C_URL=lb://database-sync-service-c
```

## 📊 Monitoring

### Health Checks
- **Individual Services**: `http://localhost:{port}/actuator/health`
- **API Gateway**: `http://localhost:8080/actuator/health`
- **Consul Health**: `http://localhost:8500/v1/status/leader`

### Metrics
- **Prometheus Metrics**: `/actuator/prometheus`
- **Service Metrics**: `/actuator/metrics`
- **Consul Metrics**: Available in Consul UI

### Consul UI
Access the Consul web interface at `http://localhost:8500` to:
- View registered services
- Monitor health checks
- Check service dependencies
- View configuration

## 🧪 Testing

### Service Discovery Testing
```bash
# Run comprehensive tests
./test-service-discovery.sh

# Test individual components
curl http://localhost:8500/v1/catalog/services  # List services
curl http://localhost:8080/actuator/health      # API Gateway health
curl http://localhost:8080/api/users           # Test routing
```

### Circuit Breaker Testing
```bash
# Stop a service to test circuit breaker
# The API Gateway will return fallback responses
curl http://localhost:8080/api/users
```

## 🔒 Security

### Gateway-Only Access (Default)
By default, all microservices are configured to **block direct access** and only allow requests through the API Gateway:

- ✅ `http://localhost:8080/api/users` (through API Gateway)
- ❌ `http://localhost:8081/api/users` (direct access BLOCKED)
- ✅ `http://localhost:8081/api/actuator/health` (health checks allowed)

### Security Configuration
```yaml
# application.yml (all services)
security:
  development:
    allow-direct-access: false  # 🔒 SECURE by default
  gateway:
    required-header:
      name: "X-Gateway-Request"
      value: "medihelp360-gateway"
```

### Toggle Security Mode
```bash
# Check current security configuration
./toggle-security.sh status

# Enable secure mode (default - block direct access)
./toggle-security.sh secure

# Enable debug mode (allow direct access for debugging)
./toggle-security.sh debug

# Test security configuration
./test-gateway-security.sh
```

### Security Features
- **🔒 Secure by Default**: Direct access blocked on all services
- **🏥 Health Checks**: Always allowed for monitoring
- **🛡️ Gateway Headers**: Automatic identification headers
- **📊 Security Logging**: Complete audit trail
- **🎯 Centralized Control**: All requests through API Gateway

## 🚀 Deployment

### Docker Deployment
```bash
# Build all services
mvn clean package -DskipTests

# Deploy with Docker Compose (coming soon)
docker-compose up -d
```

### Production Considerations
- Use external Consul cluster
- Configure SSL/TLS
- Set up monitoring and alerting
- Implement centralized logging
- Configure backup strategies

## 📁 Project Structure

```
medihelp360/
├── api-gateway/                 # API Gateway service
├── user-management-service/     # User management microservice
├── database-sync-service-a/     # PostgreSQL sync service
├── database-sync-service-b/     # MySQL sync service
├── database-sync-service-c/     # MongoDB sync service
├── docker-compose.consul.yml    # Consul Docker Compose
├── start-consul.sh             # Consul startup script
├── test-service-discovery.sh   # Service discovery testing
└── README.md                   # This file
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For technical support or questions:
- **Email**: support@medihelp360.com
- **Documentation**: [Internal Wiki](http://wiki.medihelp360.com)
- **Issues**: Create an issue in this repository

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**MediHelp360** - Transforming Healthcare Through Technology 🏥✨ 