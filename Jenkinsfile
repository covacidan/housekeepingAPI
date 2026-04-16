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

        stage('Set Version') {
            steps {
                sh "./mvnw versions:set -DnewVersion=1.0.${BUILD_NUMBER} -DgenerateBackupPoms=false"
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
                    sh "./mvnw sonar:sonar -Dsonar.projectKey=housekeeping-api -Dsonar.projectName=\"Housekeeping API\" -Dsonar.projectVersion=1.0.${BUILD_NUMBER} -Dsonar.token=\$SONAR_AUTH_TOKEN -Dsonar.qualitygate.wait=true"
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
                    string(credentialsId: 'DB_USER',                variable: 'DB_USER'),
                    string(credentialsId: 'DB_PASSWORD',            variable: 'DB_PASSWORD'),
                    string(credentialsId: 'DB_NAME',                variable: 'DB_NAME'),
                    string(credentialsId: 'KEYCLOAK_ADMIN_USER',    variable: 'KEYCLOAK_ADMIN_USER'),
                    string(credentialsId: 'KEYCLOAK_ADMIN_PASSWORD',variable: 'KEYCLOAK_ADMIN_PASSWORD')
                ]) {
                    sh """
                        export DB_USER=\$DB_USER
                        export DB_PASSWORD=\$DB_PASSWORD
                        export DB_NAME=\$DB_NAME
                        export KEYCLOAK_ADMIN_USER=\$KEYCLOAK_ADMIN_USER
                        export KEYCLOAK_ADMIN_PASSWORD=\$KEYCLOAK_ADMIN_PASSWORD
                        docker compose -f ${COMPOSE_FILE} up -d --force-recreate keycloak api
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
