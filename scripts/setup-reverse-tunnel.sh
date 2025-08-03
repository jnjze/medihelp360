#!/bin/bash

set -e

echo "🔗 Setup Reverse SSH Tunnel para Webhooks"
echo "=========================================="

# Configuración (ajusta estos valores)
PUBLIC_SERVER=${1:-"tu-servidor-publico.com"}
PUBLIC_USER=${2:-"tunnel"}
LOCAL_JENKINS_PORT=8090
REMOTE_TUNNEL_PORT=9090
SSH_KEY_PATH="$HOME/.ssh/id_rsa_tunnel"

echo "📋 Configuración del túnel:"
echo "   Servidor público: $PUBLIC_SERVER"
echo "   Usuario: $PUBLIC_USER"
echo "   Puerto local Jenkins: $LOCAL_JENKINS_PORT"
echo "   Puerto remoto túnel: $REMOTE_TUNNEL_PORT"
echo "   Clave SSH: $SSH_KEY_PATH"

# Función para generar clave SSH
generate_ssh_key() {
    if [ ! -f "$SSH_KEY_PATH" ]; then
        echo "🔑 Generando clave SSH para túnel..."
        ssh-keygen -t rsa -b 4096 -f "$SSH_KEY_PATH" -N "" -C "tunnel-medihelp360"
        echo "✅ Clave SSH generada: $SSH_KEY_PATH"
        
        echo ""
        echo "📋 IMPORTANTE: Copia esta clave pública al servidor público:"
        echo "============================================================="
        cat "$SSH_KEY_PATH.pub"
        echo "============================================================="
        echo ""
        echo "Ejecuta en el servidor público:"
        echo "  mkdir -p ~/.ssh"
        echo "  echo '$(cat $SSH_KEY_PATH.pub)' >> ~/.ssh/authorized_keys"
        echo "  chmod 600 ~/.ssh/authorized_keys"
        echo ""
        read -p "Presiona Enter cuando hayas configurado la clave en el servidor público..."
    else
        echo "✅ Clave SSH ya existe: $SSH_KEY_PATH"
    fi
}

# Función para configurar tunnel
setup_tunnel() {
    echo "🔗 Configurando túnel SSH reverse..."
    
    # Crear archivo de configuración SSH
    cat > ~/.ssh/config_tunnel << EOF
Host tunnel-server
    HostName $PUBLIC_SERVER
    User $PUBLIC_USER
    IdentityFile $SSH_KEY_PATH
    ServerAliveInterval 60
    ServerAliveCountMax 3
    ExitOnForwardFailure yes
    RemoteForward $REMOTE_TUNNEL_PORT localhost:$LOCAL_JENKINS_PORT
EOF

    echo "✅ Configuración SSH creada"
}

# Función para testear conectividad
test_connection() {
    echo "🧪 Testeando conexión SSH..."
    
    if ssh -F ~/.ssh/config_tunnel tunnel-server "echo 'Conexión exitosa'" 2>/dev/null; then
        echo "✅ Conexión SSH funcionando"
        return 0
    else
        echo "❌ Conexión SSH falló"
        echo "💡 Verifica:"
        echo "   - El servidor público está accesible"
        echo "   - La clave pública está en ~/.ssh/authorized_keys del servidor"
        echo "   - El usuario $PUBLIC_USER existe en el servidor"
        return 1
    fi
}

# Función para crear servicio systemd (Linux)
create_systemd_service() {
    if [ "$(uname)" = "Linux" ]; then
        echo "🐧 Creando servicio systemd para túnel persistente..."
        
        sudo tee /etc/systemd/system/jenkins-tunnel.service > /dev/null << EOF
[Unit]
Description=SSH Reverse Tunnel for Jenkins Webhooks
After=network.target

[Service]
Type=simple
User=$USER
ExecStart=/usr/bin/ssh -F $HOME/.ssh/config_tunnel -N tunnel-server
Restart=always
RestartSec=10
KillMode=process

[Install]
WantedBy=multi-user.target
EOF

        sudo systemctl daemon-reload
        sudo systemctl enable jenkins-tunnel.service
        
        echo "✅ Servicio systemd creado y habilitado"
        echo "💡 Comandos útiles:"
        echo "   sudo systemctl start jenkins-tunnel    # Iniciar túnel"
        echo "   sudo systemctl stop jenkins-tunnel     # Parar túnel"
        echo "   sudo systemctl status jenkins-tunnel   # Ver estado"
        echo "   sudo journalctl -u jenkins-tunnel -f   # Ver logs"
    else
        echo "⚠️  Systemd no disponible (no es Linux)"
    fi
}

