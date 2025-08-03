#!/bin/bash

set -e

echo "🔧 Solucionando Jenkins en Servidor"
echo "==================================="

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
print_info "Paso 1: Parando contenedores problemáticos"
echo "=========================================="

# Parar docker-compose que está fallando
docker-compose -f docker-compose.jenkins.yml down --remove-orphans 2>/dev/null || true
docker-compose down --remove-orphans 2>/dev/null || true

# Remover contenedores específicos del error
containers_to_remove=(
    "jenkins-medihelp360"
    "docker-registry-medihelp360"
    "registry-ui-medihelp360"
)

for container in "${containers_to_remove[@]}"; do
    if docker ps -a --format "{{.Names}}" | grep -q "^$container$"; then
        echo "🗑️  Removiendo $container..."
        docker rm -f "$container" >/dev/null 2>&1 || true
        print_success "Removido: $container"
    fi
done

echo ""
print_info "Paso 2: Verificando Registry existente"
echo "====================================="

# Buscar registry funcionando
REGISTRY_PORT=""
for container in $(docker ps --format "{{.Names}}"); do
    if docker ps --filter "name=$container" --format "{{.Image}}" | grep -q "registry"; then
        REGISTRY_PORT=$(docker port "$container" 5000/tcp 2>/dev/null | cut -d: -f2)
        if [ -n "$REGISTRY_PORT" ]; then
            print_success "Registry encontrado en puerto $REGISTRY_PORT"
            break
        fi
    fi
done

if [ -z "$REGISTRY_PORT" ]; then
    print_warning "Registry no encontrado, creando nuevo..."
    
    # Buscar puerto disponible
    for port in 5001 5002 5003 5000; do
        if ! netstat -tuln 2>/dev/null | grep -q ":$port " && ! ss -tuln 2>/dev/null | grep -q ":$port "; then
            REGISTRY_PORT=$port
            break
        fi
    done
    
    # Crear registry
    docker run -d \
        -p "$REGISTRY_PORT:5000" \
        --name local-registry \
        --restart=unless-stopped \
        -v registry_data:/var/lib/registry \
        registry:2
    
    sleep 5
    print_success "Registry creado en puerto $REGISTRY_PORT"
fi

# Verificar registry
if curl -f -s "http://localhost:$REGISTRY_PORT/v2/" >/dev/null 2>&1; then
    print_success "Registry verificado en puerto $REGISTRY_PORT"
else
    print_error "Registry no responde"
    exit 1
fi

echo ""
print_info "Paso 3: Creando Jenkins manualmente (sin docker-compose)"
echo "======================================================"

# Verificar si Jenkins ya existe
if docker ps -a --format "{{.Names}}" | grep -q "^jenkins-medihelp360$"; then
    print_info "Jenkins existe, removiendo para recrear..."
    docker rm -f jenkins-medihelp360 >/dev/null 2>&1 || true
fi

# Crear red si no existe
if ! docker network ls --format "{{.Name}}" | grep -q "^jenkins-net$"; then
    docker network create jenkins-net
    print_success "Red jenkins-net creada"
fi

# Crear Jenkins sin docker-compose
print_info "Creando Jenkins manualmente..."

docker run -d \
    --name jenkins-medihelp360 \
    --restart=unless-stopped \
    -p 8090:8080 \
    -p 50000:50000 \
    -v jenkins_home:/var/jenkins_home \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v "$(pwd)/scripts:/scripts:ro" \
    -v "$(pwd):/workspace:ro" \
    -e DOCKER_HOST=unix:///var/run/docker.sock \
    -e JENKINS_OPTS="--httpPort=8080" \
    --user root \
    --network jenkins-net \
    jenkins/jenkins:2.426.2-lts

if [ $? -eq 0 ]; then
    print_success "Jenkins creado exitosamente"
else
    print_error "Error al crear Jenkins"
    exit 1
fi

echo ""
print_info "Paso 4: Esperando que Jenkins esté listo"
echo "======================================="

echo "⏰ Esperando Jenkins (puede tomar 2-3 minutos)..."

# Esperar Jenkins
max_attempts=60
attempt=1

while [ $attempt -le $max_attempts ]; do
    if curl -f -s http://localhost:8090/login >/dev/null 2>&1; then
        print_success "Jenkins está respondiendo!"
        break
    else
        echo -n "."
        sleep 5
        attempt=$((attempt + 1))
    fi
done

if [ $attempt -gt $max_attempts ]; then
    print_error "Jenkins no responde después de 5 minutos"
    echo "Ver logs: docker logs jenkins-medihelp360"
    exit 1
fi

echo ""
print_info "Paso 5: Configurando Docker daemon para registry"
echo "=============================================="

# Configurar daemon.json
DOCKER_CONFIG_DIR="/etc/docker"

