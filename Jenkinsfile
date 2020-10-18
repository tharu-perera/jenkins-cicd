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
def approvedBy


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
        stage('checking build type') {
            steps {
                sh 'printenv'
                script {
                    /* ---------------- PR requests section [just build and send build status]--------------------*/
                    if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET == "develop") {
                        TYPE = "DEV_PR"
                    } else if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET.startsWith('release')) {
                        //release bug fixing PR
                        TYPE = "RELEASE_PR"
                    } else if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET.startsWith('hotfix')) {
                        //hotfix fixing PR
                        TYPE = "HOTFIX_PR"
                    } else if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET.startsWith('master') && env.CHANGE_BRANCH.startsWith('release')) {
                        //prod release PR
                        TYPE = "PROD_PR"
                    } else if (env.JOB_BASE_NAME.startsWith('PR') && env.CHANGE_TARGET.startsWith('master') && env.CHANGE_BRANCH.startsWith('hotfix')) {
                        // hotfix prod release PR
                        TYPE = "HOTFIX_PROD_PR"

                        /* ---------------- release requests [ get the approval ,checkout and release ] --------------------*/
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'qa') {
                        // qa release request
                        TYPE = "QA_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'staging') {
                        // prep release request
                        TYPE = "STAGE_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'dev') {
                        // dev release request
                        TYPE = "DEV_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'prod') {
                        // prod release request with LATEST tag
                        TYPE = "PROD_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'hotfix-qa') {
                        // hotfix qa release request
                        TYPE = "HOTFIX_QA_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'hotfix-staging') {
                        // hotfix staging release request
                        TYPE = "HOTFIX_STAGING_RELEASE_REQ"

                        /* ---------------- automatic release requests [ get the approval ,checkout and release ]--------------------*/
                    } else if (env.JOB_BASE_NAME == "develop") {
                        // start dev release. no need approval .
                        TYPE = "DEV_RELEASE"
                    } else if (env.JOB_BASE_NAME.startsWith('release')) {
                        // qa  release request. need approval
                        TYPE = "QA_RELEASE"
                    } else if (env.JOB_BASE_NAME.startsWith('master')) {
                        // tag and start prod"  release request .need approval.check last merged branch .if hot fix bumpup hotfix version
                        TYPE = "PROD_RELEASE"
                    } else if (env.JOB_BASE_NAME.startsWith('hotfix')) {
                        //  qa  release request. need approval
                        TYPE = "HOTFIX_QA_RELEASE"

                        /* ---------------- branches create requests [ check , create and inform general]--------------------*/
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'create-release') {
                        // create release branch with tag
                        TYPE = "CREATE_RELEASE_BR"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'create-hotfix') {
                        // create hotfix branch with tag. check whether still have one
                        TYPE = "CREATE_HOTFIX_BR"

                    } else {
                        echo "<<<could not find the change type>>>"
                    }

                    echo "type ==  $TYPE"
                }
            }
        }

        stage('setting variables') {
            steps {
                script {
                    BUILD_USER = currentBuild.getBuildCauses()[0].shortDescription
                    COMMIT_HASH = sh(returnStdout: true, script: 'git rev-parse HEAD')
                    COMMIT_AUTHOR = sh(returnStdout: true, script: "git --no-pager show -s --format='%an' ${COMMIT_HASH}").trim()
                    COMMIT_MSG = sh(returnStdout: true, script: "git log --format=%B -n 1  ${COMMIT_HASH}").trim()
                    echo "BUILD_USER : ${BUILD_USER}"
                    echo "COMMIT_MSG: ${COMMIT_MSG}"
                    echo "COMMIT_AUTHOR: ${COMMIT_AUTHOR}"
                    echo "COMMIT_HASH: ${COMMIT_HASH}"
                }
            }
            post {
                failure {
                    notifySlackBuildFileItselfHasError("error", "var setting error", TYPE, "setting variables",)
                }
            }
        }


        stage(" ") {
            parallel {
//                stage('Branch Creation[Slack]') {
//                    when {
//                        expression { TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR" }
//                    }
//                    stages {
//                        stage('checking and create branch') {
//
//                            steps {
//                                script {
//                                    def br = ""
//                                    if (TYPE == "CREATE_RELEASE_BR") {
//                                        br = sh(returnStdout: true, script: './test.sh release').trim()
//                                        if (br == '1') {
//                                            errorReport(TYPE, "Branch Creation[Slack]", "$user_name requested to create RELEASE branch. But dev branch exists")
//                                            throw exception
//                                        } else {
//                                            sh 'git checkout -b release origin/develop'
//                                            sh 'git push origin release'
//                                        }
//                                    } else {
//                                        br = sh(returnStdout: true, script: './test.sh hotfix').trim()
//                                        if (br == '1') {
//                                            errorReport(TYPE, "Branch Creation[Slack]", "$user_name requested to create HOTFIX branch. But HOTFIX branch exists")
//                                            throw exception
//                                        } else {
//                                            sh 'git checkout -b hotfix origin/master'
//                                            sh 'git push origin hotfix'
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }

                stage("non branch") {
                    when {
                        expression { TYPE != "CREATE_RELEASE_BR" && TYPE != "CREATE_HOTFIX_BR" }
                    }
                    stages {

//                        stage('checkout code when on request release[Slack]') {
//                            when {
//                                expression { TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ" }
//                            }
//                            steps {
//                                step([$class: 'WsCleanup'])
//                                checkout scm
//                                script {
//                                    def errorDesc = ""
//                                    try {
//                                        if (TYPE == "QA_RELEASE_REQ") {
//                                            errorDesc = "[User:$user_name] QA release from release branch request job is failed"
//                                            sh 'git checkout origin/release'
//                                        } else if (TYPE == "STAGE_RELEASE_REQ") {
//                                            errorDesc = "[User:$user_name] Staging release from release branch request job is failed"
//                                            sh 'git checkout origin/release'
//                                        } else if (TYPE == "DEV_RELEASE_REQ") {
//                                            errorDesc = "[User:$user_name] Dev release request job is failed"
//                                            sh 'git checkout origin/develop'
//                                        } else if (TYPE == "PROD_RELEASE_REQ") {
//                                            errorDesc = "[User:$user_name] PROD release from master branch request job is failed"
//                                            sh 'git checkout origin/master'
//                                        } else if (TYPE == "HOTFIX_QA_RELEASE_REQ") {
//                                            errorDesc = "[User:$user_name] Hotfix QA release from hotfix branch request job is failed"
//                                            sh 'git checkout origin/hotfix'
//                                        } else if (TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
//                                            errorDesc = "[User:$user_name] Hotfix Staging release from hotfix branch request job is failed"
//                                            sh 'git checkout origin/hotfix'
//                                        }
//                                    } catch (exception) {
//                                        errorReport(TYPE, "checkout code when on request release[Slack]", "$errorDesc")
//                                        throw exception
//                                    } finally {
//                                        sh 'cat README.md'
//                                    }
//                                }
//                            }
//                        }
//
//                        stage('build ') {
//                            steps {
//                                script {
//                                    try {
//                                        sh "./gradlew clean build -x test -x check"
//                                    } catch (exception) {
//                                        errorReport(TYPE, "Build", exception)
//                                        throw exception
//                                    }
//                                }
//                            }
//                        }
//
//                        stage('Junit & Jacoco') {
//                            steps('running junit') {
//                                script {
//                                    try {
//                                        sh 'chmod +x gradlew'
//                                        sh './gradlew test jacocoTestReport --no-daemon'
//                                        // if in case tests fail then subsequent stages
//                                        // will not run .but post block in this stage will run
//                                        step([$class          : 'JacocoPublisher',
//                                              execPattern     : '**/build/jacoco/*.exec',
//                                              classPattern    : '**/build/classes',
//                                              sourcePattern   : 'src/main/java',
//                                              exclusionPattern: 'src/test*'
//                                        ])
//                                        publishHTML target: [
//                                                allowMissing         : false,
//                                                alwaysLinkToLastBuild: false,
//                                                keepAll              : true,
//                                                reportDir            : "build/reports/tests/test",
//                                                reportFiles          : 'index.html',
//                                                reportName           : 'Junit Report'
//                                        ]
//                                    }
//                                    catch (exception) {
//                                        echo "$exception"
//                                        errorReport(TYPE, "Junit", exception)
//                                        throw exception
//                                    }
//                                    finally {
//                                        summary = junit testResults: '**/build/test-results/test/*.xml'
//                                        echo "test >>> ${summary.getProperties()}"
//                                    }
//                                }
//                            }
//                        }
//
//                        stage('Checkstyle') {
//                            steps {
//                                script {
//                                    try {
//                                        sh "./gradlew checkstyleMain checkstyleTest"
//                                    } catch (exception) {
//                                        errorReport(TYPE, "Checkstyle", exception)
//                                        throw exception
//                                    } finally {
//                                        publishHTML target: [
//                                                allowMissing         : false,
//                                                alwaysLinkToLastBuild: false,
//                                                keepAll              : true,
//                                                reportDir            : "build/reports/checkstyle",
//                                                reportFiles          : '**/*',
//                                                reportName           : 'Checkstyle Report'
//                                        ]
//                                    }
//                                }
//                            }
//                        }
//
//                        stage('PMD') {
//                            steps {
//                                script {
//                                    try {
//                                        sh "./gradlew pmdmain pmdtest"
//                                    } catch (exception) {
////                                        errorReportToSlack(TYPE, "PMD", exception)
////                                        throw exception
//                                    } finally {
//                                        publishHTML target: [
//                                                allowMissing         : false,
//                                                alwaysLinkToLastBuild: false,
//                                                keepAll              : true,
//                                                reportDir            : "build/reports/pmd",
//                                                reportFiles          : 'main.html,test.html',
//                                                reportName           : 'PMD Report'
//                                        ]
//                                    }
//                                }
//                            }
//                        }
//
//
//                        stage('SQ analysis') { //there are 2 ways to configure sonar in jenkins
//                            //one method usingg jenkins global configuration
//                            steps {
//                                script {
////                    def scannerHome = tool 'SonarScanner 4.0';
////                    withSonarQubeEnv('mysona') { // If you have configured more than one global server connection, you can specify its name
////                        sh "${scannerHome}/bin/sonar-scanner"
////                    }
//                                    //other one is using gradle build
//                                    withSonarQubeEnv() {
//                                        // Will pick the global server connection you have configured
//                                        sh "./gradlew sonarqube -Dsonar.projectName=${TYPE}"
//                                    }
//                                    timeout(time: 1, unit: 'HOURS') {
//                                        // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
//                                        // true = set pipeline to UNSTABLE, false = don't
//                                        waitForQualityGate abortPipeline: true
//                                    }
//                                }
//                            }
//                            post {
//                                unstable {
//                                    errorReport(TYPE, "SQ", "eroor")
//                                }
//                            }
//                        }


                        stage('On request release approval') {
                            when {
                                expression { TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ" }
                            }
                            steps {
                                script {
                                    try {
                                        timeout(time: 10, unit: "MINUTES") {
                                            getApproval(TYPE, "$user_name")
                                            approvedBy = input id: 'reqApproval', message: "$user_name requested a $TYPE ",
                                                    ok: 'Approve?',
//                                                submitter: 'user1,user2,group1',
                                                    submitterParameter: 'APPROVER'

                                            echo "This build was approved by: ${env.USER}"
                                            echo "This build was approved by: ${approvedBy}"
                                            approvedNotify(TYPE, "${env.USER}")
                                        }
                                        echo ">>><<<<"
                                    } catch (exception) {
                                        echo " rejected>>>>${env.USER}"
                                        echo "This build was rejected by: ${approvedBy}"

                                        rejectedNotify(TYPE, "${env.USER}" )
                                        throw  exception
                                    }
                                }
                            }
                        }

                        stage('commit merged auto release approval') {
                            when {
                                expression { TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE" }
                            }
                            steps {
                                echo 'get permison for commit merged auto release'
                            }
                        }
                    }
                }

            }
        }
        stage("send build status") {
            steps {
                script {
                    echo "sending final build status"
                    successReport(TYPE)
                }
            }
        }
    }
}

def successReport(TYPE) {
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

def errorReport(TYPE, stage, errorInfo) {
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
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\": \"error=${error}   type= ${type}   stage=${stage}\"}'"
        }
    }

}

def notifySlackSuccess(channel, type, msg) {
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\"  : \"msg=${msg}   type= ${type} \"}'"
        }
    }
}

def notifySlackBuildFileItselfHasError(channel, error, type, stage) {
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\": \"error=${error} : type= ${type} : stage=${stage}\"}'"
        }
    }
}

def getApproval(type, user) {
    def channel = "admin"
    def msg = "need approval for release"
    withCredentials([string(credentialsId: 'slack-token', variable: 'st')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\"  : \"msg=${msg}   type= ${type} \"}'"
        }
    }
}

def approvedNotify(type, user) {
    def channel = "general"
    def msg = "approved for release $user"
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\" :  \"msg=${msg}   type= ${type} \"}'"
        }
    }
}

def rejectedNotify(type, user) {
    def channel = "general"
    def msg = "rejected release $user"
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\" :  \"msg=${msg}   type= ${type} \"}'"
        }
    }
}