# Función para crear launchd service (macOS)
create_launchd_service() {
    if [ "$(uname)" = "Darwin" ]; then
        echo "🍎 Creando servicio launchd para túnel persistente..."
        
        PLIST_PATH="$HOME/Library/LaunchAgents/com.medihelp360.jenkins-tunnel.plist"
        
        cat > "$PLIST_PATH" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.medihelp360.jenkins-tunnel</string>
    <key>ProgramArguments</key>
    <array>
        <string>/usr/bin/ssh</string>
        <string>-F</string>
        <string>$HOME/.ssh/config_tunnel</string>
        <string>-N</string>
        <string>tunnel-server</string>
    </array>
    <key>KeepAlive</key>
    <true/>
    <key>RunAtLoad</key>
    <true/>
    <key>StandardErrorPath</key>
    <string>$HOME/Library/Logs/jenkins-tunnel.log</string>
    <key>StandardOutPath</key>
    <string>$HOME/Library/Logs/jenkins-tunnel.log</string>
</dict>
</plist>
EOF

        launchctl load "$PLIST_PATH" 2>/dev/null || true
        
        echo "✅ Servicio launchd creado y cargado"
        echo "💡 Comandos útiles:"
        echo "   launchctl start com.medihelp360.jenkins-tunnel  # Iniciar túnel"
        echo "   launchctl stop com.medihelp360.jenkins-tunnel   # Parar túnel"
        echo "   tail -f ~/Library/Logs/jenkins-tunnel.log       # Ver logs"
    else
        echo "⚠️  Launchd no disponible (no es macOS)"
    fi
}

# Función para configurar webhook
configure_webhook() {
    echo "🔗 Configuración del Webhook"
    echo "============================"
    echo ""
    echo "Configura el webhook en GitHub con esta información:"
    echo ""
    echo "📍 Payload URL: http://$PUBLIC_SERVER:$REMOTE_TUNNEL_PORT/github-webhook/"
    echo "📋 Content type: application/json"
    echo "🎯 Which events: Just the push event"
    echo "✅ Active: ☑"
    echo ""
    echo "El túnel redirigirá las peticiones a tu Jenkins local en puerto $LOCAL_JENKINS_PORT"
}

# Función para monitorear túnel
monitor_tunnel() {
    echo "📊 Monitoreando túnel SSH..."
    
    while true; do
        if ps aux | grep -q "[s]sh.*tunnel-server"; then
            echo "✅ $(date): Túnel activo"
        else
            echo "❌ $(date): Túnel inactivo - reintentando..."
            ssh -F ~/.ssh/config_tunnel -N tunnel-server &
        fi
        sleep 30
    done
}

# Función para mostrar ayuda
show_help() {
    echo "Setup Reverse SSH Tunnel para MediHelp360"
    echo ""
    echo "Uso:"
    echo "  $0 [servidor_publico] [usuario]"
    echo ""
    echo "Parámetros:"
    echo "  servidor_publico - IP o dominio del servidor público"
    echo "  usuario         - Usuario SSH en el servidor público"
    echo ""
    echo "Comandos:"
    echo "  $0 setup                    # Configuración inicial completa"
    echo "  $0 test                     # Testear conexión SSH"
    echo "  $0 start                    # Iniciar túnel manualmente"
    echo "  $0 monitor                  # Monitorear túnel continuamente"
    echo "  $0 webhook-info             # Mostrar info de configuración webhook"
    echo "  $0 help                     # Mostrar esta ayuda"
    echo ""
    echo "Ejemplo:"
    echo "  $0 mi-servidor.com tunnel-user"
}

# Menú principal
case "${3:-setup}" in
    "help"|"-h"|"--help")
        show_help
        ;;
    "test")
        test_connection
        ;;
    "start")
        echo "🚀 Iniciando túnel SSH..."
        ssh -F ~/.ssh/config_tunnel -N tunnel-server
        ;;
    "monitor")
        monitor_tunnel
        ;;
    "webhook-info")
        configure_webhook
        ;;
    "setup"|*)
        if [ -z "$PUBLIC_SERVER" ] || [ "$PUBLIC_SERVER" = "tu-servidor-publico.com" ]; then
            echo "❌ Debes especificar un servidor público válido"
            echo "Uso: $0 <servidor_publico> <usuario>"
            exit 1
        fi
        
        echo "🚀 Iniciando configuración completa..."
        
        generate_ssh_key
        setup_tunnel
        
        if test_connection; then
            create_systemd_service
            create_launchd_service
            configure_webhook
            
            echo ""
            echo "🎉 Configuración completada!"
            echo ""
            echo "📋 Próximos pasos:"
            echo "1. Configurar webhook en GitHub con la URL mostrada arriba"
            echo "2. Iniciar el servicio del túnel"
            echo "3. Testear el webhook"
            
            if [ "$(uname)" = "Linux" ]; then
                echo ""
                echo "🐧 Para iniciar el túnel en Linux:"
                echo "   sudo systemctl start jenkins-tunnel"
            elif [ "$(uname)" = "Darwin" ]; then
                echo ""
                echo "🍎 Para iniciar el túnel en macOS:"
                echo "   launchctl start com.medihelp360.jenkins-tunnel"
            fi
        else
            echo "❌ No se pudo establecer conexión SSH"
            echo "Revisa la configuración e inténtalo de nuevo"
        fi
        ;;
esac 