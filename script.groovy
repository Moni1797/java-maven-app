def buildJar() {
    echo 'building the application...'
    sh 'mvn clean package'
}

def buildImage(String tag) {
    echo "building the docker image with tag: ${tag}"

    withCredentials([usernamePassword(
        credentialsId: 'docker-hub-repo',
        usernameVariable: 'USER',
        passwordVariable: 'PASS'
    )]) {
        sh "docker build -t munibawan/demo-app:${tag} ."
        sh "echo $PASS | docker login -u $USER --password-stdin"
        sh "docker push munibawan/demo-app:${tag}"
    }
}

def deployApp() {
    echo 'deploying the application...'
}

return this
