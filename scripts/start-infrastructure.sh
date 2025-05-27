#!/bin/bash

# Script para inicializar la infraestructura de microservicios
# Autor: Arquitectura de Microservicios
# Fecha: 2024

set -e

echo "🚀 Iniciando infraestructura de microservicios..."

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
}

# Verificar que Docker esté corriendo
if ! docker info > /dev/null 2>&1; then
    error "Docker no está corriendo. Por favor inicia Docker Desktop primero."
    exit 1
fi

# Verificar que docker-compose esté disponible
if ! command -v docker-compose &> /dev/null; then
    error "docker-compose no está instalado. Por favor instálalo primero."
    exit 1
fi

# Crear directorios necesarios
log "Creando directorios necesarios..."
mkdir -p monitoring
mkdir -p logs

# Crear archivo de configuración de Prometheus
log "Creando configuración de Prometheus..."
cat > monitoring/prometheus.yml << EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'user-management-service'
    static_configs:
      - targets: ['host.docker.internal:8081']
    metrics_path: '/actuator/prometheus'

  - job_name: 'database-sync-service-a'
    static_configs:
      - targets: ['host.docker.internal:8082']
    metrics_path: '/actuator/prometheus'

  - job_name: 'database-sync-service-b'
    static_configs:
      - targets: ['host.docker.internal:8083']
    metrics_path: '/actuator/prometheus'

  - job_name: 'database-sync-service-c'
    static_configs:
      - targets: ['host.docker.internal:8084']
    metrics_path: '/actuator/prometheus'
EOF

# Detener contenedores existentes
log "Deteniendo contenedores existentes..."
docker-compose down -v || true

# Limpiar volúmenes huérfanos
log "Limpiando volúmenes huérfanos..."
docker volume prune -f || true

# Iniciar infraestructura
log "Iniciando infraestructura con Docker Compose..."
docker-compose up -d

# Esperar a que los servicios estén listos
log "Esperando a que los servicios estén listos..."

# Función para verificar si un servicio está listo
check_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if nc -z $host $port 2>/dev/null; then
            log "✅ $service_name está listo"
            return 0
        fi
        echo "Esperando $service_name... (intento $attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done

    error "❌ $service_name no pudo iniciarse después de $max_attempts intentos"
    return 1
}

# Verificar servicios principales
check_service localhost 5432 "PostgreSQL (Users DB)"
check_service localhost 5433 "PostgreSQL (Sync DB A)"
check_service localhost 3306 "MySQL (Sync DB B)"
check_service localhost 27017 "MongoDB (Sync DB C)"
check_service localhost 9092 "Kafka"

# Esperar un poco más para Kafka
log "Esperando que Kafka esté completamente listo..."
sleep 10

# Crear tópicos de Kafka
log "Creando tópicos de Kafka..."
docker exec kafka kafka-topics --create --topic user-events --partitions 3 --replication-factor 1 --if-not-exists --bootstrap-server localhost:9092
docker exec kafka kafka-topics --create --topic user-sync-errors --partitions 1 --replication-factor 1 --if-not-exists --bootstrap-server localhost:9092

# Verificar tópicos creados
log "Verificando tópicos de Kafka..."
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

log "🎉 Infraestructura iniciada exitosamente!"
echo ""
log "📋 Servicios disponibles:"
echo "  🔗 Kafka UI: http://localhost:8090"
echo "  📊 Grafana: http://localhost:3000 (admin/admin)"
echo "  📈 Prometheus: http://localhost:9090"
echo "  🔍 Jaeger: http://localhost:16686"
echo ""
log "🗄️ Bases de datos:"
echo "  🐘 PostgreSQL Users: localhost:5432 (users_db/users_user/users_pass)"
echo "  🐘 PostgreSQL Sync A: localhost:5433 (sync_db_a/sync_user_a/sync_pass_a)"
echo "  🐬 MySQL Sync B: localhost:3306 (sync_db_b/sync_user_b/sync_pass_b)"
echo "  🍃 MongoDB Sync C: localhost:27017 (sync_db_c/sync_user_c/sync_pass_c)"
echo ""
log "▶️  Siguiente paso: Compilar e iniciar los microservicios"
echo "   ./scripts/build-services.sh" 