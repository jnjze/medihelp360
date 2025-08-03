#!/bin/bash

set -e

echo "🔍 Verificación de Versión Jenkins"
echo "================================="

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Verificar si Jenkins está corriendo
if ! docker ps --format "{{.Names}}" | grep -q "jenkins-medihelp360"; then
    echo "❌ Jenkins no está corriendo"
    echo "💡 Ejecuta: ./scripts/fix-jenkins-servidor.sh"
    exit 1
fi

print_info "Jenkins está corriendo, obteniendo información..."

echo ""
echo "📋 Información del Contenedor:"
echo "=============================="

# Información básica del contenedor
docker ps --filter "name=jenkins-medihelp360" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | head -2

echo ""
echo "🏷️  Imagen Docker:"
echo "=================="
JENKINS_IMAGE=$(docker inspect jenkins-medihelp360 --format='{{.Config.Image}}')
echo "Imagen: $JENKINS_IMAGE"

echo ""
echo "📊 Versión de Jenkins:"
echo "====================="

# Esperar que Jenkins esté completamente listo
echo "⏰ Esperando que Jenkins API esté disponible..."
max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if curl -f -s http://localhost:8090/api/json >/dev/null 2>&1; then
        break
    else
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    fi
done

if [ $attempt -gt $max_attempts ]; then
    echo "⚠️  Jenkins API no responde, obteniendo versión desde logs..."
    JENKINS_VERSION=$(docker logs jenkins-medihelp360 2>&1 | grep -i "jenkins.*version" | head -1 || echo "No disponible")
    echo "Versión (desde logs): $JENKINS_VERSION"
else
    # Obtener versión desde API
    JENKINS_VERSION=$(curl -s http://localhost:8090/api/json | grep -o '"version":"[^"]*"' | cut -d'"' -f4 2>/dev/null || echo "No disponible")
    print_success "Versión Jenkins: $JENKINS_VERSION"
    
    # Información adicional
    echo ""
    echo "🔧 Información Adicional:"
    echo "========================"
    
    # Obtener información del sistema
    JAVA_VERSION=$(docker exec jenkins-medihelp360 java -version 2>&1 | head -1 || echo "No disponible")
    echo "Java: $JAVA_VERSION"
    
    # Plugins instalados (solo contar)
    PLUGINS_COUNT=$(curl -s http://localhost:8090/pluginManager/api/json?depth=1 2>/dev/null | grep -o '"plugins":\[' | wc -l || echo "0")
    echo "Plugins instalados: $PLUGINS_COUNT"
fi

echo ""
echo "🌐 URLs de Acceso:"
echo "=================="
echo "Jenkins UI: http://localhost:8090"
echo "Jenkins API: http://localhost:8090/api/"

# Verificar si es primera vez
if docker exec jenkins-medihelp360 test -f /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null; then
    echo ""
    echo "🔑 Configuración Inicial:"
    echo "========================"
    INITIAL_PASSWORD=$(docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null)
    echo "Password inicial: $INITIAL_PASSWORD"
    echo "⚠️  Este es el primer inicio, necesitas configurar Jenkins"
else
    print_success "Jenkins ya está configurado"
fi

echo ""
echo "📈 Estado del Sistema:"
echo "====================="
echo -n "Estado Jenkins: "
curl -f -s http://localhost:8090/login >/dev/null 2>&1 && echo "✅ OK" || echo "❌ No responde"

echo -n "Estado API: "
curl -f -s http://localhost:8090/api/json >/dev/null 2>&1 && echo "✅ OK" || echo "❌ No responde"

echo ""
print_success "Verificación completada" 