#!/bin/bash

echo "🔍 Diagnóstico de Servidor - MediHelp360"
echo "========================================"

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

echo ""
print_info "1. Información del Sistema"
echo "=========================="
echo "OS: $(uname -a)"
echo "Hostname: $(hostname)"
echo "IP Address: $(hostname -I 2>/dev/null || ifconfig | grep 'inet ' | grep -v '127.0.0.1' | awk '{print $2}' | head -1)"
echo "User: $(whoami)"
echo "Working Directory: $(pwd)"

echo ""
print_info "2. Verificando Puertos en Uso"
echo "============================="

# Verificar puerto 5000
if netstat -tuln 2>/dev/null | grep -q ":5000 "; then
    print_warning "Puerto 5000 está ocupado:"
    netstat -tuln | grep ":5000 " || ss -tuln | grep ":5000 "
    
    # Ver qué proceso usa el puerto
    if command -v lsof >/dev/null 2>&1; then
        echo "Proceso usando puerto 5000:"
        lsof -i :5000 2>/dev/null || echo "No se pudo determinar el proceso"
    fi
else
    print_success "Puerto 5000 está libre"
fi

# Verificar puerto 5001
if netstat -tuln 2>/dev/null | grep -q ":5001 "; then
    print_warning "Puerto 5001 está ocupado:"
    netstat -tuln | grep ":5001 " || ss -tuln | grep ":5001 "
else
    print_success "Puerto 5001 está libre"
fi

echo ""
print_info "3. Verificando Docker"
echo "===================="

if command -v docker >/dev/null 2>&1; then
    print_success "Docker está instalado: $(docker --version)"
    
    # Estado del daemon Docker
    if docker info >/dev/null 2>&1; then
        print_success "Docker daemon está corriendo"
    else
        print_error "Docker daemon no está corriendo o no hay permisos"
        echo "Intenta: sudo systemctl start docker"
    fi
    
    # Verificar contenedores registry existentes
    echo ""
    echo "Contenedores registry existentes:"
    docker ps -a | grep registry || echo "No hay contenedores registry"
    
else
    print_error "Docker no está instalado"
fi

echo ""
print_info "4. Verificando Docker Compose"
echo "============================="

if command -v docker-compose >/dev/null 2>&1; then
    print_success "Docker Compose está instalado: $(docker-compose --version)"
else
    print_error "Docker Compose no está instalado"
fi

echo ""
print_info "5. Verificando Conectividad de Red"
echo "=================================="

# Test de conectividad hacia fuera
if ping -c 1 8.8.8.8 >/dev/null 2>&1; then
    print_success "Conectividad a Internet OK"
else
    print_warning "Sin conectividad a Internet o ping bloqueado"
fi

# Test de resolución DNS
if nslookup docker.io >/dev/null 2>&1; then
    print_success "Resolución DNS OK"
else
    print_warning "Problemas con resolución DNS"
fi

echo ""
print_info "6. Verificando Registry Docker Hub"
echo "================================="

# Intentar pull de imagen registry
if docker pull registry:2 >/dev/null 2>&1; then
    print_success "Puede descargar imagen registry:2"
else
    print_warning "No se pudo descargar imagen registry:2"
fi

echo ""
print_info "7. Intentando Crear Registry Local"
echo "=================================="

# Limpiar registry existente
docker rm -f local-registry 2>/dev/null || true

# Intentar puerto 5000 primero
echo "Intentando puerto 5000..."
if docker run -d -p 5000:5000 --name local-registry-test --restart=unless-stopped registry:2 >/dev/null 2>&1; then
    print_success "Registry funcionando en puerto 5000"
    
    # Test básico
    sleep 3
    if curl -f -s http://localhost:5000/v2/ >/dev/null 2>&1; then
        print_success "Registry responde correctamente en puerto 5000"
        echo "RECOMENDACIÓN: Usar puerto 5000"
        RECOMMENDED_PORT=5000
    else
        print_warning "Registry no responde en puerto 5000"
    fi
    
    docker rm -f local-registry-test >/dev/null 2>&1
