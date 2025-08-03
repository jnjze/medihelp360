#!/bin/bash

set -e

echo "🧹 Limpieza Completa de Docker - MediHelp360"
echo "============================================"

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

echo ""
print_info "Paso 1: Parando TODOS los contenedores relacionados"
echo "=================================================="

# Parar todos los contenedores de docker-compose
echo "🛑 Parando docker-compose..."
docker-compose -f docker-compose.jenkins.yml down --remove-orphans 2>/dev/null || true
docker-compose -f docker-compose.jenkins-fixed.yml down --remove-orphans 2>/dev/null || true
docker-compose -f docker-compose.secure.yml down --remove-orphans 2>/dev/null || true

print_success "Docker-compose parado"

echo ""
print_info "Paso 2: Removiendo contenedores problemáticos"
echo "============================================="

# Lista de contenedores a remover
containers_to_remove=(
    "jenkins-medihelp360"
    "jenkins-agent-medihelp360"
    "nginx-proxy-medihelp360"
    "registry-medihelp360"
    "registry-ui-medihelp360"
    "sonarqube-medihelp360"
    "postgres-sonar"
    "local-registry"
    "nginx"
    "jenkins"
    "jenkins-agent"
    "registry"
    "sonarqube"
)

for container in "${containers_to_remove[@]}"; do
    if docker ps -a --format "{{.Names}}" 2>/dev/null | grep -q "^$container$"; then
        echo "🗑️  Removiendo $container..."
        docker rm -f "$container" >/dev/null 2>&1 || true
        print_success "Removido: $container"
    fi
done

# Remover TODOS los contenedores que contengan "medihelp360"
echo "🗑️  Removiendo contenedores con 'medihelp360'..."
docker ps -a --format "{{.Names}}" | grep medihelp360 | xargs -r docker rm -f 2>/dev/null || true

print_success "Contenedores problemáticos removidos"

echo ""
print_info "Paso 3: Limpiando redes Docker"
echo "=============================="

# Remover redes específicas
networks_to_remove=(
    "medihelp360_jenkins-network"
    "medihelp360-jenkins-net"
    "medihelp360_default"
    "jenkins-docker_default"
)

for network in "${networks_to_remove[@]}"; do
    if docker network ls --format "{{.Name}}" | grep -q "^$network$"; then
        echo "🌐 Removiendo red $network..."
        docker network rm "$network" 2>/dev/null || true
        print_success "Red removida: $network"
    fi
done

# Limpiar redes no utilizadas
echo "🌐 Limpiando redes no utilizadas..."
docker network prune -f >/dev/null 2>&1 || true

print_success "Redes limpiadas"

echo ""
print_info "Paso 4: Limpiando volúmenes problemáticos"
echo "========================================"

# Remover volúmenes específicos si existen
volumes_to_check=(
    "jenkins_home"
    "registry_data"
    "sonarqube_data"
    "postgres_sonar_data"
)

for volume in "${volumes_to_check[@]}"; do
    if docker volume ls --format "{{.Name}}" | grep -q "^$volume$"; then
        echo "💾 Volumen encontrado: $volume"
        read -p "¿Remover volumen $volume? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            docker volume rm "$volume" 2>/dev/null || true
            print_success "Volumen removido: $volume"
        else
            print_warning "Volumen conservado: $volume"
        fi
    fi
done

# Limpiar volúmenes no utilizados
echo "💾 Limpiando volúmenes no utilizados..."
docker volume prune -f >/dev/null 2>&1 || true

print_success "Volúmenes procesados"

echo ""
print_info "Paso 5: Limpiando archivos de configuración problemáticos"
echo "======================================================="

# Remover directorios de configuración que pueden causar problemas
dirs_to_clean=(
    "./nginx"
    "./jenkins-config"
    "./jenkins_home"
    "./registry-data"
    "./sonarqube"
)

for dir in "${dirs_to_clean[@]}"; do
    if [ -d "$dir" ]; then
        echo "📁 Encontrado directorio: $dir"
        read -p "¿Remover directorio $dir? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            rm -rf "$dir"
            print_success "Directorio removido: $dir"
        else
            print_warning "Directorio conservado: $dir"
        fi
    fi
done

print_success "Directorios procesados"

echo ""
print_info "Paso 6: Configurando Registry limpio"
echo "=================================="

# Buscar puerto disponible para registry
REGISTRY_PORT=""
for port in 5001 5002 5003 5000; do
    if ! netstat -tuln 2>/dev/null | grep -q ":$port " && ! ss -tuln 2>/dev/null | grep -q ":$port "; then
        REGISTRY_PORT=$port
        break
    fi
done

if [ -z "$REGISTRY_PORT" ]; then
    print_error "No se encontró puerto disponible para registry"
    exit 1
fi

print_info "Creando registry en puerto $REGISTRY_PORT..."

