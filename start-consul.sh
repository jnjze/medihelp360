#!/bin/bash

echo "🚀 Starting Consul for MediHelp360 Service Discovery..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start Consul
echo "📦 Starting Consul server..."
docker-compose -f docker-compose.consul.yml up -d

# Wait for Consul to be ready
echo "⏳ Waiting for Consul to be ready..."
sleep 10

# Check Consul health
if curl -s http://localhost:8500/v1/status/leader > /dev/null; then
    echo "✅ Consul is running successfully!"
    echo "🌐 Consul UI available at: http://localhost:8500"
    echo "📊 API endpoint: http://localhost:8500/v1/"
else
    echo "❌ Consul failed to start properly"
    exit 1
fi

echo ""
echo "🔧 To stop Consul, run:"
echo "   docker-compose -f docker-compose.consul.yml down"
echo ""
echo "📋 To view logs, run:"
echo "   docker-compose -f docker-compose.consul.yml logs -f" 