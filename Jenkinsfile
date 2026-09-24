pipeline {
    agent any

    stages {

        stage("test") {
            steps {
                echo "Executing pipeline for branch: ${env.BRANCH_NAME}"
                sh "mvn test"
            }
        }
        stage("build") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    echo "Building the application...."
                    sh "mvn package"
                }
            }
        }

        stage("deploy") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    echo "Deploying the application...."
                    sh '''
                        docker build -t java-maven-app .
                        docker images
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline completed for branch: ${env.BRANCH_NAME}"
        }
    }
}

        }               
    }
}
