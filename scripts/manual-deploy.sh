#!/bin/bash

set -e

echo "🚀 Manual Deploy Script para MediHelp360"
echo "========================================="

# Configuración
REPO_URL=${1:-"https://github.com/tu-usuario/medihelp360.git"}
BRANCH=${2:-"main"}
ENVIRONMENT=${3:-"auto"}  # auto detecta del branch
FORCE_REBUILD=${4:-"false"}
BUILD_TAG=$(date +%Y%m%d_%H%M%S)

# Auto-detectar environment desde branch si no se especifica
if [ "$ENVIRONMENT" = "auto" ]; then
    case $BRANCH in
        "main") ENVIRONMENT="production" ;;
        "preprod") ENVIRONMENT="preprod" ;;
        "develop") ENVIRONMENT="development" ;;
        *) ENVIRONMENT="development" ;;
    esac
fi

echo "📦 Configuración:"
echo "   Repository: $REPO_URL"
echo "   Branch: $BRANCH"
echo "   Environment: $ENVIRONMENT"
echo "   Build Tag: $BUILD_TAG"
echo "   Force Rebuild: $FORCE_REBUILD"

# Función para verificar si hay cambios
check_for_changes() {
    echo "🔍 Verificando cambios en el repositorio..."
    
    # Si no existe el directorio .git, es la primera vez
    if [ ! -d ".git" ]; then
        echo "📁 Primera vez - clonando repositorio..."
        return 0
    fi
    
    # Obtener último commit local
    LOCAL_COMMIT=$(git rev-parse HEAD 2>/dev/null || echo "none")
    
    # Obtener último commit remoto
    git fetch origin $BRANCH 2>/dev/null || {
        echo "⚠️  No se pudo conectar al repositorio remoto"
        return 0
    }
    REMOTE_COMMIT=$(git rev-parse origin/$BRANCH 2>/dev/null || echo "none")
    
    echo "🔄 Local commit:  $LOCAL_COMMIT"
    echo "🔄 Remote commit: $REMOTE_COMMIT"
    
    if [ "$LOCAL_COMMIT" = "$REMOTE_COMMIT" ] && [ "$FORCE_REBUILD" != "true" ]; then
        echo "✅ No hay cambios nuevos. Saltando build."
        echo "💡 Usa 'force' como cuarto parámetro para forzar rebuild:"
        echo "   ./scripts/manual-deploy.sh $REPO_URL $BRANCH $ENVIRONMENT force"
        return 1
    else
        echo "📝 Hay cambios nuevos. Procediendo con deploy..."
        return 0
    fi
}

# Función para backup actual
backup_current_deployment() {
    echo "💾 Creando backup del deployment actual..."
    
    BACKUP_DIR="/tmp/medihelp360-backup-$ENVIRONMENT-$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"
    
    # Determinar archivo compose según environment
    case $ENVIRONMENT in
        "production") COMPOSE_FILE="docker-compose.secure.yml" ;;
        "preprod") COMPOSE_FILE="docker-compose.preprod.yml" ;;
        "development") COMPOSE_FILE="docker-compose.dev.yml" ;;
        *) COMPOSE_FILE="docker-compose.secure.yml" ;;
    esac
    
    # Backup de configuraciones
    cp "$COMPOSE_FILE" "$BACKUP_DIR/" 2>/dev/null || true
    
    # Backup del estado de contenedores
    docker-compose -f "$COMPOSE_FILE" ps > "$BACKUP_DIR/containers-status.txt" 2>/dev/null || true
    
    # Backup de imágenes actuales
    docker images | grep medihelp360 > "$BACKUP_DIR/current-images.txt" 2>/dev/null || true
    
    echo "✅ Backup creado en: $BACKUP_DIR"
    echo "$BACKUP_DIR" > ".last-backup-path-$ENVIRONMENT"
}

# Función para rollback
rollback_deployment() {
    echo "🔄 Ejecutando rollback para $ENVIRONMENT..."
    
    if [ -f ".last-backup-path-$ENVIRONMENT" ]; then
        BACKUP_PATH=$(cat ".last-backup-path-$ENVIRONMENT")
        if [ -d "$BACKUP_PATH" ]; then
            echo "📂 Restaurando desde: $BACKUP_PATH"
            
            # Determinar archivo compose
            case $ENVIRONMENT in
                "production") COMPOSE_FILE="docker-compose.secure.yml" ;;
                "preprod") COMPOSE_FILE="docker-compose.preprod.yml" ;;
                "development") COMPOSE_FILE="docker-compose.dev.yml" ;;
                *) COMPOSE_FILE="docker-compose.secure.yml" ;;
            esac
            
            # Parar servicios actuales
            docker-compose -f "$COMPOSE_FILE" down || true
            
            # Restaurar configuración si existe
            if [ -f "$BACKUP_PATH/$COMPOSE_FILE" ]; then
                cp "$BACKUP_PATH/$COMPOSE_FILE" ./
                echo "✅ Configuración restaurada"
            fi
            
            # Reiniciar con configuración anterior
            docker-compose -f "$COMPOSE_FILE" up -d
            
            echo "✅ Rollback completado para $ENVIRONMENT"
            return 0
        fi
    fi
    
    echo "❌ No se pudo encontrar backup para rollback de $ENVIRONMENT"
    return 1
}

