#!/bin/bash

set -e

echo "🐳 Configuración de Registry para Servidor"
echo "=========================================="

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
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

# Función para encontrar puerto disponible
find_available_port() {
    local start_port=${1:-5000}
    local end_port=${2:-5010}
    
    for port in $(seq $start_port $end_port); do
        if ! netstat -tuln 2>/dev/null | grep -q ":$port " && ! ss -tuln 2>/dev/null | grep -q ":$port "; then
            echo $port
            return 0
        fi
    done
    return 1
}

# Función para configurar Docker daemon para registry inseguro
configure_docker_daemon() {
    local registry_port=$1
    
    echo "🔧 Configurando Docker daemon para registry inseguro..."
    
    # Determinar directorio de configuración Docker
    DOCKER_CONFIG_DIR="/etc/docker"
    
    # Crear directorio si no existe
    sudo mkdir -p "$DOCKER_CONFIG_DIR"
    
    # Backup de configuración existente
    if [ -f "$DOCKER_CONFIG_DIR/daemon.json" ]; then
        sudo cp "$DOCKER_CONFIG_DIR/daemon.json" "$DOCKER_CONFIG_DIR/daemon.json.backup.$(date +%Y%m%d_%H%M%S)"
        print_warning "Backup creado de daemon.json existente"
    fi
    
    # Crear o actualizar daemon.json
    cat << EOF | sudo tee "$DOCKER_CONFIG_DIR/daemon.json" > /dev/null
{
  "insecure-registries": [
    "localhost:$registry_port",
    "127.0.0.1:$registry_port",
    "$(hostname -I | awk '{print $1}'):$registry_port"
  ],
  "experimental": false,
  "features": {
    "buildkit": true
  }
}
EOF

    print_success "Configuración Docker actualizada"
    
    # Reiniciar Docker daemon
    echo "🔄 Reiniciando Docker daemon..."
    if sudo systemctl restart docker; then
        print_success "Docker daemon reiniciado"
        sleep 5
    else
        print_error "Error al reiniciar Docker daemon"
        return 1
    fi
}

