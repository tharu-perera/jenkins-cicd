#!groovy

enum Type {
    DEV_PR,
    RELEASE_PR,
    HOTFIX_PR,
    PROD_PR,
    HOTFIX_PROD_PR,
    QA_RELEASE_REQ,
    STAGE_RELEASE_REQ,
    DEV_RELEASE_REQ,
    PROD_RELEASE_REQ,
    HOTFIX_QA_RELEASE_REQ,
    HOTFIX_STAGING_RELEASE_REQ,
    CREATE_RELEASE_BR,
    CREATE_HOTFIX_BR,
    DEV_RELEASE,
    QA_RELEASE,
    PROD_RELEASE,
    HOTFIX_QA_RELEASE
}

def AUTHOR = ""
def BUILD_USER = ""
def type = null


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
        stage('identifying build type') {
            steps {
                sh 'printenv'
                script {
                    echo ">>getBuildUser>>>>>"
                    echo ">>${env.par1}>>>>>"
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

                    try {
                        if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET == "develop") {
                            //develop PR
                            type = Type.DEV_PR
                        } else if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET.startsWith('release')) {
                            //release bug fixing PR
                            type = Type.RELEASE_PR
                        } else if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET.startsWith('hotfix')) {
                            //hotfix fixing PR
                            type = Type.HOTFIX_PR
                        } else if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET.startsWith('master') && env.CHANGE_BRANCH.startsWith('release')) {
                            //prod release PR
                            type = Type.PROD_PR
                        } else if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET.startsWith('master') && env.CHANGE_BRANCH.startsWith('hotfix')) {
                            // hotfix prod release PR
                            type = Type.HOTFIX_PROD_PR
                        } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'qa') {
                            // qa release request
                            type = Type.QA_RELEASE_REQ
                        } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'staging') {
                            // prep release request
                            type = Type.STAGE_RELEASE_REQ
                        } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'dev') {
                            // dev release request
                            type = Type.DEV_RELEASE_REQ
                        } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'prod') {
                            // prod release request with LATEST tag
                            type = Type.PROD_RELEASE_REQ
                        } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'hotfix-qa') {
                            // hotfix qa release request
                            type = Type.HOTFIX_QA_RELEASE_REQ
                        } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'hotfix-staging') {
                            // hotfix staging release request
                            type = Type.HOTFIX_STAGING_RELEASE_REQ
                        } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'create-release') {
                            // create release branch with tag
                            type = Type.CREATE_RELEASE_BR
                        } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'create-hotfix') {
                            // create hotfix branch with tag. check whether still have one
                            type = Type.CREATE_HOTFIX_BR
                        } else if (env.JOB_BASE_NAME == "develop") {
                            // start dev release. no need approval .
                            type = Type.DEV_RELEASE
                        } else if (env.JOB_BASE_NAME.startsWith('release')) {
                            // qa  release request. need approval
                            type = Type.QA_RELEASE
                        } else if (env.JOB_BASE_NAME.startsWith('master')) {
                            // tag and start prod  release request .need approval.check last merged branch .if hot fix bumpup hotfix version
                            type = Type.PROD_RELEASE
                        } else if (env.JOB_BASE_NAME.startsWith('hotfix')) {
                            //  qa  release request. need approval
                            type = Type.HOTFIX_QA_RELEASE
                        } else {
                            echo "<<<could not find the change type>>>"
                        }

                        echo "type ==  $type"
                    }catch(Exception exception){
                        echo "${exception.toString()}"
                    }
                }
            }
        }

//        stage('build & test') {
//            steps {
//                notifySlack()
//                sh "./gradlew clean build -x test"
//            }
//
//            post {
//                failure {
//                    echo 'build & test error'
//                    slackSend channel: 'error',
//                            color: 'good',
//                            message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"
//
//                }
//            }
//        }
//
//        stage('unit test') {
//            steps {
//                notifySlack()
//                sh "./gradlew test"
//                step $class: 'JUnitResultArchiver', testResults: '**/TEST-*.xml'
//            }
//
//            post {
//                failure {
//                    echo 'test error'
//                    slackSend channel: 'error',
//                            color: 'good',
//                            message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"
//
//                }
//            }
//        }
//
//        stage('getting approval for qa release') {
//            when { branch 'develop' }
//            steps {
//                echo 'getting approval for qa release'
//                slackSend channel: 'qa-release-approval',
//                        color: 'good',
//                        message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"
//
//            }
//        }
//
//        stage('inform  build status to slack ') {
//            steps {
//                echo 'inform  build status to slack'
//            }
//        }
//    }

