stage('build') {
    node('package') {
        sh "rm -rf *"
        checkout scm
        sh 'mvn clean install -B'
        archiveArtifacts artifacts: '**/target/binaries/*.*', fingerprint: true
    }
    emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
            replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
            to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                    [$class: 'RequesterRecipientProvider']]))
}