# Crear registry limpio
if docker run -d \
    -p "$REGISTRY_PORT:5000" \
    --name local-registry \
    --restart=unless-stopped \
    -v registry_data:/var/lib/registry \
    registry:2; then
    print_success "Registry creado en puerto $REGISTRY_PORT"
else
    print_error "Error al crear registry"
    exit 1
fi

# Esperar que esté listo
echo "⏰ Esperando registry..."
sleep 10

# Verificar registry
if curl -f -s "http://localhost:$REGISTRY_PORT/v2/" >/dev/null 2>&1; then
    print_success "Registry verificado en puerto $REGISTRY_PORT"
else
    print_error "Registry no responde"
    exit 1
fi

echo ""
print_info "Paso 7: Creando docker-compose simplificado"
echo "=========================================="

# Crear docker-compose mínimo para evitar errores
cat << EOF > docker-compose.simple.yml
version: '3.8'

services:
  jenkins:
            image: jenkins/jenkins:lts
    container_name: jenkins-medihelp360
    restart: unless-stopped
    ports:
      - "8090:8080"
      - "50000:50000"
    volumes:
      - jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
      - ./scripts:/scripts:ro
      - .:/workspace:ro
    environment:
      - DOCKER_HOST=unix:///var/run/docker.sock
      - JENKINS_OPTS="--httpPort=8080"
    user: root
    networks:
      - jenkins-net

volumes:
  jenkins_home:
    driver: local

networks:
  jenkins-net:
    driver: bridge
    name: jenkins-simple-net
EOF

print_success "docker-compose.simple.yml creado"

echo ""
print_info "Paso 8: Actualizando configuraciones"
echo "==================================="

# Actualizar archivos con puerto correcto
files_to_update=(
    "Jenkinsfile"
    "scripts/deploy.sh"
    "scripts/manual-deploy.sh"
    "start-medihelp360.sh"
)

for file in "${files_to_update[@]}"; do
    if [ -f "$file" ]; then
        if sed --version 2>/dev/null | grep -q GNU; then
            sed -i "s/localhost:500[0-9]/localhost:$REGISTRY_PORT/g" "$file"
        else
            sed -i '' "s/localhost:500[0-9]/localhost:$REGISTRY_PORT/g" "$file"
        fi
        print_success "Actualizado: $file"
    fi
done

echo ""
print_info "Paso 9: Limpieza final del sistema Docker"
echo "========================================"

# Limpieza general
echo "🧹 Limpieza general de Docker..."
docker system prune -f >/dev/null 2>&1 || true
docker image prune -f >/dev/null 2>&1 || true

print_success "Sistema Docker limpio"

echo ""
echo "🎉 ¡Limpieza completa terminada!"
echo "==============================="
echo ""
echo "📊 Estado actual:"
echo "   🐳 Registry: http://localhost:$REGISTRY_PORT"
echo "   🧹 Contenedores problemáticos removidos"
echo "   🌐 Redes Docker limpiadas"
echo "   💾 Volúmenes procesados"
echo "   📁 Configuraciones problemáticas removidas"
echo ""
echo "📋 Próximos pasos:"
echo "   1. Iniciar Jenkins: docker-compose -f docker-compose.simple.yml up -d"
echo "   2. Verificar: docker ps"
echo "   3. Acceder Jenkins: http://localhost:8090"
echo "   4. Password: docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword"
echo ""
echo "🔧 Si necesitas el stack completo después:"
echo "   ./start-medihelp360.sh"

# Crear script de inicio rápido
cat << 'EOF' > inicio-rapido.sh
#!/bin/bash

echo "🚀 Inicio Rápido - Solo Jenkins y Registry"
echo "=========================================="

# Verificar registry
if ! curl -f -s http://localhost:REGISTRY_PORT/v2/ >/dev/null 2>&1; then
    echo "🐳 Iniciando registry..."
    docker start local-registry || docker run -d -p REGISTRY_PORT:5000 --name local-registry --restart=unless-stopped -v registry_data:/var/lib/registry registry:2
    sleep 5
fi

# Iniciar Jenkins
echo "🔧 Iniciando Jenkins..."
docker-compose -f docker-compose.simple.yml up -d

echo "⏰ Esperando Jenkins..."
sleep 30

echo "✅ ¡Listo!"
echo "   🔧 Jenkins: http://localhost:8090"
echo "   🐳 Registry: http://localhost:REGISTRY_PORT"
echo "   🔑 Password: docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword"
EOF

# Reemplazar puerto en script
sed "s/REGISTRY_PORT/$REGISTRY_PORT/g" inicio-rapido.sh > inicio-rapido-temp.sh
mv inicio-rapido-temp.sh inicio-rapido.sh
chmod +x inicio-rapido.sh

print_success "Script inicio-rapido.sh creado"

echo ""
print_info "💡 Tip: Para un inicio súper rápido ejecuta: ./inicio-rapido.sh" 