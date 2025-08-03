#!/bin/bash

set -e

echo "🚀 MediHelp360 Deployment Script"
echo "================================="

# Configuración
ENVIRONMENT=${1:-production}
BUILD_TAG=${BUILD_TAG:-latest}
DOCKER_REGISTRY=${DOCKER_REGISTRY:-localhost:5000}

# Configuración específica por ambiente
case $ENVIRONMENT in
    "production")
        COMPOSE_FILE="docker-compose.secure.yml"
        PROJECT_NAME="medihelp360-prod"
        API_PORT=8080
        USER_PORT=8081
        SYNC_A_PORT=8082
        SYNC_B_PORT=8083
        SYNC_C_PORT=8084
        echo "🏭 PRODUCTION Environment"
        ;;
    "preprod")
        COMPOSE_FILE="docker-compose.preprod.yml"
        PROJECT_NAME="medihelp360-preprod"
        API_PORT=8180
        USER_PORT=8181
        SYNC_A_PORT=8182
        SYNC_B_PORT=8183
        SYNC_C_PORT=8184
        echo "🧪 PRE-PRODUCTION Environment"
        ;;
    "development")
        COMPOSE_FILE="docker-compose.dev.yml"
        PROJECT_NAME="medihelp360-dev"
        API_PORT=8280
        USER_PORT=8281
        SYNC_A_PORT=8282
        SYNC_B_PORT=8283
        SYNC_C_PORT=8284
        echo "💻 DEVELOPMENT Environment"
        ;;
    *)
        echo "❌ Environment '$ENVIRONMENT' no válido"
        echo "💡 Ambientes válidos: production, preprod, development"
        exit 1
        ;;
esac

echo "📋 Configuración del deploy:"
echo "   Environment: $ENVIRONMENT"
echo "   Compose file: $COMPOSE_FILE"
echo "   Project name: $PROJECT_NAME"
echo "   Build tag: $BUILD_TAG"
echo "   API Gateway: http://localhost:$API_PORT"
echo "   User Management: http://localhost:$USER_PORT"

# Función para actualizar un servicio
update_service() {
    local service_name=$1
    local container_name="${PROJECT_NAME}-${service_name}"
    local image_tag="${ENVIRONMENT}-latest"
    
    echo "🔄 Updating service: $service_name"
    echo "   Container: $container_name"
    echo "   Image: ${DOCKER_REGISTRY}/medihelp360-${service_name}:${image_tag}"
    
    # Pull latest image
    if docker pull "${DOCKER_REGISTRY}/medihelp360-${service_name}:${image_tag}" 2>/dev/null; then
        echo "✅ Image pulled successfully"
    else
        echo "⚠️  Could not pull image, using local version"
    fi
    
    # Gracefully stop the current container
    if docker ps -q -f name="$container_name" | grep -q .; then
        echo "🛑 Stopping current container..."
        docker stop "$container_name" || true
        docker rm "$container_name" || true
    fi
    
    # Start new container using docker-compose
    echo "🚀 Starting updated service..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d "$service_name"
    
    echo "✅ Service $service_name updated successfully"
    echo ""
}

# Función para esperar que un servicio esté saludable
wait_for_service_health() {
    local service_name=$1
    local health_url=""
    local max_attempts=30
    local attempt=1
    
    # Determinar URL de health check según el servicio
    case $service_name in
        "api-gateway")
            health_url="http://localhost:$API_PORT/actuator/health"
            ;;
        "user-management-service")
            health_url="http://localhost:$USER_PORT/actuator/health"
            ;;
        "database-sync-service-a")
            health_url="http://localhost:$SYNC_A_PORT/actuator/health"
            ;;
        "database-sync-service-b")
            health_url="http://localhost:$SYNC_B_PORT/actuator/health"
            ;;
        "database-sync-service-c")
            health_url="http://localhost:$SYNC_C_PORT/actuator/health"
            ;;
        *)
            echo "⚠️  Unknown service for health check: $service_name"
            return 0
            ;;
    esac
    
    echo "🏥 Waiting for $service_name to be healthy..."
    echo "   Health URL: $health_url"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$health_url" >/dev/null 2>&1; then
            echo "✅ $service_name is healthy (attempt $attempt)"
            return 0
        else
            echo "⏳ $service_name not ready (attempt $attempt/$max_attempts)"
            sleep 10
            attempt=$((attempt + 1))
        fi
    done
    
    echo "❌ $service_name failed to become healthy after $max_attempts attempts"
    return 1
}

# Función para verificar registro en Consul
check_consul_registration() {
    local service_name=$1
    local consul_url="http://localhost:8500/v1/health/service/$service_name"
    
    echo "🔍 Checking Consul registration for $service_name..."
    
    if curl -f -s "$consul_url" | jq -e '. | length > 0' >/dev/null 2>&1; then
        echo "✅ $service_name is registered in Consul"
        return 0
    else
        echo "⚠️  $service_name not found in Consul registry"
        return 1
    fi
}

