pipeline {
    agent any

    environment {
        IMAGE_NAME = 'housekeeping-api'
        CONTAINER_NAME = 'housekeeping_api'
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
                // Fetch the API's Vault AppRole credentials at runtime.
                // The running container uses VAULT_ROLE_ID/VAULT_SECRET_ID to
                // authenticate to Vault and fetch its own dynamic DB credentials
                // on startup — no DB password is passed here.
                withVault(
                    configuration: [vaultCredentialId: 'vault-approle'],
                    vaultSecrets: [
                        [path: 'kv/housekeeping/api-approle', engineVersion: 2, secretValues: [
                            [envVar: 'API_VAULT_ROLE_ID',   vaultKey: 'role_id'],
                            [envVar: 'API_VAULT_SECRET_ID', vaultKey: 'secret_id'],
                            [envVar: 'API_VAULT_ADDR',      vaultKey: 'vault_addr']
                        ]]
                    ]
                ) {
                    sh """
                        docker stop ${CONTAINER_NAME} 2>/dev/null || true
                        docker rm   ${CONTAINER_NAME} 2>/dev/null || true
                        docker run -d \
                            --name ${CONTAINER_NAME} \
                            --network housekeeping_net \
                            --restart unless-stopped \
                            -p 8081:8080 \
                            -e DB_HOST=housekeeping_postgres \
                            -e DB_NAME=\${APP_DB_NAME} \
                            -e KEYCLOAK_HOST=housekeeping_keycloak \
                            -e KEYCLOAK_PORT=8080 \
                            -e VAULT_ADDR=\${API_VAULT_ADDR} \
                            -e VAULT_ROLE_ID=\${API_VAULT_ROLE_ID} \
                            -e VAULT_SECRET_ID=\${API_VAULT_SECRET_ID} \
                            ${IMAGE_NAME}:latest
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