# Función principal de deploy
deploy_application() {
    echo "🚀 Iniciando deployment para $ENVIRONMENT..."
    
    # Crear backup antes de cambios
    backup_current_deployment
    
    # Clonar o actualizar repositorio
    if [ ! -d ".git" ]; then
        echo "📂 Clonando repositorio..."
        git clone "$REPO_URL" temp-repo
        cd temp-repo
        git checkout "$BRANCH"
        # Mover contenido a directorio actual
        mv * ../ 2>/dev/null || true
        mv .* ../ 2>/dev/null || true
        cd ..
        rm -rf temp-repo
    else
        echo "🔄 Actualizando repositorio existente..."
        git fetch origin
        git checkout "$BRANCH"
        git reset --hard origin/"$BRANCH"
    fi
    
    # Construir imágenes con tags específicos por environment
    echo "🏗️  Construyendo imágenes Docker para $ENVIRONMENT..."
    
    ENV_TAG="$ENVIRONMENT-latest"
    
    echo "📦 Building API Gateway..."
    cd api-gateway && docker build -t localhost:5001/medihelp360-api-gateway:$BUILD_TAG . && docker build -t localhost:5001/medihelp360-api-gateway:$ENV_TAG . && cd ..
    
    echo "📦 Building User Management Service..."
    cd user-management-service && docker build -t localhost:5001/medihelp360-user-management-service:$BUILD_TAG . && docker build -t localhost:5001/medihelp360-user-management-service:$ENV_TAG . && cd ..
    
    echo "📦 Building Database Sync Service A..."
    cd database-sync-service-a && docker build -t localhost:5001/medihelp360-database-sync-service-a:$BUILD_TAG . && docker build -t localhost:5001/medihelp360-database-sync-service-a:$ENV_TAG . && cd ..
    
    echo "📦 Building Database Sync Service B..."
    cd database-sync-service-b && docker build -t localhost:5001/medihelp360-database-sync-service-b:$BUILD_TAG . && docker build -t localhost:5001/medihelp360-database-sync-service-b:$ENV_TAG . && cd ..
    
    echo "📦 Building Database Sync Service C..."
    cd database-sync-service-c && docker build -t localhost:5001/medihelp360-database-sync-service-c:$BUILD_TAG . && docker build -t localhost:5001/medihelp360-database-sync-service-c:$ENV_TAG . && cd ..
    
    # Push al registry local si está disponible
    if curl -f http://localhost:5001/v2/ >/dev/null 2>&1; then
        echo "📤 Pushing images to local registry..."
        docker push localhost:5001/medihelp360-api-gateway:$BUILD_TAG
        docker push localhost:5001/medihelp360-api-gateway:$ENV_TAG
        docker push localhost:5001/medihelp360-user-management-service:$BUILD_TAG
        docker push localhost:5001/medihelp360-user-management-service:$ENV_TAG
        docker push localhost:5001/medihelp360-database-sync-service-a:$BUILD_TAG
        docker push localhost:5001/medihelp360-database-sync-service-a:$ENV_TAG
        docker push localhost:5001/medihelp360-database-sync-service-b:$BUILD_TAG
        docker push localhost:5001/medihelp360-database-sync-service-b:$ENV_TAG
        docker push localhost:5001/medihelp360-database-sync-service-c:$BUILD_TAG
        docker push localhost:5001/medihelp360-database-sync-service-c:$ENV_TAG
    else
        echo "⚠️  Registry no disponible - usando imágenes locales"
    fi
    
    # Ejecutar deployment
    echo "🚀 Desplegando servicios en $ENVIRONMENT..."
    export BUILD_TAG=$BUILD_TAG
    export ENVIRONMENT=$ENVIRONMENT
    
    if [ -f "./scripts/deploy.sh" ]; then
        ./scripts/deploy.sh "$ENVIRONMENT"
    else
        echo "⚠️  Script deploy.sh no encontrado - deployment manual"
        
        # Determinar archivo compose
        case $ENVIRONMENT in
            "production") COMPOSE_FILE="docker-compose.secure.yml" ;;
            "preprod") COMPOSE_FILE="docker-compose.preprod.yml" ;;
            "development") COMPOSE_FILE="docker-compose.dev.yml" ;;
            *) COMPOSE_FILE="docker-compose.secure.yml" ;;
        esac
        
        docker-compose -f "$COMPOSE_FILE" down
        docker-compose -f "$COMPOSE_FILE" up -d
    fi
    
    # Verificar health
    echo "🏥 Verificando health de servicios en $ENVIRONMENT..."
    if [ -f "./scripts/wait-for-health.sh" ]; then
        if ./scripts/wait-for-health.sh; then
            echo "✅ Deployment exitoso en $ENVIRONMENT!"
        else
            echo "❌ Health check falló - considerando rollback..."
            read -p "¿Ejecutar rollback? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                rollback_deployment
            fi
        fi
    else
        echo "⚠️  Script de health check no encontrado"
        sleep 30
        
        # Determinar archivo compose
        case $ENVIRONMENT in
            "production") COMPOSE_FILE="docker-compose.secure.yml" ;;
            "preprod") COMPOSE_FILE="docker-compose.preprod.yml" ;;
            "development") COMPOSE_FILE="docker-compose.dev.yml" ;;
            *) COMPOSE_FILE="docker-compose.secure.yml" ;;
        esac
        
        docker-compose -f "$COMPOSE_FILE" ps
    fi
}

