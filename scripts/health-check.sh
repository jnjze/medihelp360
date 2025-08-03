#!/bin/bash

set -e

echo "🏥 Running comprehensive health checks for MediHelp360 services..."

# Configuration
MAX_ATTEMPTS=30
SLEEP_INTERVAL=10
HEALTH_CHECK_TIMEOUT=5

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "SUCCESS")
            echo -e "${GREEN}✅ $message${NC}"
            ;;
        "WARNING")
            echo -e "${YELLOW}⚠️  $message${NC}"
            ;;
        "ERROR")
            echo -e "${RED}❌ $message${NC}"
            ;;
        "INFO")
            echo -e "${BLUE}ℹ️  $message${NC}"
            ;;
    esac
}

# Function to check if a service is healthy via HTTP
check_http_health() {
    local service_name=$1
    local health_url=$2
    local attempt=1
    
    print_status "INFO" "Checking $service_name health at $health_url"
    
    while [ $attempt -le $MAX_ATTEMPTS ]; do
        if curl -f -s --connect-timeout $HEALTH_CHECK_TIMEOUT "$health_url" > /dev/null 2>&1; then
            # Get detailed health info
            local health_response=$(curl -s --connect-timeout $HEALTH_CHECK_TIMEOUT "$health_url" 2>/dev/null || echo '{"status":"UNKNOWN"}')
            local status=$(echo "$health_response" | jq -r '.status // "UNKNOWN"' 2>/dev/null || echo "UNKNOWN")
            
            if [ "$status" = "UP" ]; then
                print_status "SUCCESS" "$service_name is healthy (status: $status)"
                return 0
            else
                print_status "WARNING" "$service_name responded but status is: $status"
            fi
        fi
        
        print_status "INFO" "Attempt $attempt/$MAX_ATTEMPTS - $service_name not ready yet..."
        sleep $SLEEP_INTERVAL
        attempt=$((attempt + 1))
    done
    
    print_status "ERROR" "$service_name failed health check after $MAX_ATTEMPTS attempts"
    return 1
}

# Function to check Docker container status
check_docker_container() {
    local container_name=$1
    
    if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "$container_name.*Up"; then
        local status=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep "$container_name" | awk '{print $2,$3,$4}')
        print_status "SUCCESS" "Container $container_name is running ($status)"
        return 0
    else
        print_status "ERROR" "Container $container_name is not running"
        return 1
    fi
}

# Function to check service registration in Consul
check_consul_registration() {
    local service_name=$1
    
    if curl -s http://localhost:8500/v1/catalog/services | jq -r 'keys[]' | grep -q "$service_name"; then
        print_status "SUCCESS" "$service_name is registered in Consul"
        return 0
    else
        print_status "ERROR" "$service_name is not registered in Consul"
        return 1
    fi
}

# Function to check database connectivity
check_database_connectivity() {
    local db_type=$1
    local container_name=$2
    
    case $db_type in
        "postgres")
            if docker exec "$container_name" pg_isready -U postgres > /dev/null 2>&1; then
                print_status "SUCCESS" "PostgreSQL ($container_name) is accepting connections"
                return 0
            else
                print_status "ERROR" "PostgreSQL ($container_name) is not accepting connections"
                return 1
            fi
            ;;
        "mysql")
            if docker exec "$container_name" mysqladmin ping -h localhost > /dev/null 2>&1; then
                print_status "SUCCESS" "MySQL ($container_name) is accepting connections"
                return 0
            else
                print_status "ERROR" "MySQL ($container_name) is not accepting connections"
                return 1
            fi
            ;;
        "mongo")
            if docker exec "$container_name" mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
                print_status "SUCCESS" "MongoDB ($container_name) is accepting connections"
                return 0
            else
                print_status "ERROR" "MongoDB ($container_name) is not accepting connections"
                return 1
            fi
            ;;
    esac
}

