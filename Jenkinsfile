@Library('jenkins-shared-lib') _
pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
    }

    stages {

        // SECTION A — AUTO VERSION BUMP
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

        // SECTION B — READ VERSION AND STORE IN PIPELINE VARIABLE
        stage("read-version") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    env.APP_VERSION = readMavenVersion()
                    echo "Application version: ${env.APP_VERSION}"
                }
            }
        }

        // TEST ALWAYS RUNS
        stage("test") {
            steps {
                echo "Executing pipeline for branch: ${env.BRANCH_NAME}"
                sh "mvn test"
            }
        }

        // BUILD JAR USING BUMPED VERSION
        stage("build") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                buildJar()
            }
        }

        // DOCKER BUILD + PUSH USING BUMPED VERSION
        stage("docker-build-push") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    dockerLogin()
                    buildImage(env.APP_VERSION)
                    dockerPush(env.APP_VERSION)
                }
            }
        }

        // DEPLOY USING BUMPED VERSION
        stage("deploy") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                deployApp(env.APP_VERSION)
            }
        }
    }

    post {
        always {
            echo "Pipeline completed for branch: ${env.BRANCH_NAME}"
        }
    }
}