# Función para mostrar ayuda
show_help() {
    echo "Manual Deploy Script para MediHelp360"
    echo ""
    echo "Uso:"
    echo "  $0 [repo_url] [branch] [environment] [force]"
    echo ""
    echo "Parámetros:"
    echo "  repo_url      - URL del repositorio Git (default: GitHub repo)"
    echo "  branch        - Branch a desplegar (default: main)"
    echo "  environment   - Ambiente destino (auto|production|preprod|development)"
    echo "  force         - Forzar rebuild aunque no haya cambios (default: false)"
    echo ""
    echo "Ambientes:"
    echo "  auto          - Auto-detecta desde branch (main→production, preprod→preprod, develop→development)"
    echo "  production    - Ambiente de producción (puertos 8080-8084)"
    echo "  preprod       - Ambiente pre-productivo (puertos 8180-8184)"
    echo "  development   - Ambiente de desarrollo (puertos 8280-8284)"
    echo ""
    echo "Ejemplos:"
    echo "  $0                                                    # Deploy branch main a producción"
    echo "  $0 https://github.com/user/repo.git                  # Deploy desde repo específico"
    echo "  $0 https://github.com/user/repo.git develop          # Deploy branch develop a development"
    echo "  $0 https://github.com/user/repo.git preprod preprod  # Deploy branch preprod a preprod"
    echo "  $0 . main production force                            # Forzar rebuild en producción"
    echo ""
    echo "Comandos especiales:"
    echo "  $0 rollback [environment]                             # Ejecutar rollback"
    echo "  $0 status [environment]                               # Mostrar estado actual"
    echo "  $0 help                                               # Mostrar esta ayuda"
}

# Función para mostrar estado
show_status() {
    local env=${1:-"production"}
    
    echo "📊 Estado actual de MediHelp360 - $env:"
    echo "========================================"
    
    # Determinar archivo compose y puertos
    case $env in
        "production")
            COMPOSE_FILE="docker-compose.secure.yml"
            API_PORT=8080
            ;;
        "preprod")
            COMPOSE_FILE="docker-compose.preprod.yml"
            API_PORT=8180
            ;;
        "development")
            COMPOSE_FILE="docker-compose.dev.yml"
            API_PORT=8280
            ;;
        *)
            echo "❌ Environment '$env' no válido"
            return 1
            ;;
    esac
    
    echo ""
    echo "🐳 Contenedores ($env):"
    docker-compose -f "$COMPOSE_FILE" ps 2>/dev/null || echo "❌ No se pudo obtener estado de contenedores"
    
    echo ""
    echo "🏥 Health check rápido ($env):"
    if curl -f http://localhost:$API_PORT/actuator/health >/dev/null 2>&1; then
        echo "✅ API Gateway está respondiendo en puerto $API_PORT"
    else
        echo "❌ API Gateway no responde en puerto $API_PORT"
    fi
    
    echo ""
    echo "📦 Imágenes locales ($env):"
    docker images | grep medihelp360 | grep "$env\|latest" || echo "❌ No se encontraron imágenes de MediHelp360 para $env"
    
    echo ""
    echo "🔍 Último commit local:"
    if [ -d ".git" ]; then
        git log -1 --oneline
    else
        echo "❌ No es un repositorio Git"
    fi
}

# Menú principal
case "${1:-deploy}" in
    "help"|"-h"|"--help")
        show_help
        ;;
    "status")
        show_status "${2:-production}"
        ;;
    "rollback")
        ENVIRONMENT="${2:-production}"
        rollback_deployment
        ;;
    *)
        # Verificar si hay cambios (a menos que sea force)
        if check_for_changes; then
            deploy_application
        fi
        ;;
esac

echo ""
echo "🎉 Script completado para $ENVIRONMENT!" 