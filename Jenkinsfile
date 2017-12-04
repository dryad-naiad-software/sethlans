/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

stage('compile') {
    node {
        sh "rm -rf *"
        checkout scm
        sh 'mvn clean compile'
    }
    if(currentBuild.currentResult == 'UNSTABLE' || currentBuild.currentResult == 'FAILURE') {
        emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
                replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
                to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                        [$class: 'RequesterRecipientProvider']]))
    }
}
stage('unitests') {
    parallel linux: {
        node('linux') {
            sh "rm -rf *"
            checkout scm
            sh 'mvn clean test'
            junit '**/target/surefire-reports/*.xml'
        }
        if(currentBuild.currentResult == 'UNSTABLE' || currentBuild.currentResult == 'FAILURE') {
            emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
                    replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
                    to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                            [$class: 'RequesterRecipientProvider']]))
        }
    }, windows: {
        node('windows') {
            sh "rm -rf *"
            checkout scm
            sh 'mvn clean test'
            junit '**/target/surefire-reports/*.xml'
        }
        if(currentBuild.currentResult == 'UNSTABLE' || currentBuild.currentResult == 'FAILURE') {
            emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
                    replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
                    to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                            [$class: 'RequesterRecipientProvider']]))
        }
    }, mac: {
        node('mac') {
            sh "rm -rf *"
            checkout scm
            sh 'mvn clean test'
            junit '**/target/surefire-reports/*.xml'
        }
        if(currentBuild.currentResult == 'UNSTABLE' || currentBuild.currentResult == 'FAILURE') {
            emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
                    replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
                    to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                            [$class: 'RequesterRecipientProvider']]))
        }
    }, failFast: false
}
stage('publish') {
    node('package') {
        sh "rm -rf *"
        checkout scm
        sh 'mvn clean package -DskipTests'
        archiveArtifacts artifacts: '**/target/binaries/*.jar, **/target/binaries/*.exe, **/target/binaries/*.dmg', fingerprint: true
    }
    emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
            replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
            to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                    [$class: 'RequesterRecipientProvider']]))
}