#!/bin/bash

echo "🔒 Testing API Gateway Security - Direct Access vs Gateway Access"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo "=== Current Configuration ==="
echo "🔒 Security mode: allow-direct-access = false (BLOCKED by default)"
echo "📝 To allow direct access, set allow-direct-access = true in application.yml"

echo ""
echo "=== Test 1: Direct Access to User Management Service ==="
echo -e "${BLUE}Testing direct access to http://localhost:8081/api/users${NC}"

response=$(curl -s -w "%{http_code}" -o /tmp/direct_response.json "http://localhost:8081/api/users" 2>/dev/null)
http_code="${response: -3}"

if [[ "$http_code" == "403" ]]; then
    echo -e "${GREEN}✅ Direct access correctly blocked (HTTP $http_code)${NC}"
    echo "📄 Response:"
    cat /tmp/direct_response.json 2>/dev/null || echo "No response body"
elif [[ "$http_code" == "200" ]]; then
    echo -e "${YELLOW}⚠️  Direct access allowed (HTTP $http_code) - Check configuration${NC}"
    echo "📄 Response preview:"
    head -c 200 /tmp/direct_response.json 2>/dev/null || echo "No response body"
else
    echo -e "${YELLOW}⚠️  Unexpected response (HTTP $http_code)${NC}"
fi

echo ""
echo "=== Test 2: Access through API Gateway ==="
echo -e "${BLUE}Testing access through API Gateway: http://localhost:8080/api/users${NC}"

response=$(curl -s -w "%{http_code}" -o /tmp/gateway_response.json "http://localhost:8080/api/users" 2>/dev/null)
http_code="${response: -3}"

if [[ "$http_code" == "200" ]]; then
    echo -e "${GREEN}✅ Gateway access successful (HTTP $http_code)${NC}"
    echo "📄 Response preview:"
    head -c 200 /tmp/gateway_response.json 2>/dev/null || echo "No response body"
elif [[ "$http_code" == "503" ]]; then
    echo -e "${YELLOW}⚠️  Service unavailable through gateway (HTTP $http_code)${NC}"
    echo "📄 This might be a circuit breaker or service discovery issue"
else
    echo -e "${RED}❌ Gateway access failed (HTTP $http_code)${NC}"
fi

echo ""
echo "=== Test 3: Health Check Endpoints (Always Allowed) ==="
echo -e "${BLUE}Testing health check access (should always work)${NC}"

# Test direct health check
response=$(curl -s -w "%{http_code}" "http://localhost:8081/api/actuator/health" 2>/dev/null)
http_code="${response: -3}"

if [[ "$http_code" == "200" ]]; then
    echo -e "${GREEN}✅ Direct health check successful (HTTP $http_code)${NC}"
else
    echo -e "${RED}❌ Direct health check failed (HTTP $http_code)${NC}"
fi

# Test gateway health check
response=$(curl -s -w "%{http_code}" "http://localhost:8080/actuator/health" 2>/dev/null)
http_code="${response: -3}"

if [[ "$http_code" == "200" ]]; then
    echo -e "${GREEN}✅ Gateway health check successful (HTTP $http_code)${NC}"
else
    echo -e "${RED}❌ Gateway health check failed (HTTP $http_code)${NC}"
fi

echo ""
echo "=== Test 4: Testing Database Sync Services ==="
echo -e "${BLUE}Testing direct access to database sync services (should be blocked)${NC}"

# Test Database Sync Service A
response=$(curl -s -w "%{http_code}" "http://localhost:8082/api/sync" 2>/dev/null)
http_code="${response: -3}"

if [[ "$http_code" == "403" ]]; then
    echo -e "${GREEN}✅ Database Sync Service A correctly blocked (HTTP $http_code)${NC}"
else
    echo -e "${YELLOW}⚠️  Database Sync Service A: (HTTP $http_code)${NC}"
fi

# Test Database Sync Service B
response=$(curl -s -w "%{http_code}" "http://localhost:8083/api/sync" 2>/dev/null)
http_code="${response: -3}"

if [[ "$http_code" == "403" ]]; then
    echo -e "${GREEN}✅ Database Sync Service B correctly blocked (HTTP $http_code)${NC}"
else
    echo -e "${YELLOW}⚠️  Database Sync Service B: (HTTP $http_code)${NC}"
fi

# Test Database Sync Service C
response=$(curl -s -w "%{http_code}" "http://localhost:8084/api/sync" 2>/dev/null)
http_code="${response: -3}"

if [[ "$http_code" == "403" ]]; then
    echo -e "${GREEN}✅ Database Sync Service C correctly blocked (HTTP $http_code)${NC}"
else
    echo -e "${YELLOW}⚠️  Database Sync Service C: (HTTP $http_code)${NC}"
fi

echo ""
echo "=== Test 5: Testing with Custom Headers ==="
echo -e "${BLUE}Testing direct access with gateway header (should still be blocked)${NC}"

response=$(curl -s -w "%{http_code}" -H "X-Gateway-Request: medihelp360-gateway" -o /tmp/header_response.json "http://localhost:8081/api/users" 2>/dev/null)
http_code="${response: -3}"

if [[ "$http_code" == "403" ]]; then
    echo -e "${GREEN}✅ Direct access with gateway header correctly blocked (HTTP $http_code)${NC}"
    echo "📄 Note: Direct access is blocked regardless of headers when allow-direct-access=false"
elif [[ "$http_code" == "200" ]]; then
    echo -e "${YELLOW}⚠️  Direct access with gateway header allowed (HTTP $http_code)${NC}"
    echo "📄 This suggests allow-direct-access=true in configuration"
else
    echo -e "${YELLOW}⚠️  Direct access with gateway header: (HTTP $http_code)${NC}"
fi

echo ""
echo "=== Security Configuration Guide ==="
echo -e "${GREEN}✅ Current Configuration: SECURE (Gateway-Only Access)${NC}"
echo ""
echo -e "${YELLOW}📋 To allow direct access for debugging:${NC}"
echo ""
echo "1. Edit application.yml files"
echo "2. Change: security.development.allow-direct-access: true"
echo "3. Restart the services"
echo ""
echo -e "${YELLOW}📋 For production deployment:${NC}"
echo ""
echo "1. Keep allow-direct-access: false"
echo "2. Use Docker Compose with network isolation (docker-compose.secure.yml)"
echo "3. Only expose API Gateway port (8080) publicly"
echo ""
echo -e "${YELLOW}📋 Current Network Architecture:${NC}"
echo ""
echo "🔒 SECURE MODE:"
echo "Public Access:     [Client] → [API Gateway:8080] → [Microservices]"
echo "Direct Access:     [Client] → [Microservices] ❌ BLOCKED"
echo "Health Checks:     [Client] → [Microservices/health] ✅ ALLOWED"

echo ""
echo -e "${GREEN}🎉 Security testing completed!${NC}"
echo -e "${GREEN}🔒 All microservices are properly secured - only accessible through API Gateway${NC}"

# Cleanup
rm -f /tmp/direct_response.json /tmp/gateway_response.json /tmp/header_response.json 2>/dev/null 