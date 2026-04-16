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
                // Fetch all secrets at runtime from Vault — no static credentials here.
                //
                // database/creds/jenkins-deploy: short-lived dynamic PG credentials
                //   (auto-rotated by Vault, TTL controlled by vault/database.tf)
                //
                // kv/housekeeping/api-approle: the API container's own Vault AppRole
                //   credentials — the running container uses these to fetch its own
                //   dynamic DB credentials on startup.
                withVault(
                    configuration: [vaultCredentialId: 'vault-approle'],
                    vaultSecrets: [
                        [path: 'database/creds/jenkins-deploy', engineVersion: 1, secretValues: [
                            [envVar: 'DB_USER',     vaultKey: 'username'],
                            [envVar: 'DB_PASSWORD', vaultKey: 'password']
                        ]],
                        [path: 'kv/housekeeping/api-approle', engineVersion: 2, secretValues: [
                            [envVar: 'VAULT_ROLE_ID',   vaultKey: 'role_id'],
                            [envVar: 'VAULT_SECRET_ID', vaultKey: 'secret_id'],
                            [envVar: 'VAULT_ADDR',      vaultKey: 'vault_addr']
                        ]],
                        [path: 'kv/housekeeping/keycloak', engineVersion: 2, secretValues: [
                            [envVar: 'KEYCLOAK_ADMIN_USER',     vaultKey: 'admin_user'],
                            [envVar: 'KEYCLOAK_ADMIN_PASSWORD', vaultKey: 'admin_password']
                        ]]
                    ]
                ) {
                    sh """
                        export DB_NAME=\${APP_DB_NAME}
                        docker compose -f ${COMPOSE_FILE} up -d --force-recreate api
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
