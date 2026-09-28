@Library('jenkins-shared-lib') _
pipeline {
    agent any
    tools {
        maven 'Maven-3.9.6'
    }
    stages {

        // ⭐ NEW STAGE GOES HERE — BEFORE ANY BUILDING HAPPENS
        stage("increment-version") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    echo "Incrementing application version..."

                    sh '''
                        mvn build-helper:parse-version versions:set \
                        -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} \
                        versions:commit
                    '''
                }
            }
        }

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
                buildJar()
            }
        }

        stage("docker-build-push") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    def tag = env.GIT_COMMIT.take(7)
                    dockerLogin()
                    buildImage(tag)
                    dockerPush(tag)
                }
            }
        }

        stage("deploy") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                deployApp()
            }
        }
    }

    post {
        always {
            echo "Pipeline completed for branch: ${env.BRANCH_NAME}"
        	}
	}
}