# Start health checks
echo "=================================================="
echo "🏥 MediHelp360 Health Check Report"
echo "📅 $(date)"
echo "=================================================="

OVERALL_STATUS=0

# Check infrastructure services
echo ""
print_status "INFO" "Checking infrastructure services..."

# Consul
if check_docker_container "consul-server" && check_http_health "Consul" "http://localhost:8500/v1/status/leader"; then
    print_status "SUCCESS" "Consul is healthy"
else
    print_status "ERROR" "Consul is unhealthy"
    OVERALL_STATUS=1
fi

# Kafka
if check_docker_container "kafka"; then
    print_status "SUCCESS" "Kafka container is running"
else
    print_status "ERROR" "Kafka container is not running"
    OVERALL_STATUS=1
fi

# Zookeeper
if check_docker_container "zookeeper"; then
    print_status "SUCCESS" "Zookeeper container is running"
else
    print_status "ERROR" "Zookeeper container is not running"
    OVERALL_STATUS=1
fi

# Database connectivity
echo ""
print_status "INFO" "Checking database connectivity..."

check_database_connectivity "postgres" "postgres-users" || OVERALL_STATUS=1
check_database_connectivity "postgres" "postgres-sync-a" || OVERALL_STATUS=1
check_database_connectivity "mysql" "mysql-sync-b" || OVERALL_STATUS=1
check_database_connectivity "mongo" "mongo-sync-c" || OVERALL_STATUS=1

# Check application services
echo ""
print_status "INFO" "Checking application services..."

# API Gateway
if check_docker_container "api-gateway" && \
   check_http_health "API Gateway" "http://localhost:8080/actuator/health" && \
   check_consul_registration "api-gateway"; then
    print_status "SUCCESS" "API Gateway is fully operational"
else
    print_status "ERROR" "API Gateway has issues"
    OVERALL_STATUS=1
fi

# User Management Service
if check_docker_container "user-management-service" && \
   check_consul_registration "user-management-service"; then
    print_status "SUCCESS" "User Management Service is operational"
else
    print_status "ERROR" "User Management Service has issues"
    OVERALL_STATUS=1
fi

# Database Sync Service A
if check_docker_container "database-sync-service-a" && \
   check_consul_registration "database-sync-service-a"; then
    print_status "SUCCESS" "Database Sync Service A is operational"
else
    print_status "WARNING" "Database Sync Service A has issues"
fi

# Database Sync Service B
if check_docker_container "database-sync-service-b"; then
    print_status "SUCCESS" "Database Sync Service B is operational"
else
    print_status "WARNING" "Database Sync Service B has issues"
fi

# Database Sync Service C
if check_docker_container "database-sync-service-c" && \
   check_consul_registration "database-sync-service-c"; then
    print_status "SUCCESS" "Database Sync Service C is operational"
else
    print_status "WARNING" "Database Sync Service C has issues"
fi

# Integration tests
echo ""
print_status "INFO" "Running integration tests..."

# Test API Gateway routing
if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
    print_status "SUCCESS" "API Gateway routing is working"
else
    print_status "ERROR" "API Gateway routing failed"
    OVERALL_STATUS=1
fi

# Test service discovery
if [ "$(curl -s http://localhost:8500/v1/catalog/services | jq -r 'keys | length')" -gt 1 ]; then
    print_status "SUCCESS" "Service discovery is working"
else
    print_status "ERROR" "Service discovery failed"
    OVERALL_STATUS=1
fi

# Summary
echo ""
echo "=================================================="
if [ $OVERALL_STATUS -eq 0 ]; then
    print_status "SUCCESS" "All critical health checks passed! 🎉"
    echo "🚀 MediHelp360 is ready for production traffic"
else
    print_status "ERROR" "Some health checks failed! ⚠️"
    echo "🔧 Please review the issues above before proceeding"
fi
echo "=================================================="

exit $OVERALL_STATUS 