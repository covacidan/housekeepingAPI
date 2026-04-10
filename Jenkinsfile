pipeline {
    agent any

    environment {
        IMAGE_NAME = 'housekeeping-api'
        CONTAINER_NAME = 'housekeeping_api'
        COMPOSE_FILE = 'docker-compose.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test -Dspring.profiles.active=test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build') {
            steps {
                sh './mvnw package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './mvnw sonar:sonar -Dsonar.projectKey=housekeeping-api -Dsonar.projectName="Housekeeping API"'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'DB_USER',        variable: 'DB_USER'),
                    string(credentialsId: 'DB_PASSWORD',    variable: 'DB_PASSWORD'),
                    string(credentialsId: 'DB_NAME',        variable: 'DB_NAME'),
                    string(credentialsId: 'JWT_SECRET',     variable: 'JWT_SECRET'),
                    string(credentialsId: 'ADMIN_EMAIL',    variable: 'ADMIN_EMAIL'),
                    string(credentialsId: 'ADMIN_PASSWORD', variable: 'ADMIN_PASSWORD')
                ]) {
                    sh """
                        export DB_USER=\$DB_USER
                        export DB_PASSWORD=\$DB_PASSWORD
                        export DB_NAME=\$DB_NAME
                        export JWT_SECRET=\$JWT_SECRET
                        export ADMIN_EMAIL=\$ADMIN_EMAIL
                        export ADMIN_PASSWORD=\$ADMIN_PASSWORD
                        docker compose -f ${COMPOSE_FILE} up -d --no-deps api
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'API deployed successfully.'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
