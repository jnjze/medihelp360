#!/bin/bash

# Script para alternar acceso externo a bases de datos
# Uso: ./scripts/toggle-database-access.sh [enable|disable]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

print_header() {
    echo "=========================================="
    echo "🔒 MEDIHELP360 - Database Access Toggle"
    echo "=========================================="
}

print_status() {
    echo ""
    echo "📊 Estado actual de puertos de base de datos:"
    echo ""
    
    if docker ps --format "table {{.Names}}\t{{.Ports}}" | grep -E "(postgres|mysql|mongo)" | grep -q "0.0.0.0"; then
        echo "✅ HABILITADO - Las bases de datos son accesibles externamente"
        echo ""
        echo "📋 Conexiones disponibles:"
        echo "   PostgreSQL (users):     localhost:5432"
        echo "   PostgreSQL (sync-a):    localhost:5433" 
        echo "   MySQL (sync-b):         localhost:3306"
        echo "   MongoDB (sync-c):       localhost:27017"
        echo ""
        echo "🔑 Credenciales:"
        echo "   PostgreSQL users:   users_user / users_pass"
        echo "   PostgreSQL sync-a:  sync_user_a / sync_pass_a"
        echo "   MySQL sync-b:       sync_user_b / sync_pass_b"
        echo "   MongoDB sync-c:     sync_user_c / sync_pass_c"
    else
        echo "🔒 DESHABILITADO - Las bases de datos solo son accesibles internamente"
        echo "   Para habilitar acceso externo: ./scripts/toggle-database-access.sh enable"
    fi
    echo ""
}

enable_external_access() {
    echo "🔧 Habilitando acceso externo a bases de datos..."
    echo ""
    
    # Detener servicios actuales
    echo "⏹️  Deteniendo servicios actuales..."
    docker-compose -f docker-compose.secure.yml down || true
    
    # Iniciar con configuración de desarrollo
    echo "🚀 Iniciando con acceso externo habilitado..."
    docker-compose -f docker-compose.dev.yml up -d
    
    echo ""
    echo "✅ Acceso externo habilitado exitosamente!"
    echo ""
    echo "📋 Puertos disponibles:"
    echo "   Frontend:               http://localhost:3001"
    echo "   API Gateway:            http://localhost:8080"
    echo "   Consul UI:              http://localhost:8500"
    echo "   PostgreSQL (users):     localhost:5432"
    echo "   PostgreSQL (sync-a):    localhost:5433"
    echo "   MySQL (sync-b):         localhost:3306"
    echo "   MongoDB (sync-c):       localhost:27017"
    echo "   Kafka:                  localhost:9092"
    echo ""
    echo "🔧 Herramientas recomendadas:"
    echo "   - DBeaver (PostgreSQL/MySQL)"
    echo "   - MongoDB Compass (MongoDB)"
    echo "   - Kafka Tool (Kafka)"
}

disable_external_access() {
    echo "🔒 Deshabilitando acceso externo a bases de datos..."
    echo ""
    
    # Detener servicios de desarrollo
    echo "⏹️  Deteniendo servicios de desarrollo..."
    docker-compose -f docker-compose.dev.yml down || true
    
    # Iniciar con configuración segura
    echo "🚀 Iniciando con configuración segura..."
    docker-compose -f docker-compose.secure.yml up -d
    
    echo ""
    echo "✅ Acceso externo deshabilitado exitosamente!"
    echo ""
    echo "📋 Solo estos puertos están disponibles externamente:"
    echo "   API Gateway:            http://localhost:8080"
    echo "   Consul UI:              http://localhost:8500"
    echo ""
    echo "🔒 Las bases de datos solo son accesibles desde microservicios internos"
}

show_help() {
    echo "Uso: $0 [enable|disable|status]"
    echo ""
    echo "Comandos:"
    echo "  enable   - Habilita acceso externo a bases de datos (modo desarrollo)"
    echo "  disable  - Deshabilita acceso externo (modo seguro/producción)"
    echo "  status   - Muestra el estado actual"
    echo ""
    echo "Ejemplos:"
    echo "  $0 enable   # Habilitar acceso para desarrollo"
    echo "  $0 disable # Volver a modo seguro"
    echo "  $0 status  # Ver estado actual"
}

main() {
    print_header
    
    case "${1:-}" in
        "enable")
            enable_external_access
            print_status
            ;;
        "disable")
            disable_external_access
            print_status
            ;;
        "status")
            print_status
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            echo "❌ Comando no válido: ${1:-}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Verificar que Docker esté funcionando
if ! docker info >/dev/null 2>&1; then
    echo "❌ Error: Docker no está funcionando"
    echo "   Por favor, inicia Docker y vuelve a intentar"
    exit 1
fi

main "$@"