# Función principal
main() {
    echo "🔍 Verificando sistema..."
    
    # Verificar Docker
    if ! command -v docker >/dev/null 2>&1; then
        print_error "Docker no está instalado"
        echo "Instala Docker primero: https://docs.docker.com/engine/install/"
        exit 1
    fi
    
    # Verificar que Docker esté corriendo
    if ! docker info >/dev/null 2>&1; then
        print_warning "Docker no está corriendo, intentando iniciarlo..."
        if sudo systemctl start docker; then
            print_success "Docker iniciado"
            sleep 3
        else
            print_error "No se pudo iniciar Docker"
            exit 1
        fi
    fi
    
    # Limpiar registry existente
    echo "🧹 Limpiando registry existente..."
    docker rm -f local-registry 2>/dev/null || true
    docker rm -f local-registry-test 2>/dev/null || true
    
    # Encontrar puerto disponible
    echo "🔍 Buscando puerto disponible..."
    AVAILABLE_PORT=$(find_available_port 5000 5010)
    
    if [ -z "$AVAILABLE_PORT" ]; then
        print_error "No se encontró puerto disponible entre 5000-5010"
        exit 1
    fi
    
    print_success "Puerto disponible encontrado: $AVAILABLE_PORT"
    
    # Configurar Docker daemon
    configure_docker_daemon $AVAILABLE_PORT
    
    # Descargar imagen registry si no existe
    echo "📥 Descargando imagen registry..."
    if docker pull registry:2; then
        print_success "Imagen registry descargada"
    else
        print_error "No se pudo descargar imagen registry"
        exit 1
    fi
    
    # Crear registry
    echo "🚀 Creando registry en puerto $AVAILABLE_PORT..."
    if docker run -d \
        -p $AVAILABLE_PORT:5000 \
        --name local-registry \
        --restart=unless-stopped \
        -v registry_data:/var/lib/registry \
        registry:2; then
        print_success "Registry creado exitosamente"
    else
        print_error "Error al crear registry"
        exit 1
    fi
    
    # Esperar que el registry esté listo
    echo "⏰ Esperando que registry esté listo..."
    sleep 10
    
    # Verificar que el registry responde
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s http://localhost:$AVAILABLE_PORT/v2/ >/dev/null 2>&1; then
            print_success "Registry está respondiendo"
            break
        else
            echo "Intento $attempt/$max_attempts - esperando registry..."
            sleep 2
            attempt=$((attempt + 1))
        fi
    done
    
    if [ $attempt -gt $max_attempts ]; then
        print_error "Registry no responde después de $max_attempts intentos"
        exit 1
    fi
    
    # Test de funcionalidad
    echo "🧪 Probando funcionalidad del registry..."
    
    # Descargar imagen de prueba
    docker pull hello-world:latest
    
    # Tag para registry local
    docker tag hello-world:latest localhost:$AVAILABLE_PORT/hello-world:test
    
    # Push al registry
    if docker push localhost:$AVAILABLE_PORT/hello-world:test; then
        print_success "Test de push exitoso"
        
        # Limpiar imagen de prueba
        docker rmi localhost:$AVAILABLE_PORT/hello-world:test
    else
        print_error "Test de push falló"
        exit 1
    fi
    
    # Actualizar configuraciones del proyecto
    echo "🔧 Actualizando configuraciones del proyecto..."
    
    # Lista de archivos a actualizar
    files_to_update=(
        "Jenkinsfile"
        "scripts/deploy.sh"
        "scripts/manual-deploy.sh"
        "start-medihelp360.sh"
        "scripts/configure-docker-registry.sh"
        "GUIA-INICIO-SERVIDOR.md"
        "scripts/setup-jenkins.sh"
        "CI-CD-SETUP.md"
    )
    
    for file in "${files_to_update[@]}"; do
        if [ -f "$file" ]; then
            # Usar sed apropiado para Linux
            if sed --version 2>/dev/null | grep -q GNU; then
                # GNU sed (Linux)
                sed -i "s/localhost:500[0-9]/localhost:$AVAILABLE_PORT/g" "$file"
            else
                # BSD sed (macOS y otros)
                sed -i '' "s/localhost:500[0-9]/localhost:$AVAILABLE_PORT/g" "$file"
            fi
            echo "✅ Actualizado: $file"
        fi
    done
    
    # Crear script de arranque automático
    echo "📄 Creando script de inicio automático..."
    
    cat << EOF > start-registry.sh
#!/bin/bash
# Script para iniciar registry automáticamente

echo "🚀 Iniciando Docker Registry..."

# Verificar si ya está corriendo
if docker ps | grep -q local-registry; then
    echo "✅ Registry ya está corriendo"
    exit 0
fi

# Iniciar registry
if docker start local-registry >/dev/null 2>&1; then
    echo "✅ Registry iniciado"
else
    echo "🚀 Creando nuevo registry..."
    docker run -d \\
        -p $AVAILABLE_PORT:5000 \\
        --name local-registry \\
        --restart=unless-stopped \\
        -v registry_data:/var/lib/registry \\
        registry:2
    echo "✅ Registry creado"
fi

# Verificar
sleep 3
if curl -f http://localhost:$AVAILABLE_PORT/v2/ >/dev/null 2>&1; then
    echo "✅ Registry disponible en http://localhost:$AVAILABLE_PORT"
else
    echo "❌ Registry no responde"
    exit 1
fi
EOF
    
    chmod +x start-registry.sh
    
    # Resumen final
    echo ""
    echo "🎉 ¡Registry configurado exitosamente!"
    echo "======================================"
    echo ""
    echo "📊 Información del Registry:"
    echo "   Puerto: $AVAILABLE_PORT"
    echo "   URL API: http://localhost:$AVAILABLE_PORT/v2/"
    echo "   Contenedor: local-registry"
    echo "   Volumen: registry_data"
    echo ""
    echo "🔧 Configuraciones actualizadas:"
    for file in "${files_to_update[@]}"; do
        if [ -f "$file" ]; then
            echo "   ✅ $file"
        fi
    done
    echo ""
    echo "📋 Comandos útiles:"
    echo "   Ver estado: docker ps | grep registry"
    echo "   Ver logs: docker logs local-registry"
    echo "   Reiniciar: ./start-registry.sh"
    echo "   Verificar: curl http://localhost:$AVAILABLE_PORT/v2/"
    echo ""
    echo "🚀 Próximo paso: ./start-medihelp360.sh"
}

# Ejecutar función principal
main "$@" 