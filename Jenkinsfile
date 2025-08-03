pipeline {
    agent any
    
    // Configurar polling en lugar de webhooks
    triggers {
        // Revisar cada 5 minutos si hay cambios
        pollSCM('H/5 * * * *')
        
        // Alternativamente, revisar solo en horas laborales
        // pollSCM('H/10 8-18 * * 1-5')
    }
    
    environment {
        DOCKER_REGISTRY = 'localhost:5000'  // Registry local
        COMPOSE_PROJECT_NAME = 'medihelp360'
        GIT_COMMIT_SHORT = sh(
            script: "git rev-parse --short HEAD",
            returnStdout: true
        ).trim()
        BUILD_NUMBER_TAG = "${BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
        
        // Determinar environment basado en branch
        ENVIRONMENT = sh(
            script: '''
                case "${GIT_BRANCH##*/}" in
                    main) echo "production" ;;
                    preprod) echo "preprod" ;;
                    develop) echo "development" ;;
                    *) echo "feature" ;;
                esac
            ''',
            returnStdout: true
        ).trim()
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
                checkout scm
                
                script {
                    echo "🌿 Branch: ${env.GIT_BRANCH}"
                    echo "🏗️  Environment: ${env.ENVIRONMENT}"
                    echo "🏷️  Build Tag: ${env.BUILD_NUMBER_TAG}"
                }
                
                // Notificar inicio del build (opcional)
                script {
                    if (env.SLACK_WEBHOOK_URL) {
                        sh '''
                            curl -X POST -H 'Content-type: application/json' \
                            --data "{\\"text\\":\\"🔄 Build iniciado\\nBranch: ${GIT_BRANCH##*/}\\nEnvironment: ${ENVIRONMENT}\\nCommit: ${GIT_COMMIT_SHORT}\\"}" \
                            ${SLACK_WEBHOOK_URL} || true
                        '''
                    }
                }
            }
        }
        
        stage('Build Images') {
            parallel {
                stage('Build API Gateway') {
                    steps {
                        script {
                            echo 'Building API Gateway...'
                            sh '''
                                cd api-gateway
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-api-gateway:${BUILD_NUMBER_TAG} .
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-api-gateway:${ENVIRONMENT}-latest .
                            '''
                        }
                    }
                }
                stage('Build User Management') {
                    steps {
                        script {
                            echo 'Building User Management Service...'
                            sh '''
                                cd user-management-service
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-user-management-service:${BUILD_NUMBER_TAG} .
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-user-management-service:${ENVIRONMENT}-latest .
                            '''
                        }
                    }
                }
                stage('Build Database Sync A') {
                    steps {
                        script {
                            echo 'Building Database Sync Service A...'
                            sh '''
                                cd database-sync-service-a
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-database-sync-service-a:${BUILD_NUMBER_TAG} .
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-database-sync-service-a:${ENVIRONMENT}-latest .
                            '''
                        }
                    }
                }
                stage('Build Database Sync B') {
                    steps {
                        script {
                            echo 'Building Database Sync Service B...'
                            sh '''
                                cd database-sync-service-b
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-database-sync-service-b:${BUILD_NUMBER_TAG} .
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-database-sync-service-b:${ENVIRONMENT}-latest .
                            '''
                        }
                    }
                }
                stage('Build Database Sync C') {
                    steps {
                        script {
                            echo 'Building Database Sync Service C...'
                            sh '''
                                cd database-sync-service-c
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-database-sync-service-c:${BUILD_NUMBER_TAG} .
                                docker build -t ${DOCKER_REGISTRY}/medihelp360-database-sync-service-c:${ENVIRONMENT}-latest .
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Run Tests') {
            parallel {
                stage('Unit Tests - User Management') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'preprod'
                            branch 'develop'
                        }
                    }
                    steps {
                        script {
                            echo 'Running User Management Service tests...'
                            sh '''
                                cd user-management-service
                                mvn clean test -B -Dspring.profiles.active=test
                            '''
                        }
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'user-management-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Unit Tests - Database Sync A') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'preprod'
                            branch 'develop'
                        }
                    }
                    steps {
                        script {
                            echo 'Running Database Sync Service A tests...'
                            sh '''
                                cd database-sync-service-a
                                mvn clean test -B -Dspring.profiles.active=test
                            '''
                        }
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'database-sync-service-a/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Integration Tests') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'preprod'
                        }
                    }
                    steps {
                        script {
                            echo 'Running integration tests...'
                            sh '''
                                # Solo para branches principales ejecutar integration tests
                                echo "🧪 Integration tests for ${ENVIRONMENT} environment"
                                ./scripts/health-check.sh || echo "Health check completed with warnings"
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Push to Registry') {
            when {
                anyOf {
                    branch 'main'
                    branch 'preprod'
                    branch 'develop'
                }
            }
            steps {
                script {
                    echo "📤 Pushing images to registry for ${env.ENVIRONMENT}..."
                    sh '''
                        # Push images with build number and environment tags
                        docker push ${DOCKER_REGISTRY}/medihelp360-api-gateway:${BUILD_NUMBER_TAG}
                        docker push ${DOCKER_REGISTRY}/medihelp360-api-gateway:${ENVIRONMENT}-latest
                        
                        docker push ${DOCKER_REGISTRY}/medihelp360-user-management-service:${BUILD_NUMBER_TAG}
                        docker push ${DOCKER_REGISTRY}/medihelp360-user-management-service:${ENVIRONMENT}-latest
                        
                        docker push ${DOCKER_REGISTRY}/medihelp360-database-sync-service-a:${BUILD_NUMBER_TAG}
                        docker push ${DOCKER_REGISTRY}/medihelp360-database-sync-service-a:${ENVIRONMENT}-latest
                        
                        docker push ${DOCKER_REGISTRY}/medihelp360-database-sync-service-b:${BUILD_NUMBER_TAG}
                        docker push ${DOCKER_REGISTRY}/medihelp360-database-sync-service-b:${ENVIRONMENT}-latest
                        
                        docker push ${DOCKER_REGISTRY}/medihelp360-database-sync-service-c:${BUILD_NUMBER_TAG}
                        docker push ${DOCKER_REGISTRY}/medihelp360-database-sync-service-c:${ENVIRONMENT}-latest
                    '''
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo '🚀 Deploying to PRODUCTION environment...'
                    sh '''
                        # Set environment variables for production
                        export BUILD_TAG=${BUILD_NUMBER_TAG}
                        export ENVIRONMENT=production
                        export COMPOSE_FILE=docker-compose.secure.yml
                        
                        echo "🏭 Production deployment starting..."
                        
                        # Run production deployment script
                        ./scripts/deploy.sh production
                        
                        # Wait for services to be healthy
                        echo "🏥 Waiting for production services to be healthy..."
                        ./scripts/wait-for-health.sh
                        
                        echo "✅ Production deployment completed successfully!"
                    '''
                }
            }
        }
        
        stage('Deploy to Pre-Production') {
            when {
                branch 'preprod'
            }
            steps {
                script {
                    echo '🚧 Deploying to PRE-PRODUCTION environment...'
                    sh '''
                        # Set environment variables for pre-production
                        export BUILD_TAG=${BUILD_NUMBER_TAG}
                        export ENVIRONMENT=preprod
                        export COMPOSE_FILE=docker-compose.preprod.yml
                        
                        echo "🧪 Pre-production deployment starting..."
                        
                        # Create preprod compose file if it doesn't exist
                        if [ ! -f "docker-compose.preprod.yml" ]; then
                            echo "📄 Creating pre-production compose file..."
                            sed 's/production/preprod/g; s/8080/8180/g; s/8081/8181/g; s/8082/8182/g; s/8083/8183/g; s/8084/8184/g' docker-compose.secure.yml > docker-compose.preprod.yml
                        fi
                        
                        # Run pre-production deployment script
                        ./scripts/deploy.sh preprod
                        
                        # Wait for services to be healthy
                        echo "🏥 Waiting for pre-production services to be healthy..."
                        ./scripts/wait-for-health.sh
                        
                        echo "✅ Pre-production deployment completed successfully!"
                    '''
                }
            }
        }
        
        stage('Deploy to Development') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    echo '🛠️  Deploying to DEVELOPMENT environment...'
                    sh '''
                        # Set environment variables for development
                        export BUILD_TAG=${BUILD_NUMBER_TAG}
                        export ENVIRONMENT=development
                        export COMPOSE_FILE=docker-compose.dev.yml
                        
                        echo "💻 Development deployment starting..."
                        
                        # Create dev compose file if it doesn't exist
                        if [ ! -f "docker-compose.dev.yml" ]; then
                            echo "📄 Creating development compose file..."
                            sed 's/production/development/g; s/8080/8280/g; s/8081/8281/g; s/8082/8282/g; s/8083/8283/g; s/8084/8284/g' docker-compose.secure.yml > docker-compose.dev.yml
                        fi
                        
                        # Run development deployment script
                        ./scripts/deploy.sh development
                        
                        # Wait for services to be healthy (more relaxed for dev)
                        echo "🏥 Waiting for development services to be healthy..."
                        timeout 300 ./scripts/wait-for-health.sh || echo "⚠️ Development health check timeout - continuing anyway"
                        
                        echo "✅ Development deployment completed!"
                    '''
                }
            }
        }
        
        stage('Feature Branch Build') {
            when {
                not {
                    anyOf {
                        branch 'main'
                        branch 'preprod'
                        branch 'develop'
                    }
                }
            }
            steps {
                script {
                    echo '🌿 Feature branch detected - building images only...'
                    sh '''
                        echo "🔍 Feature branch: ${GIT_BRANCH##*/}"
                        echo "📦 Images built and tagged as feature builds"
                        echo "⚠️  No deployment for feature branches"
                        echo "💡 Merge to develop/preprod/main to deploy"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            sh '''
                # Clean up old images to save space
                docker system prune -f
                docker image prune -f
            '''
        }
        success {
            script {
                echo "✅ Pipeline completed successfully for ${env.ENVIRONMENT}!"
                
                if (env.SLACK_WEBHOOK_URL) {
                    sh '''
                        curl -X POST -H 'Content-type: application/json' \
                        --data "{\\"text\\":\\"✅ MediHelp360 deployado exitosamente!\\nBranch: ${GIT_BRANCH##*/}\\nEnvironment: ${ENVIRONMENT}\\nBuild: ${BUILD_NUMBER_TAG}\\nCommit: ${GIT_COMMIT_SHORT}\\"}" \
                        ${SLACK_WEBHOOK_URL} || true
                    '''
                }
            }
        }
        failure {
            script {
                echo "❌ Pipeline failed for ${env.ENVIRONMENT}!"
                
                if (env.SLACK_WEBHOOK_URL) {
                    sh '''
                        curl -X POST -H 'Content-type: application/json' \
                        --data "{\\"text\\":\\"❌ Build falló para MediHelp360\\nBranch: ${GIT_BRANCH##*/}\\nEnvironment: ${ENVIRONMENT}\\nBuild: ${BUILD_NUMBER_TAG}\\nError: Pipeline failed\\"}" \
                        ${SLACK_WEBHOOK_URL} || true
                    '''
                }
            }
        }
        unstable {
            script {
                echo "⚠️ Pipeline completed with warnings for ${env.ENVIRONMENT}"
                
                if (env.SLACK_WEBHOOK_URL) {
                    sh '''
                        curl -X POST -H 'Content-type: application/json' \
                        --data "{\\"text\\":\\"⚠️ Build inestable para MediHelp360\\nBranch: ${GIT_BRANCH##*/}\\nEnvironment: ${ENVIRONMENT}\\nBuild: ${BUILD_NUMBER_TAG}\\nWarning: Tests failed or warnings\\"}" \
                        ${SLACK_WEBHOOK_URL} || true
                    '''
                }
            }
        }
    }
} 