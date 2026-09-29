@Library('jenkins-shared-lib') _
pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
    }

    stages {

        /* --------------------------------------------------------------------
         * SECTION A — AUTO VERSION BUMP
         *
         * REAL-WORLD NOTES:
         * 1. Maven only increments versions reliably when the current version
         *    ends with "-SNAPSHOT".
         *
         * 2. Jenkins multibranch pipelines DO NOT automatically reload the
         *    updated pom.xml after Maven commits a new version.
         *    This is why a second bump would fail unless we explicitly pull
         *    the updated pom.xml before running Maven again.
         *
         * 3. Maven variables (${parsedVersion.majorVersion}, etc.) MUST be
         *    escaped as \\${...} inside sh ''' ... ''' blocks.
         *    Otherwise the shell tries to expand them → "Bad substitution".
         *
         * 4. We ALWAYS re-add "-SNAPSHOT" after bumping so future bumps keep
         *    working. This produces continuous version increments:
         *
         *      1.1.0-SNAPSHOT → 1.1.1-SNAPSHOT
         *      1.1.1-SNAPSHOT → 1.1.2-SNAPSHOT
         *      1.1.2-SNAPSHOT → 1.1.3-SNAPSHOT
         *
         * ------------------------------------------------------------------ */
        stage("increment-version") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                script {
                    echo "Incrementing application version..."

                    // IMPORTANT:
                    // Reload updated pom.xml from previous bump commit.
                    // Without this, Maven sees the old version and refuses to bump again.
                    sh 'git pull'

                    sh '''
                        mvn build-helper:parse-version versions:set \
                        -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion}-SNAPSHOT \
                        versions:commit
                    '''
                }
            }
        }

        /* --------------------------------------------------------------------
         * SECTION B — READ VERSION AND STORE IN PIPELINE VARIABLE
         *
         * Reads the current Maven version (including -SNAPSHOT) and stores it
         * in APP_VERSION. This ensures:
         *
         *  - JAR build uses the correct version
         *  - Docker image tag matches the Maven version
         *  - Deployment uses the exact same version
         *
         * ------------------------------------------------------------------ */
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

        /* --------------------------------------------------------------------
         * SECTION C — TEST (RUNS ON ALL BRANCHES)
         *
         * Tests do not depend on versioning, so they run for every branch.
         * ------------------------------------------------------------------ */
        stage("test") {
            steps {
                echo "Executing pipeline for branch: ${env.BRANCH_NAME}"
                sh "mvn test"
            }
        }

        /* --------------------------------------------------------------------
         * SECTION D — BUILD JAR USING BUMPED VERSION
         *
         * Only master builds the final artifact.
         * ------------------------------------------------------------------ */
        stage("build") {
            when {
                expression { env.BRANCH_NAME == "master" }
            }
            steps {
                buildJar()
            }
        }

        /* --------------------------------------------------------------------
         * SECTION E — DOCKER BUILD + PUSH USING BUMPED VERSION
         *
         * Uses APP_VERSION (including -SNAPSHOT) as the Docker tag.
         * This keeps Docker Hub perfectly aligned with Maven.
         * ------------------------------------------------------------------ */
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

        /* --------------------------------------------------------------------
         * SECTION F — DEPLOY USING BUMPED VERSION
         *
         * Deploys the exact version that was built and pushed.
         * ------------------------------------------------------------------ */
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

