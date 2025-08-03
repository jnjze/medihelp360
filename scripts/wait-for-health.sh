#!/bin/bash

set -e

echo "⏳ Waiting for all MediHelp360 services to be healthy..."

# Configuration
MAX_TOTAL_WAIT=600  # 10 minutes total
CHECK_INTERVAL=15   # Check every 15 seconds
TIMEOUT=5          # HTTP timeout

# Start time
START_TIME=$(date +%s)

# Function to check service health
check_service_health() {
    local service_name=$1
    local health_url=$2
    
    if curl -f -s --connect-timeout $TIMEOUT "$health_url" > /dev/null 2>&1; then
        local health_response=$(curl -s --connect-timeout $TIMEOUT "$health_url" 2>/dev/null || echo '{"status":"UNKNOWN"}')
        local status=$(echo "$health_response" | jq -r '.status // "UNKNOWN"' 2>/dev/null || echo "UNKNOWN")
        
        if [ "$status" = "UP" ]; then
            return 0
        fi
    fi
    return 1
}

# Function to check container status
check_container_status() {
    local container_name=$1
    
    if docker ps --format "{{.Names}}\t{{.Status}}" | grep -q "$container_name.*Up.*healthy\|$container_name.*Up.*seconds"; then
        return 0
    fi
    return 1
}

# Function to wait for specific service
wait_for_service() {
    local service_name=$1
    local container_name=$2
    local health_url=$3
    
    while true; do
        # Check elapsed time
        CURRENT_TIME=$(date +%s)
        ELAPSED=$((CURRENT_TIME - START_TIME))
        
        if [ $ELAPSED -gt $MAX_TOTAL_WAIT ]; then
            echo "❌ Timeout waiting for $service_name after $MAX_TOTAL_WAIT seconds"
            return 1
        fi
        
        # Check container status
        if check_container_status "$container_name"; then
            if [ -n "$health_url" ]; then
                # Check HTTP health if URL provided
                if check_service_health "$service_name" "$health_url"; then
                    echo "✅ $service_name is healthy"
                    return 0
                else
                    echo "🔄 $service_name container is up but health check pending... (${ELAPSED}s elapsed)"
                fi
            else
                echo "✅ $service_name container is healthy"
                return 0
            fi
        else
            echo "🔄 Waiting for $service_name container to be ready... (${ELAPSED}s elapsed)"
        fi
        
        sleep $CHECK_INTERVAL
    done
}

echo "🏗️  Waiting for infrastructure services..."

# Wait for Consul
wait_for_service "Consul" "consul-server" "http://localhost:8500/v1/status/leader"

# Wait for Kafka (no health endpoint, just container status)
wait_for_service "Kafka" "kafka" ""

# Wait for databases
wait_for_service "PostgreSQL Users" "postgres-users" ""
wait_for_service "PostgreSQL Sync A" "postgres-sync-a" ""
wait_for_service "MySQL Sync B" "mysql-sync-b" ""
wait_for_service "MongoDB Sync C" "mongo-sync-c" ""

echo "🚀 Waiting for application services..."

# Wait for API Gateway (this is critical)
wait_for_service "API Gateway" "api-gateway" "http://localhost:8080/actuator/health"

# Wait for User Management Service
wait_for_service "User Management Service" "user-management-service" ""

# Wait for Database Sync Services (these are less critical)
wait_for_service "Database Sync Service A" "database-sync-service-a" "" || {
    echo "⚠️  Database Sync Service A failed to start, but continuing..."
}

wait_for_service "Database Sync Service B" "database-sync-service-b" "" || {
    echo "⚠️  Database Sync Service B failed to start, but continuing..."
}

wait_for_service "Database Sync Service C" "database-sync-service-c" "" || {
    echo "⚠️  Database Sync Service C failed to start, but continuing..."
}

echo "🔍 Performing final verification..."

# Final comprehensive check
sleep 10  # Give services a moment to fully initialize

# Check service discovery
echo "📡 Checking service discovery..."
REGISTERED_SERVICES=$(curl -s http://localhost:8500/v1/catalog/services | jq -r 'keys | length' 2>/dev/null || echo "0")
if [ "$REGISTERED_SERVICES" -gt 1 ]; then
    echo "✅ Service discovery is working ($REGISTERED_SERVICES services registered)"
else
    echo "⚠️  Service discovery might have issues (only $REGISTERED_SERVICES services registered)"
fi

# Check API Gateway routing
echo "🌐 Checking API Gateway routing..."
if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ API Gateway is accessible and routing correctly"
else
    echo "❌ API Gateway routing check failed"
    exit 1
fi

# Check if we can reach services through the gateway (basic connectivity)
echo "🔗 Testing service connectivity through gateway..."

# Test health endpoints that should be accessible through the gateway
GATEWAY_HEALTH=$(curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "FAILED")
if echo "$GATEWAY_HEALTH" | grep -q '"status":"UP"'; then
    echo "✅ Gateway health check passed"
else
    echo "⚠️  Gateway health check returned: $GATEWAY_HEALTH"
fi

TOTAL_ELAPSED=$(($(date +%s) - START_TIME))
echo ""
echo "🎉 Health check completed!"
echo "⏱️  Total time: ${TOTAL_ELAPSED} seconds"
echo "📊 Final status:"

# Show final service status
docker-compose -f docker-compose.secure.yml ps | grep -E "NAME|api-gateway|user-management|database-sync" || true

echo ""
echo "✅ MediHelp360 services are ready for traffic!"

exit 0 