# Función principal de deployment
main_deployment() {
    echo "🚀 Starting $ENVIRONMENT deployment..."
    echo "======================================"
    
    # Verificar que existe el archivo compose
    if [ ! -f "$COMPOSE_FILE" ]; then
        echo "❌ Compose file not found: $COMPOSE_FILE"
        exit 1
    fi
    
    # Crear/actualizar variables de ambiente para el compose
    export BUILD_TAG="$BUILD_TAG"
    export ENVIRONMENT="$ENVIRONMENT"
    export DOCKER_REGISTRY="$DOCKER_REGISTRY"
    
    # 1. Actualizar API Gateway primero (punto de entrada)
    echo "🌐 Phase 1: Updating API Gateway..."
    update_service "api-gateway"
    wait_for_service_health "api-gateway"
    
    # 2. Actualizar User Management Service
    echo "👥 Phase 2: Updating User Management Service..."
    update_service "user-management-service"
    wait_for_service_health "user-management-service"
    
    # 3. Actualizar Database Sync Services en paralelo
    echo "🔄 Phase 3: Updating Database Sync Services..."
    
    echo "📊 Updating Database Sync Service A (PostgreSQL)..."
    update_service "database-sync-service-a" &
    pid_a=$!
    
    echo "📊 Updating Database Sync Service B (MySQL)..."
    update_service "database-sync-service-b" &
    pid_b=$!
    
    echo "📊 Updating Database Sync Service C (MongoDB)..."
    update_service "database-sync-service-c" &
    pid_c=$!
    
    # Esperar que todos los sync services terminen
    wait $pid_a && echo "✅ Database Sync Service A updated" || echo "❌ Database Sync Service A failed"
    wait $pid_b && echo "✅ Database Sync Service B updated" || echo "❌ Database Sync Service B failed"
    wait $pid_c && echo "✅ Database Sync Service C updated" || echo "❌ Database Sync Service C failed"
    
    # 4. Health checks finales
    echo "🏥 Phase 4: Final Health Checks..."
    
    services=("api-gateway" "user-management-service" "database-sync-service-a" "database-sync-service-b" "database-sync-service-c")
    failed_services=()
    
    for service in "${services[@]}"; do
        if ! wait_for_service_health "$service"; then
            failed_services+=("$service")
        fi
    done
    
    # 5. Verificar registro en Consul
    echo "🗂️  Phase 5: Checking Consul Registration..."
    consul_failed=()
    
    for service in "${services[@]}"; do
        if ! check_consul_registration "$service"; then
            consul_failed+=("$service")
        fi
    done
    
    # 6. Reporte final
    echo ""
    echo "📊 DEPLOYMENT SUMMARY for $ENVIRONMENT"
    echo "======================================"
    echo "Environment: $ENVIRONMENT"
    echo "Build Tag: $BUILD_TAG"
    echo "Project Name: $PROJECT_NAME"
    echo ""
    
    if [ ${#failed_services[@]} -eq 0 ]; then
        echo "✅ All services are healthy!"
    else
        echo "❌ Failed services: ${failed_services[*]}"
    fi
    
    if [ ${#consul_failed[@]} -eq 0 ]; then
        echo "✅ All services registered in Consul!"
    else
        echo "⚠️  Services not in Consul: ${consul_failed[*]}"
    fi
    
    echo ""
    echo "🌐 Access URLs for $ENVIRONMENT:"
    echo "   API Gateway: http://localhost:$API_PORT"
    echo "   User Management: http://localhost:$USER_PORT"
    echo "   Consul UI: http://localhost:8500"
    echo "   Kafka UI: http://localhost:8080 (if available)"
    
    # Determinar exit code
    if [ ${#failed_services[@]} -eq 0 ]; then
        echo ""
        echo "🎉 $ENVIRONMENT deployment completed successfully!"
        exit 0
    else
        echo ""
        echo "💥 $ENVIRONMENT deployment completed with errors!"
        exit 1
    fi
}

# Función para mostrar ayuda
show_help() {
    echo "MediHelp360 Deployment Script"
    echo ""
    echo "Uso:"
    echo "  $0 [environment]"
    echo ""
    echo "Ambientes disponibles:"
    echo "  production   - Ambiente de producción (puertos 8080-8084)"
    echo "  preprod      - Ambiente pre-productivo (puertos 8180-8184)"
    echo "  development  - Ambiente de desarrollo (puertos 8280-8284)"
    echo ""
    echo "Variables de ambiente:"
    echo "  BUILD_TAG       - Tag de la imagen a desplegar (default: latest)"
    echo "  DOCKER_REGISTRY - Registry de Docker (default: localhost:5000)"
    echo ""
    echo "Ejemplos:"
    echo "  $0 production                    # Deploy a producción"
    echo "  $0 preprod                       # Deploy a pre-producción"
    echo "  $0 development                   # Deploy a desarrollo"
    echo "  BUILD_TAG=v1.2.3 $0 production   # Deploy tag específico"
}

# Verificar argumentos
case "${1:-help}" in
    "production"|"preprod"|"development")
        main_deployment
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        echo "❌ Ambiente no válido: $1"
        echo ""
        show_help
        exit 1
        ;;
esac 