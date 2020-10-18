#!groovy
def COLOR_MAP = ['SUCCESS': 'good', 'FAILURE': 'danger',]

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

def COMMIT_AUTHOR = ""
def BUILD_USER = ""
def COMMIT_MSG = ""
def TYPE = ""
def summary = ""
def COMMIT_HASH = ""
def getGitAuthor = {
    def commit = sh(returnStdout: true, script: 'git rev-parse HEAD')
    def commit2 = sh(returnStdout: true, script: 'git branch --list  develop')
    sh 'git branch --list  develop'
    echo "????? $commit   "
    echo "?????### $commit2 "
}
//TODO chnageset  ,  changelog, try catch bloc , send test summary, sonar summary ,
def approvalMap             // collect data from approval step

pipeline {
//    try{
    agent any
    options {
//        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '1'))
    }

    stages {
//        stage ("Long Running Stage") {
//            steps {
//
//                script {
//                    def hook = registerWebhook()
//
//                    echo "Waiting for POST to ${hook.getURL()}"
//                    requestApproval(hook.getURL())
//                    def  data = waitForWebhook hook
//                    echo "Webhook called with data: ${data}"
//                }
//            }
//        }

        stage('On request release approval') {

            steps {
                sh 'printenv'
                echo "get permison for On request release <<$par1>> "
                echo "get permison for On request release <<$channel_id>> "
                echo "get permison for On request release <<$user_name>> "
                // Call a remote system to start execution, passing a callback url
                echo " ${env.BUILD_URL}input/Async-input/proceedEmpty "
                echo " ${env.BUILD_URL}input/Async-input/proceedEmpty "
//send approval messgae with  linkn to this page

                    timeout(time: 10, unit: "MINUTES") {
                        script {
                            // capture the approval details in approvalMap.
                            approvalMap = input id: 'test',  message: 'Hello',
                            ok: 'Proceed?',
                            parameters: [
                                    choice(
                                            choices: 'apple\npear\norange',
                                            description: 'Select a fruit for this build',
                                            name: 'FRUIT'
                                    ),
                                    string(
                                            defaultValue: '',
                                            description: '',
                                            name: 'myparam'
                                    )
                            ],
                            submitter: 'user1,user2,group1',
                            submitterParameter: 'APPROVER'

                        }
                    }


            }
        }
        stage('Stage 2') {
            agent any

            steps {
                // print the details gathered from the approval
                echo "This build was approved by: ${approvalMap['APPROVER']}"
                echo "This build is brought to you today by the fruit: ${approvalMap['FRUIT']}"
                echo "This is myparam: ${approvalMap['myparam']}"
            }
        }

    }
}

def requestApproval(callBack){
    def channel="release-approval"
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\": \"wedwed\"}'"
        }
    }
}



def successReportToSlack(TYPE) {
    echo " type =$TYPE"
    if (TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR") {
        notifySlackSuccess("general", TYPE, "release/hotfix branch created")
    } else if (TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        notifySlackSuccess("admin", TYPE, "Release done on request")
    } else if (TYPE == "DEV_PR" || TYPE == "RELEASE_PR" || TYPE == "HOTFIX_PR" || TYPE == "PROD_PR" || TYPE == "HOTFIX_PROD_PR") {
        notifySlackSuccess("pull-request", TYPE, "PR status is ok")
    } else if (TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE") {
        notifySlackSuccess("general", TYPE, "merged commit released")
    }
}

def errorReportToSlack(TYPE, stage, errorInfo) {
    echo " type =$TYPE  info = $errorInfo  stage = $stage"
    if (TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR") {
        notifySlackError("admin", errorInfo, TYPE, stage)
    } else if (TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        notifySlackError("admin", errorInfo, TYPE, stage)
    } else if (TYPE == "DEV_PR" || TYPE == "RELEASE_PR" || TYPE == "HOTFIX_PR" || TYPE == "PROD_PR" || TYPE == "HOTFIX_PROD_PR") {
        notifySlackError("pull-request", errorInfo, TYPE, stage)
    } else if (TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE") {
        notifySlackError("error", errorInfo, TYPE, stage)
    }
}

def notifySlackError(channel, error, type, stage) {
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\": \"error=${error} : type= ${type} : stage=${stage}\"}'"
        }
    }

}

def notifySlackSuccess(channel, type, msg) {
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\": \"msg=${msg} : type= ${type} \"}'"
        }
    }

}