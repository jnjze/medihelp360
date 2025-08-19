# Multi-stage build para React + Vite

# Etapa 1: Build de React con Vite
FROM node:18-alpine AS builder

LABEL maintainer="MediHelp360 Team"
LABEL version="1.0.0"
LABEL description="MediHelp360 Frontend - Vite + React"

# Crear directorio de trabajo
WORKDIR /app

# Copiar package files primero (para aprovechar cache de Docker)
COPY package*.json ./

# Instalar dependencias
RUN npm ci --only=production && npm cache clean --force

# Copiar código fuente
COPY . .

# Variables de entorno para build
ARG VITE_API_BASE_URL=http://api-gateway:8080
ARG VITE_ENV=production
ARG VITE_APP_VERSION=1.0.0

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_ENV=$VITE_ENV
ENV VITE_APP_VERSION=$VITE_APP_VERSION
ENV VITE_DEBUG=false

# Construir aplicación Vite para producción
RUN npm run build

# Etapa 2: Servir con Nginx
FROM nginx:alpine

# Instalar curl para health checks
RUN apk add --no-cache curl

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Copiar archivos compilados de Vite (dist en lugar de build)
COPY --from=builder /app/dist /usr/share/nginx/html

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

# Configurar permisos
RUN chown -R appuser:appgroup /usr/share/nginx/html && \
    chown -R appuser:appgroup /var/cache/nginx && \
    chown -R appuser:appgroup /var/log/nginx && \
    chown -R appuser:appgroup /etc/nginx/conf.d

# Crear directorio para PID con permisos correctos
RUN touch /var/run/nginx.pid && \
    chown appuser:appgroup /var/run/nginx.pid

# Cambiar a usuario no-root
USER appuser

# Exponer puerto
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3000/ || exit 1

# Comando por defecto
CMD ["nginx", "-g", "daemon off;"]
