stage('build') {
    node('package') {
        sh "rm -rf *"
        checkout scm
        sh 'mvn clean install -B'
        sh 'mvn sonar:sonar -Dsonar.projectKey=dryad-naiad-software_sethlans -Dsonar.organization=dryad-naiad-software -Dsonar.host.url=https://sonarcloud.io  -Dsonar.login=30e23e48f553208accea7aca0587602c4aa28b7b -B'
        archiveArtifacts artifacts: '**/target/binaries/*.*', fingerprint: true
    }
    emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
            replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
            to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                    [$class: 'RequesterRecipientProvider']]))
}