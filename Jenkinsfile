@Library('jenkins-shared-lib') _
pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
    }

    stages {

        // SECTION A — AUTO VERSION BUMP
        //
        // IMPORTANT:
        //  - Maven variables like ${parsedVersion.majorVersion} must NOT be expanded by the shell.
        //  - We escape them as \\${...} inside the sh ''' ... ''' block.
        //  - Groovy eats the first backslash, the shell sees the second, Maven receives ${...}.
        //  - We ALWAYS re-add "-SNAPSHOT" so future bumps keep working.
        stage("increment-version") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    echo "Incrementing application version..."

                    sh '''
                        mvn build-helper:parse-version versions:set \
                        -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion}-SNAPSHOT \
                        versions:commit
                    '''
                    // Example flow:
                    //   1.1.0-SNAPSHOT -> 1.1.1-SNAPSHOT
                    //   1.1.1-SNAPSHOT -> 1.1.2-SNAPSHOT
                    //   1.1.2-SNAPSHOT -> 1.1.3-SNAPSHOT
                    // Without "-SNAPSHOT", the version would get stuck after the first bump.
                    // Without \\${...} escaping, the shell throws "Bad substitution".
                }
            }
        }

        // SECTION B — READ VERSION AND STORE IN PIPELINE VARIABLE
        //
        // Reads the current Maven version (including -SNAPSHOT) and exposes it as APP_VERSION.
        // This APP_VERSION is then used consistently for:
        //   - JAR build
        //   - Docker image tag
        //   - Deployment version
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

        // TEST ALWAYS RUNS (ALL BRANCHES)
        stage("test") {
            steps {
                echo "Executing pipeline for branch: ${env.BRANCH_NAME}"
                sh "mvn test"
            }
        }

        // BUILD JAR USING BUMPED VERSION (MASTER ONLY)
        stage("build") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                buildJar()
            }
        }

        // DOCKER BUILD + PUSH USING BUMPED VERSION (MASTER ONLY)
        //
        // Uses APP_VERSION (which includes -SNAPSHOT) as the image tag.
        // This keeps Docker tags aligned with Maven versions.
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

        // DEPLOY USING BUMPED VERSION (MASTER ONLY)
        //
        // Deploys the exact version that was built and pushed.
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

