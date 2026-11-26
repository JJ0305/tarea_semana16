pipeline {
    agent any

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Etiqueta publicada por GitHub Actions')
        string(name: 'GHCR_OWNER', defaultValue: 'your-github-owner', description: 'Propietario (usuario u organización) de las imágenes en GHCR')
    }

    environment {
        COMPOSE_PROJECT_NAME = 'calificada16-test'
        APP_PORT = '8082'
        BACKEND_IMAGE = "ghcr.io/${params.GHCR_OWNER.toLowerCase()}/calificada16-backend:${params.IMAGE_TAG}"
        FRONTEND_IMAGE = "ghcr.io/${params.GHCR_OWNER.toLowerCase()}/calificada16-frontend:${params.IMAGE_TAG}"
    }

    stages {
        stage('Preparar despliegue') {
            steps {
                checkout scm
                script {
                    def gitAuthor = sh(
                        script: 'git log -1 --format=%an',
                        returnStdout: true
                    ).trim().replaceAll(/[\r\n<>&"']/, ' ')
                    def gitCommit = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    currentBuild.description = "Autor del commit: ${gitAuthor} | ${gitCommit}"
                }
                withCredentials([
                    usernamePassword(credentialsId: 'ghcr-readonly', usernameVariable: 'GHCR_USER', passwordVariable: 'GHCR_TOKEN'),
                    string(credentialsId: 'calificada16-db-password', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'calificada16-mysql-root-password', variable: 'MYSQL_ROOT_PASSWORD'),
                    string(credentialsId: 'calificada16-admin-password', variable: 'APP_ADMIN_PASSWORD')
                ]) {
                    sh '''
                        echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USER" --password-stdin
                        docker compose pull backend frontend
                        docker compose up -d --remove-orphans
                    '''
                }
            }
        }

        stage('Verificar ambiente de prueba') {
            steps {
                sh '''
                    for attempt in $(seq 1 30); do
                      if curl --fail --silent http://host.docker.internal:${APP_PORT}/ > /dev/null; then
                        echo "Aplicación disponible en http://localhost:${APP_PORT}"
                        exit 0
                      fi
                      sleep 5
                    done
                    docker compose ps
                    docker compose logs --tail=100
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            sh 'docker logout ghcr.io || true'
        }
    }
}
