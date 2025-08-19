# Multi-stage build para optimizar tamaño de imagen

# Etapa 1: Build de React
FROM node:18-alpine AS builder

# Información del mantenedor
LABEL maintainer="MediHelp360 Team"
LABEL version="1.0.0"
LABEL description="Frontend React para MediHelp360"

# Crear directorio de trabajo
WORKDIR /app

# Copiar package files primero (para aprovechar cache de Docker)
COPY package*.json ./

# Instalar dependencias
RUN npm ci --only=production && npm cache clean --force

# Copiar código fuente
COPY . .

# Construir aplicación React para producción
ARG REACT_APP_ENVIRONMENT=production
ARG REACT_APP_API_BASE_URL=http://api-gateway:8080
ARG REACT_APP_VERSION=1.0.0

ENV REACT_APP_ENVIRONMENT=$REACT_APP_ENVIRONMENT
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL
ENV REACT_APP_VERSION=$REACT_APP_VERSION
ENV REACT_APP_DEBUG=false
ENV GENERATE_SOURCEMAP=false

RUN npm run build

# Etapa 2: Servir con Nginx
FROM nginx:alpine

# Instalar curl para health checks
RUN apk add --no-cache curl

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Copiar archivos compilados de React
COPY --from=builder /app/build /usr/share/nginx/html

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
