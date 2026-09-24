pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
    }

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
                    def gv = load "script.groovy"
                    gv.buildJar()
                }
            }
        }

        stage("docker-build-push") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    def gv = load "script.groovy"
                    gv.buildImage(params.IMAGE_TAG)
                }
            }
        }

        stage("deploy") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    def gv = load "script.groovy"
                    gv.deployApp()
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

