pipeline {
  agent any

  environment {
    DOCKER_REGISTRY = 'docker.io/jnjze'  // cámbialo si usas otro registry
    BRANCH_NAME = "${env.BRANCH_NAME ?: 'main'}"
  }

  stages {
    stage('Checkout') {
      steps {
        git url: 'https://github.com/jnjze/medihelp360.git'
      }
    }

    stage('Build Docker Images') {
      steps {
        script {
          def services = [
            'api-gateway',
            'user-management-service',
            'database-sync-service-a',
            'database-sync-service-b',
            'database-sync-service-c'
          ]

          for (service in services) {
            def imageName = "${DOCKER_REGISTRY}/${service}:${BRANCH_NAME}"
            echo "Building ${imageName}"
            sh "docker build -t ${imageName} ${service}"
          }
        }
      }
    }

    stage('Push Docker Images (optional)') {
      when {
        expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'preprod' || env.BRANCH_NAME == 'production' }
      }
      steps {
        script {
          def services = [
            'api-gateway',
            'user-management-service',
            'database-sync-service-a',
            'database-sync-service-b',
            'database-sync-service-c'
          ]

          for (service in services) {
            def imageName = "${DOCKER_REGISTRY}/${service}:${BRANCH_NAME}"
            echo "Pushing ${imageName}"
            sh "docker push ${imageName}"
          }
        }
      }
    }

    stage('Post') {
      steps {
        echo "Build completed for branch: ${BRANCH_NAME}"
      }
    }
  }
}
