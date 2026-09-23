pipeline {
    agent any
tools {
        maven 'Maven'
    }
parameters {
        string(
            name: 'IMAGE_TAG',
            defaultValue: '1.0.0',
            description: 'Docker image tag'
        )
    }
stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
stage('Build Jar') {
            steps {
                sh 'mvn clean package'
            }
        }
stage('Build & Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-repo',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh """
                            docker build -t ${DOCKER_USER}/java-maven-app:${params.IMAGE_TAG} .
                            echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
                            docker push ${DOCKER_USER}/java-maven-app:${params.IMAGE_TAG}
                        """
                    }
                }
            }
        }
    }
post {
        success {
            echo "Pipeline succeeded. Image pushed: ${env.DOCKER_USER}/java-maven-app:${params.IMAGE_TAG}"
        }
        failure {
            echo "Pipeline failed."
        }
    }
}