//    post { // these post steps will get executed at the end of build
//        always {
//            echo ' post outside stages always '
//            sh "echo ${currentBuild.result}"
//        }
//        failure {
//            echo ' post outside stages failure '
//            sh "echo ${currentBuild.result}"
//        }

    }
//    } catch (e) {
//       echo 'Error in pipeline'
//        slackSend channel: 'general',
//                color: 'good',
//                message: "Error in pipelin [${e.toString()}]"
//
//    }

}

def notifySlack() {
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{\n" +
                    "\"blocks\": [\n" +
                    "{\n" +
                    "\"type\": \"section\",\n" +
                    "\"text\": {\n" +
                    "\"type\": \"mrkdwn\",\n" +
                    "\"text\": \"Danny Torrence left the following review for your property:\"\n" +
                    "}\n" +
                    "},\n" +
                    "{\n" +
                    "\"type\": \"section\",\n" +
                    "\"block_id\": \"section567\",\n" +
                    "\"text\": {\n" +
                    "\"type\": \"mrkdwn\",\n" +
                    "\"text\": \"<https://example.com|Overlook Hotel> \\n :star: \\n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s.\"\n" +
                    "},\n" +
                    "\"accessory\": {\n" +
                    "\"type\": \"image\",\n" +
                    "\"image_url\": \"https://is5-ssl.mzstatic.com/image/thumb/Purple3/v4/d3/72/5c/d3725c8f-c642-5d69-1904-aa36e4297885/source/256x256bb.jpg\",\n" +
                    "\"alt_text\": \"Haunted hotel image\"\n" +
                    "}\n" +
                    "},\n" +
                    "{\n" +
                    "\"type\": \"section\",\n" +
                    "\"block_id\": \"section789\",\n" +
                    "\"fields\": [\n" +
                    "{\n" +
                    "\"type\": \"mrkdwn\",\n" +
                    "\"text\": \"*Average Rating*\\n1.0\"\n" +
                    "}\n" +
                    "]\n" +
                    "},\n" +
                    "{\n" +
                    "\"type\": \"actions\",\n" +
                    "\"elements\": [\n" +
                    "{\n" +
                    "\"type\": \"button\",\n" +
                    "\"text\": {\n" +
                    "\"type\": \"plain_text\",\n" +
                    "\"text\": \"Reply to review\",\n" +
                    "\"emoji\": false\n" +
                    "}\n" +
                    "}\n" +
                    "]\n" +
                    "},\n" +
                    "{\n" +
                    "\"type\": \"section\",\n" +
                    "\"text\": {\n" +
                    "\"type\": \"mrkdwn\",\n" +
                    "\"text\": \"This is a section block with a button.\"\n" +
                    "},\n" +
                    "\"accessory\": {\n" +
                    "\"type\": \"button\",\n" +
                    "\"text\": {\n" +
                    "\"type\": \"plain_text\",\n" +
                    "\"text\": \"Click Me\",\n" +
                    "\"emoji\": true\n" +
                    "},\n" +
                    "\"value\": \"click_me_123\",\n" +
                    "\"url\": \"https://google.com\",\n" +
                    "\"action_id\": \"button-action\"\n" +
                    "}\n" +
                    "},\n" +
                    "{\n" +
                    "\"type\": \"section\",\n" +
                    "\"text\": {\n" +
                    "\"type\": \"plain_text\",\n" +
                    "\"text\": \"This is a plain text section block.\",\n" +
                    "\"emoji\": true\n" +
                    "}\n" +
                    "}\n" +
                    "]\n" +
                    "}'"
        }
    }

}
