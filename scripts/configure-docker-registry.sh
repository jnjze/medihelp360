#!/bin/bash

set -e

echo "🐳 Configuring Docker for local registry..."

# Detect OS
OS=$(uname -s)
DOCKER_CONFIG_DIR=""

case $OS in
    "Darwin")
        echo "📱 Detected macOS"
        echo "⚠️  On macOS, you need to configure Docker Desktop manually:"
        echo "1. Open Docker Desktop"
        echo "2. Go to Settings → Docker Engine"
        echo "3. Add this configuration:"
        echo '{
  "builder": {
    "gc": {
      "enabled": true,
      "defaultKeepStorage": "20GB"
    }
  },
  "experimental": false,
  "features": {
    "buildkit": true
  },
  "insecure-registries": [
    "localhost:5000",
    "127.0.0.1:5000"
  ]
}'
        echo "4. Click 'Apply & Restart'"
        ;;
    "Linux")
        echo "🐧 Detected Linux"
        DOCKER_CONFIG_DIR="/etc/docker"
        
        # Create Docker config directory if it doesn't exist
        if [ ! -d "$DOCKER_CONFIG_DIR" ]; then
            echo "📁 Creating Docker config directory..."
            sudo mkdir -p "$DOCKER_CONFIG_DIR"
        fi
        
        # Backup existing daemon.json if it exists
        if [ -f "$DOCKER_CONFIG_DIR/daemon.json" ]; then
            echo "💾 Backing up existing daemon.json..."
            sudo cp "$DOCKER_CONFIG_DIR/daemon.json" "$DOCKER_CONFIG_DIR/daemon.json.backup.$(date +%Y%m%d_%H%M%S)"
        fi
        
        # Create or update daemon.json
        echo "📝 Updating Docker daemon configuration..."
        
        # If daemon.json exists, merge the configuration
        if [ -f "$DOCKER_CONFIG_DIR/daemon.json" ]; then
            # Use jq to merge configurations if available
            if command -v jq >/dev/null 2>&1; then
                echo "🔧 Merging with existing configuration using jq..."
                sudo jq '. + {"insecure-registries": ["localhost:5000", "127.0.0.1:5000"]}' "$DOCKER_CONFIG_DIR/daemon.json" > /tmp/daemon.json.new
                sudo mv /tmp/daemon.json.new "$DOCKER_CONFIG_DIR/daemon.json"
            else
                echo "⚠️  jq not found. Please manually add insecure-registries to $DOCKER_CONFIG_DIR/daemon.json"
                echo "Add this to your daemon.json:"
                echo '  "insecure-registries": ["localhost:5000", "127.0.0.1:5000"]'
            fi
        else
            # Create new daemon.json
            echo "📝 Creating new daemon.json..."
            sudo tee "$DOCKER_CONFIG_DIR/daemon.json" > /dev/null << 'EOF'
{
  "insecure-registries": [
    "localhost:5000",
    "127.0.0.1:5000"
  ],
  "features": {
    "buildkit": true
  },
  "builder": {
    "gc": {
      "enabled": true,
      "defaultKeepStorage": "20GB"
    }
  }
}
EOF
        fi
        
        echo "🔄 Restarting Docker daemon..."
        sudo systemctl restart docker || {
            echo "❌ Failed to restart Docker. Please restart manually:"
            echo "   sudo systemctl restart docker"
            exit 1
        }
        
        echo "⏳ Waiting for Docker to be ready..."
        sleep 5
        
        if docker info >/dev/null 2>&1; then
            echo "✅ Docker is running"
        else
            echo "❌ Docker is not responding. Please check the configuration."
            exit 1
        fi
        ;;
    *)
        echo "❓ Unsupported OS: $OS"
        echo "Please manually configure Docker daemon.json with:"
        echo '{
  "insecure-registries": ["localhost:5000", "127.0.0.1:5000"]
}'
        exit 1
        ;;
esac

# Test registry connectivity
echo "🧪 Testing registry connectivity..."
if curl -f http://localhost:5000/v2/ >/dev/null 2>&1; then
    echo "✅ Registry is accessible at http://localhost:5000"
else
    echo "⚠️  Registry is not running. Start it with:"
    echo "   docker-compose -f docker-compose.jenkins.yml up -d registry"
fi

# Test Docker push to registry
echo "🧪 Testing Docker push to local registry..."
echo "Pulling hello-world image..."
docker pull hello-world:latest

echo "Tagging for local registry..."
docker tag hello-world:latest localhost:5000/hello-world:test

echo "Pushing to local registry..."
if docker push localhost:5000/hello-world:test; then
    echo "✅ Successfully pushed to local registry!"
    
    # Clean up test image
    docker rmi localhost:5000/hello-world:test || true
    
    echo "🧹 Cleaned up test images"
else
    echo "❌ Failed to push to local registry"
    echo "Please check Docker configuration and registry status"
    exit 1
fi

echo ""
echo "🎉 Docker registry configuration completed!"
echo "✅ Your Docker daemon is now configured to work with localhost:5000"
echo ""
echo "💡 You can now:"
echo "1. Start Jenkins: ./scripts/setup-jenkins.sh"
echo "2. Push images to localhost:5000/image-name:tag"
echo "3. View registry contents at http://localhost:8091" 