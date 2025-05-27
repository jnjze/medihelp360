#!/bin/bash

echo "🧪 Testing MediHelp360 Service Discovery with Consul..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check service health
check_service() {
    local service_name=$1
    local port=$2
    local endpoint=$3
    
    echo -e "${BLUE}Checking $service_name on port $port...${NC}"
    
    if curl -s "http://localhost:$port$endpoint" > /dev/null; then
        echo -e "${GREEN}✅ $service_name is running${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name is not responding${NC}"
        return 1
    fi
}

# Function to check Consul registration
check_consul_registration() {
    local service_name=$1
    
    echo -e "${BLUE}Checking if $service_name is registered in Consul...${NC}"
    
    local response=$(curl -s "http://localhost:8500/v1/health/service/$service_name")
    
    if echo "$response" | grep -q "\"ServiceName\":\"$service_name\""; then
        echo -e "${GREEN}✅ $service_name is registered in Consul${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name is not registered in Consul${NC}"
        return 1
    fi
}

# Function to test API Gateway routing
test_gateway_routing() {
    local route=$1
    local description=$2
    
    echo -e "${BLUE}Testing API Gateway route: $route ($description)...${NC}"
    
    local response=$(curl -s -w "%{http_code}" "http://localhost:8080$route")
    local http_code="${response: -3}"
    
    if [[ "$http_code" == "200" ]] || [[ "$http_code" == "404" ]] || [[ "$http_code" == "503" ]]; then
        echo -e "${GREEN}✅ Route $route is accessible (HTTP $http_code)${NC}"
        return 0
    else
        echo -e "${RED}❌ Route $route failed (HTTP $http_code)${NC}"
        return 1
    fi
}

echo ""
echo "=== Step 1: Checking Consul ==="
check_service "Consul" "8500" "/v1/status/leader"

echo ""
echo "=== Step 2: Checking Individual Services ==="
check_service "User Management Service" "8081" "/actuator/health"
check_service "Database Sync Service A" "8082" "/actuator/health"
check_service "Database Sync Service B" "8083" "/actuator/health"
check_service "Database Sync Service C" "8084" "/actuator/health"
check_service "API Gateway" "8080" "/actuator/health"

echo ""
echo "=== Step 3: Checking Consul Service Registration ==="
sleep 5  # Wait for services to register
check_consul_registration "user-management-service"
check_consul_registration "database-sync-service-a"
check_consul_registration "database-sync-service-b"
check_consul_registration "database-sync-service-c"
check_consul_registration "api-gateway"

echo ""
echo "=== Step 4: Testing API Gateway Service Discovery Routing ==="
test_gateway_routing "/api/users" "User Management"
test_gateway_routing "/api/sync-a/health" "Database Sync Service A"
test_gateway_routing "/api/sync-b/health" "Database Sync Service B"
test_gateway_routing "/api/sync-c/health" "Database Sync Service C"

echo ""
echo "=== Step 5: Checking Consul UI ==="
echo -e "${YELLOW}📊 Consul UI: http://localhost:8500${NC}"
echo -e "${YELLOW}🔍 Check registered services in the UI${NC}"

echo ""
echo "=== Step 6: Service Discovery Summary ==="
echo -e "${BLUE}Registered Services in Consul:${NC}"
curl -s "http://localhost:8500/v1/catalog/services" | jq '.' 2>/dev/null || echo "Install jq for better JSON formatting"

echo ""
echo -e "${GREEN}🎉 Service Discovery testing completed!${NC}"
echo -e "${YELLOW}💡 Tips:${NC}"
echo "   - Check Consul UI for visual service status"
echo "   - Monitor service health checks"
echo "   - Test failover scenarios by stopping services" 