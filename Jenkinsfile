#!groovy
import groovy.json.JsonOutput

def author = ""
def DEPLOY_QA = 'qa'
def buildCause = ""
def BUILD_USER = ""
def SLACK_ACCESS_KEY = ""
def jen = ""

def notifySlack(text, channel, attachments) {

    sh "curl --location --request POST '$SLACK_ACCESS_KEY' " +
            "--header 'Content-Type: application/json' \\\n" +
            "--data-raw '" +
            "{\n" +
            "\t\"blocks\": [\n" +
            "\t\t{\n" +
            "\t\t\t\"type\": \"section\",\n" +
            "\t\t\t\"text\": {\n" +
            "\t\t\t\t\"type\": \"mrkdwn\",\n" +
            "\t\t\t\t\"text\": \"Danny Torrence left the following review for your property:\"\n" +
            "\t\t\t}\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"type\": \"section\",\n" +
            "\t\t\t\"block_id\": \"section567\",\n" +
            "\t\t\t\"text\": {\n" +
            "\t\t\t\t\"type\": \"mrkdwn\",\n" +
            "\t\t\t\t\"text\": \"<https://example.com|Overlook Hotel> \\n :star: \\n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s.\"\n" +
            "\t\t\t},\n" +
            "\t\t\t\"accessory\": {\n" +
            "\t\t\t\t\"type\": \"image\",\n" +
            "\t\t\t\t\"image_url\": \"https://is5-ssl.mzstatic.com/image/thumb/Purple3/v4/d3/72/5c/d3725c8f-c642-5d69-1904-aa36e4297885/source/256x256bb.jpg\",\n" +
            "\t\t\t\t\"alt_text\": \"Haunted hotel image\"\n" +
            "\t\t\t}\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"type\": \"section\",\n" +
            "\t\t\t\"block_id\": \"section789\",\n" +
            "\t\t\t\"fields\": [\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"type\": \"mrkdwn\",\n" +
            "\t\t\t\t\t\"text\": \"*Average Rating*\\n1.0\"\n" +
            "\t\t\t\t}\n" +
            "\t\t\t]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"type\": \"actions\",\n" +
            "\t\t\t\"elements\": [\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"type\": \"button\",\n" +
            "\t\t\t\t\t\"text\": {\n" +
            "\t\t\t\t\t\t\"type\": \"plain_text\",\n" +
            "\t\t\t\t\t\t\"text\": \"Reply to review\",\n" +
            "\t\t\t\t\t\t\"emoji\": false\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"type\": \"section\",\n" +
            "\t\t\t\"text\": {\n" +
            "\t\t\t\t\"type\": \"mrkdwn\",\n" +
            "\t\t\t\t\"text\": \"This is a section block with a button.\"\n" +
            "\t\t\t},\n" +
            "\t\t\t\"accessory\": {\n" +
            "\t\t\t\t\"type\": \"button\",\n" +
            "\t\t\t\t\"text\": {\n" +
            "\t\t\t\t\t\"type\": \"plain_text\",\n" +
            "\t\t\t\t\t\"text\": \"Click Me\",\n" +
            "\t\t\t\t\t\"emoji\": true\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"value\": \"click_me_123\",\n" +
            "\t\t\t\t\"url\": \"https://google.com\",\n" +
            "\t\t\t\t\"action_id\": \"button-action\"\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}" +
            "'"
}


//TODO chnageset  ,  changelog, try catch bloc , send test summary, sonar summary ,
pipeline {
//    try{
    agent any
    options {
//        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '1'))
    }

    // =============== stages====================
    stages {

        stage('preparation') {

            steps {
                withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
                    echo ">>SLACK_ACCESS_KEY >>>>>${st}"
                    echo ">>jen >>>>>${jenn}"
                         script {
                              jen=jenn
                             SLACK_ACCESS_KEY=st
                          }

                }
                script {
                    notifySlack("qd","wed","wed")
                    echo ">>getBuildUser>>>>>"
                    echo ">>SLACK_ACCESS_KEY >>>>>${SLACK_ACCESS_KEY}"
                    echo ">>jen >>>>>${jen}"
                    echo " git branch  ${env.GIT_BRANCH}  "
                    echo "${currentBuild.getBuildCauses()}"
                    echo "${currentBuild.buildCauses}" // same as currentBuild.getBuildCauses()
                    echo "${currentBuild.getBuildCauses('hudson.model.Cause$UserCause')}"
                    echo "${currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause')}"

                    // started by commit
                    echo "${currentBuild.getBuildCauses('jenkins.branch.BranchEventCause')}"
// started by timer
                    echo "${currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause')}"
// started by user
                    echo "${currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')}"
                    echo ">>getBuildUser> ENd >>>>"
                    // get build cause (time triggered vs. SCM change)
                    BUILD_USER = currentBuild.getBuildCauses()[0].shortDescription
                    echo "Current build was caused by: ${BUILD_USER}\n"

                }
            }
        }

        stage('build & test') {
            steps {
                sh "./gradlew clean build test"
                step $class: 'JUnitResultArchiver', testResults: '**/TEST-*.xml'
            }

            post {
                failure {
                    echo 'build & test error'
                    slackSend channel: 'error',
                            color: 'good',
                            message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"

                }
            }
        }

        stage('getting approval for qa release') {
            when { branch 'develop' }
            steps {
                echo 'getting approval for qa release'
                slackSend channel: 'qa-release-approval',
                        color: 'good',
                        message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"

            }
        }

        stage('inform  build status to slack ') {
            steps {
                echo 'inform  build status to slack'
                script {
                    def slackURL = SLACK_ACCESS_KEY
                    def jenkinsIcon = 'https://wiki.jenkins-ci.org/download/attachments/2916393/logo.png'

                    def payload = JsonOutput.toJson([text    : "xxxxxxx",
                                                     channel : "general",
                                                     username: "Jenkins",
                                                     icon_url: jenkinsIcon
                    ])

                    sh "curl -X POST --data-urlencode \'payload=${payload}\' ${slackURL}"

                }

                slackSend channel: 'general',
                        color: 'good',
                        message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"

            }
        }
    }

    post { // these post steps will get executed at the end of build
        always {
            echo ' post outside stages always '
            sh "echo ${currentBuild.result}"
        }
        failure {
            echo ' post outside stages failure '
            sh "echo ${currentBuild.result}"
        }

    }
//    } catch (e) {
//       echo 'Error in pipeline'
//        slackSend channel: 'general',
//                color: 'good',
//                message: "Error in pipelin [${e.toString()}]"
//
//    }

}
