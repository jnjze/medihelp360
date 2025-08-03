# =========================================
# API Gateway - MediHelp360
# Multi-stage Dockerfile for Spring Boot
# =========================================

# Stage 1: Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS builder

LABEL stage=builder
LABEL service=api-gateway
LABEL version=1.0.0

WORKDIR /app

# Copy pom.xml first for better Docker layer caching
COPY pom.xml .

# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre AS runtime

LABEL maintainer="MediHelp360 Team <dev@medihelp360.com>"
LABEL service="api-gateway"
LABEL version="1.0.0"
LABEL description="API Gateway for MediHelp360 microservices ecosystem"
LABEL project="medihelp360"

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create application user (non-root for security)
RUN groupadd -r appgroup --gid=1001 && \
    useradd -r -g appgroup --uid=1001 --create-home --shell=/bin/bash appuser

# Create application directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership of the app directory
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Environment variables with defaults
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=docker
ENV CONSUL_HOST=consul
ENV CONSUL_PORT=8500
ENV JVM_OPTS="-Xmx512m -Xms256m"
ENV JAVA_OPTS="-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}"

# Expose port
EXPOSE ${SERVER_PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# Set entrypoint
ENTRYPOINT ["java"]

# Run the application
CMD ["-Xmx512m", "-Xms256m", "-jar", "app.jar"] 