else
    print_warning "No se pudo crear registry en puerto 5000"
    
    # Intentar puerto 5001
    echo "Intentando puerto 5001..."
    if docker run -d -p 5001:5000 --name local-registry-test --restart=unless-stopped registry:2 >/dev/null 2>&1; then
        print_success "Registry funcionando en puerto 5001"
        
        # Test básico
        sleep 3
        if curl -f -s http://localhost:5001/v2/ >/dev/null 2>&1; then
            print_success "Registry responde correctamente en puerto 5001"
            echo "RECOMENDACIÓN: Usar puerto 5001"
            RECOMMENDED_PORT=5001
        else
            print_warning "Registry no responde en puerto 5001"
        fi
        
        docker rm -f local-registry-test >/dev/null 2>&1
    else
        print_error "No se pudo crear registry en puerto 5001"
        
        # Intentar puerto 5002
        echo "Intentando puerto 5002..."
        if docker run -d -p 5002:5000 --name local-registry-test --restart=unless-stopped registry:2 >/dev/null 2>&1; then
            print_success "Registry funcionando en puerto 5002"
            
            sleep 3
            if curl -f -s http://localhost:5002/v2/ >/dev/null 2>&1; then
                print_success "Registry responde correctamente en puerto 5002"
                echo "RECOMENDACIÓN: Usar puerto 5002"
                RECOMMENDED_PORT=5002
            fi
            
            docker rm -f local-registry-test >/dev/null 2>&1
        else
            print_error "No se pudo crear registry en ningún puerto"
        fi
    fi
fi

echo ""
print_info "8. Verificando Permisos"
echo "======================"

# Verificar si está en grupo docker
if groups | grep -q docker; then
    print_success "Usuario está en grupo docker"
else
    print_warning "Usuario NO está en grupo docker"
    echo "Ejecuta: sudo usermod -aG docker \$USER && newgrp docker"
fi

# Verificar sudo
if sudo -n true 2>/dev/null; then
    print_success "Tiene permisos sudo sin password"
elif sudo -l >/dev/null 2>&1; then
    print_warning "Tiene permisos sudo con password"
else
    print_warning "No tiene permisos sudo"
fi

echo ""
print_info "9. Configuración Recomendada"
echo "============================"

if [ -n "$RECOMMENDED_PORT" ]; then
    echo "🎯 Puerto recomendado para registry: $RECOMMENDED_PORT"
    echo ""
    echo "💡 Comandos sugeridos:"
    echo ""
    echo "# 1. Crear registry permanente"
    echo "docker run -d -p $RECOMMENDED_PORT:5000 --name local-registry --restart=unless-stopped registry:2"
    echo ""
    echo "# 2. Actualizar configuraciones"
    echo "sed -i 's/localhost:500[0-9]/localhost:$RECOMMENDED_PORT/g' Jenkinsfile"
    echo "sed -i 's/localhost:500[0-9]/localhost:$RECOMMENDED_PORT/g' scripts/*.sh"
    echo "sed -i 's/localhost:500[0-9]/localhost:$RECOMMENDED_PORT/g' *.sh"
    echo ""
    echo "# 3. Verificar registry"
    echo "curl http://localhost:$RECOMMENDED_PORT/v2/"
    echo ""
    echo "# 4. Continuar con setup"
    echo "./start-medihelp360.sh"
else
    print_error "No se pudo determinar un puerto válido para el registry"
    echo ""
    echo "💡 Soluciones alternativas:"
    echo "1. Verificar que Docker esté corriendo: sudo systemctl start docker"
    echo "2. Agregar usuario a grupo docker: sudo usermod -aG docker \$USER"
    echo "3. Reiniciar sesión o ejecutar: newgrp docker"
    echo "4. Verificar firewall: sudo ufw status"
    echo "5. Liberar puertos en uso si es posible"
fi

echo ""
echo "📋 RESUMEN DEL DIAGNÓSTICO"
echo "=========================="
echo "Fecha: $(date)"
echo "Servidor: $(hostname)"
echo "Usuario: $(whoami)"
echo "Docker: $(docker --version 2>/dev/null || echo 'No instalado')"
echo "Puerto sugerido: ${RECOMMENDED_PORT:-'No determinado'}"
echo ""
echo "🔧 Para ayuda específica, comparte esta salida." 