#!/bin/bash

set -e

echo "🚀 MediHelp360 Quick Start"
echo "=========================="
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para print con colores
print_step() {
    echo -e "${BLUE}$1${NC} $2"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 1. Verificar requisitos
print_step "1️⃣" "Checking requirements..."

if ! command -v docker &> /dev/null; then
    print_error "Docker not found. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose not found. Please install Docker Compose first."
    exit 1
fi

if ! command -v git &> /dev/null; then
    print_error "Git not found. Please install Git first."
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    print_warning "Maven not found. Some builds might fail."
fi

print_success "All requirements met"

# 2. Verificar que estamos en el directorio correcto
print_step "2️⃣" "Verifying project structure..."

if [ ! -f "docker-compose.secure.yml" ]; then
    print_error "docker-compose.secure.yml not found. Are you in the correct directory?"
    exit 1
fi

if [ ! -d "scripts" ]; then
    print_error "scripts directory not found. Are you in the correct directory?"
    exit 1
fi

print_success "Project structure verified"

# 3. Hacer scripts ejecutables
print_step "3️⃣" "Making scripts executable..."
chmod +x scripts/*.sh
print_success "Scripts are now executable"

# 4. Configurar registry
print_step "4️⃣" "Configuring Docker registry..."
if [ -f "./scripts/configure-docker-registry.sh" ]; then
    ./scripts/configure-docker-registry.sh || print_warning "Registry configuration had warnings"
else
    print_warning "Registry configuration script not found"
fi

# 5. Iniciar Jenkins y CI/CD infrastructure
print_step "5️⃣" "Starting Jenkins and CI/CD infrastructure..."
docker-compose -f docker-compose.jenkins.yml up -d

print_success "Jenkins infrastructure started"
echo "   🔧 Jenkins will be available at: http://localhost:8090"
echo "   📦 Registry UI at: http://localhost:5001"
echo "   📊 SonarQube at: http://localhost:9000"

# 6. Esperar que Jenkins esté listo
print_step "6️⃣" "Waiting for Jenkins to be ready..."
echo "   This might take 2-3 minutes..."

timeout=180
count=0
while [ $count -lt $timeout ]; do
    if curl -f -s http://localhost:8090/login >/dev/null 2>&1; then
        print_success "Jenkins is ready!"
        break
    fi
    echo -n "."
    sleep 5
    count=$((count + 5))
done

if [ $count -ge $timeout ]; then
    print_warning "Jenkins took longer than expected. Check manually at http://localhost:8090"
fi

# 7. Configurar Jenkins
print_step "7️⃣" "Setting up Jenkins configuration..."
if [ -f "./scripts/setup-jenkins.sh" ]; then
    ./scripts/setup-jenkins.sh || print_warning "Jenkins setup had warnings"
else
    print_warning "Jenkins setup script not found"
fi

# 8. Construir imágenes iniciales
print_step "8️⃣" "Building initial Docker images..."
echo "   This will take several minutes..."

# Build all services
build_service() {
    local service=$1
    local path=$2
    echo "   📦 Building $service..."
    cd "$path"
    docker build -t "localhost:5001/medihelp360-$service:latest" . || {
        print_warning "Failed to build $service"
        cd ..
        return 1
    }
    cd ..
    return 0
}

build_service "api-gateway" "api-gateway"
build_service "user-management-service" "user-management-service"
build_service "database-sync-service-a" "database-sync-service-a"
build_service "database-sync-service-b" "database-sync-service-b"
build_service "database-sync-service-c" "database-sync-service-c"

print_success "Initial images built"

# 9. Push imágenes al registry local
print_step "9️⃣" "Pushing images to local registry..."

if curl -f http://localhost:5001/v2/ >/dev/null 2>&1; then
    docker push localhost:5001/medihelp360-api-gateway:latest || print_warning "Failed to push api-gateway"
    docker push localhost:5001/medihelp360-user-management-service:latest || print_warning "Failed to push user-management-service"
    docker push localhost:5001/medihelp360-database-sync-service-a:latest || print_warning "Failed to push database-sync-service-a"
    docker push localhost:5001/medihelp360-database-sync-service-b:latest || print_warning "Failed to push database-sync-service-b"
    docker push localhost:5001/medihelp360-database-sync-service-c:latest || print_warning "Failed to push database-sync-service-c"
    print_success "Images pushed to registry"
else
    print_warning "Registry not available, using local images"
fi

# 10. Crear ramas si no existen
print_step "🔟" "Creating Git branches..."

# Crear develop branch
git checkout -b develop 2>/dev/null || git checkout develop 2>/dev/null || true
if git push origin develop 2>/dev/null; then
    print_success "develop branch created/updated"
else
    print_warning "Could not push develop branch (might already exist)"
fi

# Crear preprod branch
git checkout main 2>/dev/null || true
git checkout -b preprod 2>/dev/null || git checkout preprod 2>/dev/null || true
if git push origin preprod 2>/dev/null; then
    print_success "preprod branch created/updated"
else
    print_warning "Could not push preprod branch (might already exist)"
fi

# Volver a main
git checkout main 2>/dev/null || true

# 11. Deploy inicial
print_step "1️⃣1️⃣" "Deploying to production environment..."

# Iniciar infraestructura primero
echo "   🗄️ Starting infrastructure services..."
docker-compose -f docker-compose.secure.yml up -d zookeeper kafka consul-server postgres-sync-a mysql-sync-b mongo-sync-c

# Esperar que la infraestructura esté lista
echo "   ⏰ Waiting for infrastructure to be ready..."
sleep 60

# Deploy aplicaciones
echo "   🚀 Deploying applications..."
if [ -f "./scripts/deploy.sh" ]; then
    ./scripts/deploy.sh production || print_warning "Production deployment had issues"
else
    print_warning "Deploy script not found, starting manually"
    docker-compose -f docker-compose.secure.yml up -d
fi

print_success "Production deployment completed"

# 12. Verificación final
print_step "1️⃣2️⃣" "Final verification..."

echo "   🏥 Performing health checks..."
sleep 30

# Health checks
services_ok=0
total_services=5

check_service() {
    local name=$1
    local url=$2
    if curl -f -s "$url" >/dev/null 2>&1; then
        print_success "$name is healthy"
        return 0
    else
        print_warning "$name is not responding"
        return 1
    fi
}

check_service "API Gateway" "http://localhost:8080/actuator/health" && ((services_ok++))
check_service "User Management" "http://localhost:8081/actuator/health" && ((services_ok++))
check_service "Database Sync A" "http://localhost:8082/actuator/health" && ((services_ok++))
check_service "Database Sync B" "http://localhost:8083/actuator/health" && ((services_ok++))
check_service "Database Sync C" "http://localhost:8084/actuator/health" && ((services_ok++))

echo ""
echo "🎉 MediHelp360 Quick Start Completed!"
echo "======================================"
echo ""
echo "📊 Deployment Summary:"
echo "   ✅ Services healthy: $services_ok/$total_services"
if [ $services_ok -eq $total_services ]; then
    echo "   🎯 Status: ALL SYSTEMS OPERATIONAL"
else
    echo "   ⚠️  Status: Some services need attention"
fi
echo ""
echo "🌐 Access URLs:"
echo "   🏭 Production API:  http://localhost:8080"
echo "   🧪 Preprod API:     http://localhost:8180 (deploy manually)"
echo "   💻 Development API: http://localhost:8280 (deploy manually)"
echo "   🔧 Jenkins:         http://localhost:8090"
echo "   🗂️ Consul:          http://localhost:8500"
echo "   📦 Registry UI:     http://localhost:5001"
echo ""
echo "📋 Next Steps:"
echo "   1. Configure Jenkins job at http://localhost:8090"
echo "   2. Get Jenkins password: docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword"
echo "   3. Test auto-deployment by making commits to different branches"
echo "   4. Monitor with: ./scripts/health-check.sh"
echo "   5. Deploy other environments: ./scripts/deploy.sh preprod && ./scripts/deploy.sh development"
echo ""
echo "🚀 Your MediHelp360 CI/CD system is ready!"

# Jenkins password info
echo ""
echo "🔑 Jenkins Initial Password:"
echo "================================"
if docker ps | grep -q jenkins-medihelp360; then
    echo "$(docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo 'Password not available yet')"
else
    echo "Jenkins container not running"
fi
echo ""

# Final reminder
echo "💡 Remember:"
echo "   - Git Polling is active (checks every 5 minutes)"
echo "   - Push to 'main' → deploys to production"
echo "   - Push to 'preprod' → deploys to preprod"
echo "   - Push to 'develop' → deploys to development"
echo ""
echo "Happy coding! 🎉" 