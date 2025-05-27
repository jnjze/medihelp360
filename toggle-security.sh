#!/bin/bash

# Script para alternar entre modo seguro y modo debugging
# Uso: ./toggle-security.sh [secure|debug]

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Archivos de configuración
CONFIG_FILES=(
    "user-management-service/src/main/resources/application.yml"
    "database-sync-service-a/src/main/resources/application.yml"
    "database-sync-service-b/src/main/resources/application.yml"
    "database-sync-service-c/src/main/resources/application.yml"
)

show_usage() {
    echo "🔧 MediHelp360 Security Configuration Toggle"
    echo ""
    echo "Usage: $0 [secure|debug|status]"
    echo ""
    echo "Commands:"
    echo "  secure  - Enable secure mode (block direct access)"
    echo "  debug   - Enable debug mode (allow direct access)"
    echo "  status  - Show current security configuration"
    echo ""
    echo "Examples:"
    echo "  $0 secure   # Block direct access to all services"
    echo "  $0 debug    # Allow direct access for debugging"
    echo "  $0 status   # Check current configuration"
}

check_current_config() {
    echo -e "${BLUE}📋 Current Security Configuration:${NC}"
    echo ""
    
    for config_file in "${CONFIG_FILES[@]}"; do
        if [[ -f "$config_file" ]]; then
            service_name=$(echo "$config_file" | cut -d'/' -f1)
            current_setting=$(grep -A 1 "allow-direct-access:" "$config_file" | grep -o "true\|false" | head -1)
            
            if [[ "$current_setting" == "false" ]]; then
                echo -e "  🔒 ${service_name}: ${GREEN}SECURE${NC} (direct access blocked)"
            elif [[ "$current_setting" == "true" ]]; then
                echo -e "  🔓 ${service_name}: ${YELLOW}DEBUG${NC} (direct access allowed)"
            else
                echo -e "  ❓ ${service_name}: ${RED}UNKNOWN${NC} (configuration not found)"
            fi
        else
            service_name=$(echo "$config_file" | cut -d'/' -f1)
            echo -e "  ❌ ${service_name}: ${RED}FILE NOT FOUND${NC}"
        fi
    done
    echo ""
}

set_security_mode() {
    local mode=$1
    local setting_value
    local mode_description
    local mode_color
    
    if [[ "$mode" == "secure" ]]; then
        setting_value="false"
        mode_description="SECURE MODE (Gateway Only)"
        mode_color="${GREEN}"
    elif [[ "$mode" == "debug" ]]; then
        setting_value="true"
        mode_description="DEBUG MODE (Direct Access Allowed)"
        mode_color="${YELLOW}"
    else
        echo -e "${RED}❌ Invalid mode: $mode${NC}"
        show_usage
        exit 1
    fi
    
    echo -e "${mode_color}🔧 Setting all services to: $mode_description${NC}"
    echo ""
    
    for config_file in "${CONFIG_FILES[@]}"; do
        if [[ -f "$config_file" ]]; then
            service_name=$(echo "$config_file" | cut -d'/' -f1)
            
            # Backup original file
            cp "$config_file" "$config_file.backup.$(date +%Y%m%d_%H%M%S)"
            
            # Update the configuration
            if grep -q "allow-direct-access:" "$config_file"; then
                sed -i.tmp "s/allow-direct-access: .*/allow-direct-access: $setting_value  # Updated by toggle-security.sh/" "$config_file"
                rm "$config_file.tmp" 2>/dev/null
                echo -e "  ✅ ${service_name}: Updated to ${mode_color}$mode_description${NC}"
            else
                echo -e "  ⚠️  ${service_name}: ${YELLOW}Configuration not found, skipping${NC}"
            fi
        else
            service_name=$(echo "$config_file" | cut -d'/' -f1)
            echo -e "  ❌ ${service_name}: ${RED}File not found${NC}"
        fi
    done
    
    echo ""
    echo -e "${mode_color}🎉 Security mode updated to: $mode_description${NC}"
    echo ""
    echo -e "${BLUE}📋 Next steps:${NC}"
    echo "1. Restart all services for changes to take effect"
    echo "2. Run ./test-gateway-security.sh to verify configuration"
    
    if [[ "$mode" == "debug" ]]; then
        echo ""
        echo -e "${YELLOW}⚠️  WARNING: Debug mode allows direct access to services${NC}"
        echo -e "${YELLOW}   Remember to switch back to secure mode when done debugging${NC}"
        echo -e "${YELLOW}   Command: $0 secure${NC}"
    fi
}

# Main script logic
case "${1:-}" in
    "secure")
        set_security_mode "secure"
        ;;
    "debug")
        set_security_mode "debug"
        ;;
    "status")
        check_current_config
        ;;
    "")
        echo -e "${RED}❌ No command specified${NC}"
        echo ""
        show_usage
        exit 1
        ;;
    *)
        echo -e "${RED}❌ Unknown command: $1${NC}"
        echo ""
        show_usage
        exit 1
        ;;
esac

echo ""
echo -e "${BLUE}💡 Tip: Use '$0 status' to check current configuration anytime${NC}" 