if [ -w "$DOCKER_CONFIG_DIR" ] || sudo test -w "$DOCKER_CONFIG_DIR" 2>/dev/null; then
    print_info "Configurando daemon.json..."
    
    # Backup
    if [ -f "$DOCKER_CONFIG_DIR/daemon.json" ]; then
        sudo cp "$DOCKER_CONFIG_DIR/daemon.json" "$DOCKER_CONFIG_DIR/daemon.json.backup.$(date +%Y%m%d_%H%M%S)" 2>/dev/null || true
    fi
    
    # Crear configuración
    cat << EOF | sudo tee "$DOCKER_CONFIG_DIR/daemon.json" > /dev/null
{
  "insecure-registries": [
    "localhost:$REGISTRY_PORT",
    "127.0.0.1:$REGISTRY_PORT"
  ],
  "experimental": false,
  "features": {
    "buildkit": true
  }
}
EOF
    
    print_success "daemon.json configurado"
    
    # Reiniciar Docker
    if sudo systemctl restart docker 2>/dev/null; then
        print_success "Docker daemon reiniciado"
        sleep 10
        
        # Reiniciar contenedores después del restart
        docker start local-registry >/dev/null 2>&1 || true
        docker start jenkins-medihelp360 >/dev/null 2>&1 || true
        sleep 5
    fi
else
    print_warning "No se puede configurar daemon.json automáticamente"
fi

echo ""
print_info "Paso 6: Creando configuración de job Jenkins"
echo "==========================================="

# Crear directorio para configuración
mkdir -p jenkins-config

# Crear XML de configuración del job
cat << 'EOF' > jenkins-config/medihelp360-pipeline.xml
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <actions/>
  <description>MediHelp360 CI/CD Pipeline - Multi-ambiente</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
      <triggers>
        <hudson.triggers.SCMTrigger>
          <spec>H/5 * * * *</spec>
          <ignorePostCommitHooks>false</ignorePostCommitHooks>
        </hudson.triggers.SCMTrigger>
      </triggers>
    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.90">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.8.3">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/TU_USUARIO/medihelp360.git</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
        <hudson.plugins.git.BranchSpec>
          <name>*/develop</name>
        </hudson.plugins.git.BranchSpec>
        <hudson.plugins.git.BranchSpec>
          <name>*/preprod</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF

print_success "Configuración de job creada"

echo ""
print_info "Paso 7: Obteniendo password de Jenkins"
echo "====================================="

sleep 5

if docker exec jenkins-medihelp360 test -f /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null; then
    JENKINS_PASSWORD=$(docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null)
    if [ -n "$JENKINS_PASSWORD" ]; then
        print_success "Password de Jenkins obtenido"
        echo "🔑 Password: $JENKINS_PASSWORD"
    else
        print_warning "Password no disponible aún, espera 1-2 minutos más"
    fi
else
    print_warning "Password no disponible aún, Jenkins aún está inicializando"
fi

echo ""
echo "🎉 ¡Jenkins configurado exitosamente!"
echo "===================================="
echo ""
echo "📊 Estado final:"
echo "   🐳 Registry: http://localhost:$REGISTRY_PORT"
echo "   🔧 Jenkins: http://localhost:8090"
echo "   🔑 Password: ${JENKINS_PASSWORD:-'Ejecutar: docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword'}"
echo ""
echo "📋 Próximos pasos:"
echo "   1. Abrir Jenkins: http://TU_SERVIDOR_IP:8090"
echo "   2. Usar password mostrado arriba"
echo "   3. Instalar plugins sugeridos"
echo "   4. Crear usuario admin"
echo "   5. Crear job usando: jenkins-config/medihelp360-pipeline.xml"
echo ""
echo "🚀 Para continuar con el setup completo:"
echo "   ./start-medihelp360.sh"

# Crear script de verificación
cat << EOF > verificar-jenkins.sh
#!/bin/bash

echo "🔍 Verificación rápida del sistema"
echo "================================="

echo ""
echo "🐳 Contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "🏥 Health checks:"
echo -n "Registry: "
curl -f -s http://localhost:$REGISTRY_PORT/v2/ >/dev/null 2>&1 && echo "✅ OK" || echo "❌ FAIL"

echo -n "Jenkins: "
curl -f -s http://localhost:8090/login >/dev/null 2>&1 && echo "✅ OK" || echo "❌ FAIL"

echo ""
echo "🔑 Jenkins Password:"
docker exec jenkins-medihelp360 cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "No disponible aún"

echo ""
echo "🌐 URLs:"
echo "   Jenkins: http://\$(hostname -I | awk '{print \$1}'):8090"
echo "   Registry: http://localhost:$REGISTRY_PORT"
EOF

chmod +x verificar-jenkins.sh
print_success "Script verificar-jenkins.sh creado"

echo ""
print_info "💡 Para verificar estado: ./verificar-jenkins.sh" 