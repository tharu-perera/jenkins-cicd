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
pipeline {
//    try{
    agent any
    options {
//        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '1'))
    }

    stages {

        stage('On request release approval') {

            steps {
                echo "get permison for On request release <<$par1>> "
                // Call a remote system to start execution, passing a callback url
                echo " ${env.BUILD_URL}input/Async-input/proceedEmpty "

                timeout(time: 15, unit: "MINUTES") {
                    input id: 'Async-input', message: 'Waiting for remote system'
                    input message: 'Do you want to approve the deploy in production?', ok: 'Yes'
                }

            }
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