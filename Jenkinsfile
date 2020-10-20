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

COMMIT_AUTHOR = ""
BUILD_USER = ""
SLACK_USER = ""
COMMIT_MSG = ""
TYPE = ""
COMMIT_HASH = ""
approvedBy = ""
slackUserRequestedReleaseType = ""
autoTriggeredGitBranch = ""
slackUserCreatedBranch = ""
testsummary = ""
sonarLink = ""
pmdLink = ""
checkstyleLink = ""
testRpeortLink = ""
coverageRpeortLink = ""
GIT_PR_LINK = ""
br = ","

//TODO chnageset  ,  changelog, try catch bloc , send test summary, sonar summary ,

pipeline {
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
                        slackUserRequestedReleaseType = "QA release from RELEASE"
                        TYPE = "QA_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'staging') {
                        // prep release request
                        slackUserRequestedReleaseType = "STAGING release from RELEASE"
                        TYPE = "STAGE_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'dev') {
                        // dev release request
                        slackUserRequestedReleaseType = "DEV release from DEVELOP"
                        TYPE = "DEV_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'prod') {
                        // prod release request with LATEST tag
                        slackUserRequestedReleaseType = "PRODUCTION release from MASTER"
                        TYPE = "PROD_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'hotfix-qa') {
                        // hotfix qa release request
                        slackUserRequestedReleaseType = "QA release from HOTFIX"
                        TYPE = "HOTFIX_QA_RELEASE_REQ"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'hotfix-staging') {
                        // hotfix staging release request
                        slackUserRequestedReleaseType = "STAGING release from HOTFIX"
                        TYPE = "HOTFIX_STAGING_RELEASE_REQ"
                        /* ---------------- automatic release requests [ get the approval ,checkout and release ]--------------------*/
                    } else if (env.JOB_BASE_NAME == "develop") {
                        // start dev release. no need approval .
                        autoTriggeredGitBranch = "develop"
                        TYPE = "DEV_RELEASE"
                    } else if (env.JOB_BASE_NAME.startsWith('release')) {
                        // qa  release request. need approval
                        autoTriggeredGitBranch = "release"
                        TYPE = "QA_RELEASE"
                    } else if (env.JOB_BASE_NAME.startsWith('master')) {
                        // tag and start prod"  release request .need approval.check last merged branch .if hot fix bumpup hotfix version
                        autoTriggeredGitBranch = "master"
                        TYPE = "PROD_RELEASE"
                    } else if (env.JOB_BASE_NAME.startsWith('hotfix')) {
                        //  qa  release request. need approval
                        autoTriggeredGitBranch = "hotfix"
                        TYPE = "HOTFIX_QA_RELEASE"
                        /* ---------------- branches create requests [ check , create and inform general]--------------------*/
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'create-release') {
                        // create release branch with tag
                        slackUserCreatedBranch = "release"
                        TYPE = "CREATE_RELEASE_BR"
                    } else if (env.JOB_BASE_NAME == 'onrequest-release' && env.par1 == 'create-hotfix') {
                        // create hotfix branch with tag. check whether still have one
                        slackUserCreatedBranch = "hotfix"
                        TYPE = "CREATE_HOTFIX_BR"
                    } else {
                        echo "<<<could not find the change type>>>"
                        withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
                            script {
                                sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"error\", \"text\": \"Jenkins file error\"}'"
                            }
                        }
                        throw RuntimeException()
                    }

                    echo "Build trigger type ==  $TYPE"
                }
            }
        }

        stage('setting variables') {
            steps {
                script {
                    BUILD_USER = currentBuild.getBuildCauses()[0].shortDescription
                    SLACK_USER = env.user_name
                    GIT_PR_LINK = env.CHANGE_URL
                    COMMIT_HASH = sh(returnStdout: true, script: 'git rev-parse HEAD')
                    COMMIT_AUTHOR = sh(returnStdout: true, script: "git --no-pager show -s --format='%an' ${COMMIT_HASH}").trim()
                    COMMIT_MSG = sh(returnStdout: true, script: "git log --format=%B -n 1  ${COMMIT_HASH}").trim()
                    echo "BUILD_USER : ${BUILD_USER}"
                    echo "COMMIT_MSG: ${COMMIT_MSG}"
                    echo "SLACK_USER: ${SLACK_USER}"
                    echo "COMMIT_AUTHOR: ${COMMIT_AUTHOR}"
                    echo "COMMIT_HASH: ${COMMIT_HASH}"
                    echo "GIT_PR_LINK: ${GIT_PR_LINK}"
                }
            }
            post {
                failure {
                    notifySlackBuildFileItselfHasError("error", "var setting error", TYPE, "setting variables",)
                    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
                        script {
                            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"error\", \"text\": \"Jenkins file error\"}'"
                        }
                    }
                }
            }
        }


        stage('Branch Creation') {
            when {
                expression { TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR" }
            }
            stages {
                stage('checking and create branch') {

                    steps {
                        script {
                            def br = ""
                            if (TYPE == "CREATE_RELEASE_BR") {
                                br = sh(returnStdout: true, script: './test.sh release').trim()
                                if (br == '1') {
                                    branchCreationError()
                                    throw exception
                                } else {
                                    sh 'git checkout -b release origin/develop'
                                    sh 'git push origin release'
                                }
                            } else {
                                br = sh(returnStdout: true, script: './test.sh hotfix').trim()
                                if (br == '1') {
                                    branchCreationError()
                                    throw exception
                                } else {
                                    sh 'git checkout -b hotfix origin/master'
                                    sh 'git push origin hotfix'
                                }
                            }
                        }
                    }
                }
            }
        }

        stage("non branch") {
            when {
                expression { TYPE != "CREATE_RELEASE_BR" && TYPE != "CREATE_HOTFIX_BR" }
            }
            stages {

                stage('checkout code when on request releases[Slack]') {
                    when {
                        expression { TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ" }
                    }
                    steps {
                        step([$class: 'WsCleanup'])
                        checkout scm
                        script {
                            try {
                                if (TYPE == "QA_RELEASE_REQ") {
                                    sh 'git checkout origin/release'
                                } else if (TYPE == "STAGE_RELEASE_REQ") {
                                    sh 'git checkout origin/release'
                                } else if (TYPE == "DEV_RELEASE_REQ") {
                                    sh 'git checkout origin/develop'
                                } else if (TYPE == "PROD_RELEASE_REQ") {
                                    sh 'git checkout origin/master'
                                } else if (TYPE == "HOTFIX_QA_RELEASE_REQ") {
                                    sh 'git checkout origin/hotfix'
                                } else if (TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
                                    sh 'git checkout origin/hotfix'
                                }
                            } catch (exception) {
                                errorReport(TYPE)
                                throw exception
                            }
                        }
                    }
                }

//                stage('build ') {
//                    steps {
//                        script {
//                            try {
//                                sh "./gradlew clean build -x test -x check"
//                            } catch (exception) {
//                                errorReport(TYPE)
//                                throw exception
//                            }
//                        }
//                    }
//                }
//////
//                stage('Junit & Jacoco') {
//                    steps('running junit') {
//                        script {
//                            try {
//                                sh 'chmod +x gradlew'
//                                sh './gradlew test jacocoTestReport --no-daemon'
//                                // if in case tests fail then subsequent stages
//                                // will not run .but post block in this stage will run
//                            }
//                            catch (exception) {
//                                echo "$exception"
//                                summary = junit testResults: '**/build/test-results/test/*.xml'
//                                testsummary = summary.getProperties().toString().replaceAll("class:class hudson.tasks.junit.TestResultSummary,", "")
//                                testRpeortLink = env.RUN_TESTS_DISPLAY_URL
//                                coverageRpeortLink = BUILD_URL + "jacoco"
//                                errorReport(TYPE)
//                                throw exception
//                            }
//                            finally {
//                                summary = junit testResults: '**/build/test-results/test/*.xml'
//                                testsummary = summary.getProperties().toString().replaceAll("class:class hudson.tasks.junit.TestResultSummary,", "")
//                                testRpeortLink = env.RUN_TESTS_DISPLAY_URL
//                                coverageRpeortLink = BUILD_URL + "jacoco"
//                                step([$class          : 'JacocoPublisher',
//                                      execPattern     : '**/build/jacoco/*.exec',
//                                      classPattern    : '**/build/classes',
//                                      sourcePattern   : 'src/main/java',
//                                      exclusionPattern: 'src/test*'
//                                ])
//                                publishHTML target: [
//                                        allowMissing         : false,
//                                        alwaysLinkToLastBuild: false,
//                                        keepAll              : true,
//                                        reportDir            : "build/reports/tests/test",
//                                        reportFiles          : 'index.html',
//                                        reportName           : 'Junit Report'
//                                ]
//                            }
//                        }
//                    }
//                }
//
//                stage('Checkstyle') {
//                    steps {
//                        script {
//                            try {
//                                sh "./gradlew checkstyleMain checkstyleTest"
//                            } catch (exception) {
//                                checkstyleLink = BUILD_URL + "Checkstyle_20Report"
//                                errorReport(TYPE)
//                                throw exception
//                            } finally {
//                                checkstyleLink = BUILD_URL + "Checkstyle_20Report"
//                                publishHTML target: [
//                                        allowMissing         : false,
//                                        alwaysLinkToLastBuild: false,
//                                        keepAll              : true,
//                                        reportDir            : "build/reports/checkstyle",
//                                        reportFiles          : '**/*',
//                                        reportName           : 'Checkstyle Report'
//                                ]
//                            }
//                        }
//                    }
//                }
////
//                stage('PMD') {
//                    steps {
//                        script {
//                            try {
//                                sh "./gradlew pmdmain pmdtest"
//                            } catch (exception) {
//                                pmdLink = BUILD_URL + "PMD_20Report"
////                                errorReport(TYPE)
////                                        throw exception
//                            } finally {
//                                pmdLink = BUILD_URL + "PMD_20Report"
//                                publishHTML target: [
//                                        allowMissing         : false,
//                                        alwaysLinkToLastBuild: false,
//                                        keepAll              : true,
//                                        reportDir            : "build/reports/pmd",
//                                        reportFiles          : 'main.html,test.html',
//                                        reportName           : 'PMD Report'
//                                ]
//                            }
//                        }
//                    }
//                }
//
//                stage('SQ analysis') { //there are 2 ways to configure sonar in jenkins
//                    //one method usingg jenkins global configuration
//                    steps {
//                        script {
////                    def scannerHome = tool 'SonarScanner 4.0';
////                    withSonarQubeEnv('mysona') { // If you have configured more than one global server connection, you can specify its name
////                        sh "${scannerHome}/bin/sonar-scanner"
////                    }
//                            //other one is using gradle build
//                            withSonarQubeEnv() {
//                                // Will pick the global server connection you have configured
//                                sh "./gradlew sonarqube -Dsonar.projectName=${env.JOB_BASE_NAME}_${env.BUILD_NUMBER}"
//                            }
//                            timeout(time: 1, unit: 'HOURS') {
//                                // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
//                                // true = set pipeline to UNSTABLE, false = don't
//                                waitForQualityGate abortPipeline: true
//                            }
//                        }
//                    }
//                    post {
//                        always {
//
//                            script {
//                                sonarLink = "http://localhost:9000/dashboard?id=${env.JOB_BASE_NAME}_${env.BUILD_NUMBER}"
//
//                            }
//                        }
//                        unstable {
//                            errorReport(TYPE)
//                        }
//                    }
//                }


                stage('Release Request[Manual]') {
                    when {
                        expression { TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ" }
                    }
                    steps {

                        script {
                            try {
                                timeout(time: 1, unit: "HOURS") {
                                    getApproval(TYPE)
//                                            successReport(TYPE)
                                    approvedBy = input id: 'reqApproval', message: "$SLACK_USER requested  $slackUserRequestedReleaseType ",
                                            ok: 'Approve?',
//                                                submitter: 'user1,user2,group1',
                                            submitterParameter: 'APPROVER'

                                    notifyApproval(TYPE)
                                }
                            } catch (exception) {
                                def user123 = exception.getCauses()[0].getUser().toString()
                                notifyReject(TYPE, "${user123}")
                                throw exception
                            }
                        }
                    }
                }

                stage('Release Request[Auto]') {
                    when {
                        expression { TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE" }
                    }
                    steps {
                        script {
                            try {
                                timeout(time: 1, unit: "HOURS") {
                                    getApproval(TYPE)
                                    approvedBy = input id: 'reqApprovalAuto', message: "Latest code changes have been merged to $autoTriggeredGitBranch branch.You can check " +
                                            "the coverage and sonarqube report .",
                                            ok: 'Approve the release?',
//                                                submitter: 'user1,user2,group1',
                                            submitterParameter: 'APPROVER'

                                    notifyApproval(TYPE)
                                }
                            } catch (exception) {
                                def user123 = exception.getCauses()[0].getUser().toString()
                                notifyReject(TYPE, "${user123}")
                                throw exception
                            }
                        }
                    }
                }

                stage('deployment') {
                    when {
                        expression {
                            TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE" ||
                                    TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" ||
                                    TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ"
                        }
                    }
                    steps {
                        echo 'deployment for PR merge commits and release requests>>>'
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
    if (TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR") {
        branchCreationSuccessful()
    } else if (TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        notifySlack(manualReleaseSuccessMSGBuilder('general'))
    } else if (TYPE == "DEV_PR" || TYPE == "RELEASE_PR" || TYPE == "HOTFIX_PR" || TYPE == "PROD_PR" || TYPE == "HOTFIX_PROD_PR") {
        notifySlack(pullReqSuccessMSGBuilder("pull-request"))
    } else if (TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE") {
        notifySlack(autoReleaseSuccessMSGBuilder('general'))
    }
}

def errorReport(TYPE) {
    if (TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR") {
        branchCreationError()
    } else if (TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        notifySlack(manualReleaseFailedMSGBuilder("admin"))
    } else if (TYPE == "DEV_PR" || TYPE == "RELEASE_PR" || TYPE == "HOTFIX_PR" || TYPE == "PROD_PR" || TYPE == "HOTFIX_PROD_PR") {
        notifySlack(pullReqFailedMSGBuilder("pull-request"))
    } else if (TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE") {
        notifySlack(autoReleaseFailedMSGBuilder('pull-request'))
    }
}

def notifySlack(body) {
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '$body'"
        }
    }
}

def getApproval(TYPE) {
    def channel = "release-approval"
    def branch = ""
    def envTemp = ""
    def manualReq = false
    def body=""
    if (TYPE == "DEV_RELEASE_REQ") {
        branch = "Develop"
        envTemp = "Develop"
        manualReq = true
    } else if (TYPE == "QA_RELEASE_REQ") {
        branch = "Release"
        envTemp = "QA"
        manualReq = true
    } else if (TYPE == "STAGE_RELEASE_REQ") {
        branch = "release"
        envTemp = "staging"
        manualReq = true
    } else if (TYPE == "PROD_RELEASE_REQ") {
        branch = "master"
        envTemp = "Production"
        manualReq = true
    } else if (TYPE == "HOTFIX_QA_RELEASE_REQ") {
        branch = "Hotfix"
        envTemp = "QA"
        manualReq = true
    } else if (TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        branch = "Hotfix"
        envTemp = "Staging"
        manualReq = true
    } else if (TYPE == "DEV_RELEASE") {
        branch = "Develop"
        envTemp = "Develop"
    } else if (TYPE == "QA_RELEASE") {
        branch = "Release"
        envTemp = "QA"
    } else if (TYPE == "PROD_RELEASE") {
        branch = "Master"
        envTemp = "Production"
    } else if (TYPE == "HOTFIX_QA_RELEASE") {
        branch = "Hotfix"
        envTemp = "QA"
    }

    if (manualReq) {
          body = '''
 { "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":white_check_mark: *Approval is pending * <''' + env.RUN_DISPLAY_URL + '''|[*jenkins pipeline*]>\n 
:fire:*''' + branch + '''* branch will be released to *''' + envTemp + '''* environment*:fire:\n
Initiated by *''' + SLACK_USER + '''* \n 
Git commit [*''' + COMMIT_HASH + '''*] \n
Author [*''' + COMMIT_AUTHOR + '''*]\n
Git message [*''' + COMMIT_MSG + '''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": "
\tReports \n  
\t :heavy_check_mark:Test Summary ''' + testsummary + '''<''' + testRpeortLink + '''|[*Junit Report*]>\n 
\t :heavy_check_mark:Jacoco Coverage <''' + coverageRpeortLink + '''|[*Jacoco Report*]>\n 
\t :heavy_check_mark:PMD<''' + pmdLink + '''|[*PMD Report*]>\n 
\t :heavy_check_mark:Checkstyle <''' + checkstyleLink + '''|[*Checkstyle Report*]>\n
\t :heavy_check_mark:Sonarqube <''' + sonarLink + '''|[*Sonarqube Report*]>"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
 '''

    } else {
         body = '''
 { "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":white_check_mark: *Approval is pending * <''' + env.RUN_DISPLAY_URL + '''|[*jenkins pipeline*]>\n 
:fire:*''' + branch + '''* branch will be released to *''' + envTemp + '''* environment*:fire:\n
Initiated by *''' + System + '''*\n 
Git commit [*''' + COMMIT_HASH + '''*] \n
Author [*''' + COMMIT_AUTHOR + '''*]\n
Git message [*''' + COMMIT_MSG + '''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": "
\tReports \n  
\t :heavy_check_mark:Test Summary ''' + testsummary + '''<''' + testRpeortLink + '''|[*Junit Report*]>\n 
\t :heavy_check_mark:Jacoco Coverage <''' + coverageRpeortLink + '''|[*Jacoco Report*]>\n 
\t :heavy_check_mark:PMD<''' + pmdLink + '''|[*PMD Report*]>\n 
\t :heavy_check_mark:Checkstyle <''' + checkstyleLink + '''|[*Checkstyle Report*]>\n
\t :heavy_check_mark:Sonarqube <''' + sonarLink + '''|[*Sonarqube Report*]>"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
 '''

    }
    notifySlack(body)

}

def notifyApproval(type) {
    def channel = "general"
    def body='''
{
 { "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":white_check_mark: *Release Was Approved* <'''+ env.RUN_DISPLAY_URL+'''|[*jenkins pipeline*]>:x: \\nApproved by *'''+approvedBy+'''*\\nGit commit [*'''+COMMIT_HASH+'''*]\\nAuthor [*'''+COMMIT_AUTHOR+'''*]\\nGit message [*'''+COMMIT_MSG+'''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
'''
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '$body'"
        }
    }
}

def notifyReject(type, user) {
    def channel = "general"
    def body='''
{
 { "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":white_check_mark: *Release Was Rejected* <'''+ env.RUN_DISPLAY_URL+'''|[*jenkins pipeline*]>:x: \\n Rejected by *'''+user+'''*\\nGit commit [*'''+COMMIT_HASH+'''*]\\nAuthor [*'''+COMMIT_AUTHOR+'''*]\\nGit message [*'''+COMMIT_MSG+'''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
'''
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '$body'"
        }
    }
}

def branchCreationError() {
    def channel = "admin"
    def msg = "$user requested to create $slackUserCreatedBranch branch .But $slackUserCreatedBranch branch already exist"
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\" :  \"${msg}\"}'"
        }
    }
}

def branchCreationSuccessful() {
    def msg = ""
    def channel = "general"
    msg = "$SLACK_USER created  $slackUserCreatedBranch branch."
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\" :  \"${msg}\"}'"
        }
    }
}

def pullReqSuccessMSGBuilder(channel) {

    return '''
 { "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":white_check_mark: *PR Build Successful* <''' + env.RUN_DISPLAY_URL + '''|[*jenkins pipeline*]>\n 
\t:fire:<''' + GIT_PR_LINK + ''' |Pull Request> \n 
\t:fire:Git commit [*''' + COMMIT_HASH + '''*]\n 
\t:fire:Author [*''' + COMMIT_AUTHOR + '''*]\\n  
\\t:fire:Git message[*''' + COMMIT_MSG + '''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": "
\tReports \n  
\t :heavy_check_mark:Test Summary ''' + testsummary + '''<''' + testRpeortLink + '''|[*Junit Report*]>\n 
\t :heavy_check_mark:Jacoco Coverage <''' + coverageRpeortLink + '''|[*Jacoco Report*]>\n 
\t :heavy_check_mark:PMD<''' + pmdLink + '''|[*PMD Report*]>\n 
\t :heavy_check_mark:Checkstyle <''' + checkstyleLink + '''|[*Checkstyle Report*]>\n
\t :heavy_check_mark:Sonarqube <''' + sonarLink + '''|[*Sonarqube Report*]>"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
 '''
}


def pullReqFailedMSGBuilder(channel) {
    return '''
{ "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":x: *Pull Request Failed* <''' + env.RUN_DISPLAY_URL + '''|[*jenkins pipeline*]>:x:\n 
\t:fire:<''' +GIT_PR_LINK+ ''' |Pull Request> \n 
\t:fire:Git commit [*''' + COMMIT_HASH + '''*]\n 
\t:fire:Author [*''' + COMMIT_AUTHOR + '''*]\n  
\t:fire:Git message[*''' + COMMIT_MSG + '''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
 '''
}


def manualReleaseSuccessMSGBuilder(channel) {
    def branch = ""
    def envTemp = ""
    if (TYPE == "DEV_RELEASE_REQ") {
        branch = "Develop"
        envTemp = "Develop"
    } else if (TYPE == "QA_RELEASE_REQ") {
        branch = "Release"
        envTemp = "QA"
    } else if (TYPE == "STAGE_RELEASE_REQ") {
        branch = "release"
        envTemp = "staging"
    } else if (TYPE == "PROD_RELEASE_REQ") {
        branch = "master"
        envTemp = "Production"
    } else if (TYPE == "HOTFIX_QA_RELEASE_REQ") {
        branch = "Hotfix"
        envTemp = "QA"
    } else if (TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        branch = "Hotfix"
        envTemp = "Staging"
    }


    return '''
 { "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":white_check_mark: *Release Successful* <''' + env.RUN_DISPLAY_URL + '''|[*jenkins pipeline*]>\n 
:fire:*''' + branch + '''* branch released to *''' + envTemp + '''* environment*:fire:\n
Initiated by *''' + SLACK_USER + '''* , Approved by *''' + approvedBy + '''*\n 
Git commit [*''' + COMMIT_HASH + '''*] \n
Author [*''' + COMMIT_AUTHOR + '''*]\n
Git message [*''' + COMMIT_MSG + '''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": "
\tReports \n  
\t :heavy_check_mark:Test Summary ''' + testsummary + '''<''' + testRpeortLink + '''|[*Junit Report*]>\n 
\t :heavy_check_mark:Jacoco Coverage <''' + coverageRpeortLink + '''|[*Jacoco Report*]>\n 
\t :heavy_check_mark:PMD<''' + pmdLink + '''|[*PMD Report*]>\n 
\t :heavy_check_mark:Checkstyle <''' + checkstyleLink + '''|[*Checkstyle Report*]>\n
\t :heavy_check_mark:Sonarqube <''' + sonarLink + '''|[*Sonarqube Report*]>"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
 '''
}

def manualReleaseFailedMSGBuilder(channel) {
    def branch = ""
    def envTemp = ""
    if (TYPE == "DEV_RELEASE_REQ") {
        branch = "Develop"
        envTemp = "Develop"
    } else if (TYPE == "QA_RELEASE_REQ") {
        branch = "Release"
        envTemp = "QA"
    } else if (TYPE == "STAGE_RELEASE_REQ") {
        branch = "release"
        envTemp = "staging"
    } else if (TYPE == "PROD_RELEASE_REQ") {
        branch = "master"
        envTemp = "Production"
    } else if (TYPE == "HOTFIX_QA_RELEASE_REQ") {
        branch = "Hotfix"
        envTemp = "QA"
    } else if (TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        branch = "Hotfix"
        envTemp = "Staging"
    }

    return '''
{ "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":x: *Release Failed* <''' + env.RUN_DISPLAY_URL + '''|[*jenkins pipeline*]>:x:\n 
\t :fire:*''' + branch + '''* branch released to *''' + envTemp + '''* environment :fire:\n 
Initiated by *''' + SLACK_USER + '''* \n
Approved by [*''' + approvedBy + '''*]\n
Git commit [*''' + COMMIT_HASH + '''*] \n
Author [*''' + COMMIT_AUTHOR + '''*]\n  
Git message[*''' + COMMIT_MSG + '''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
 '''
}

def autoReleaseSuccessMSGBuilder(channel) {
    def branch = ""
    def envTemp = ""
    if (TYPE == "DEV_RELEASE") {
        branch = "Develop"
        envTemp = "Develop"
    } else if (TYPE == "QA_RELEASE") {
        branch = "Release"
        envTemp = "QA"
    } else if (TYPE == "PROD_RELEASE") {
        branch = "Master"
        envTemp = "Production"
    } else if (TYPE == "HOTFIX_QA_RELEASE") {
        branch = "Hotfix"
        envTemp = "QA"
    }

    return '''
 { "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":white_check_mark: *Release Successful* <''' + env.RUN_DISPLAY_URL + '''|[*jenkins pipeline*]>\n 
:fire:*''' + branch + '''* branch released to *''' + envTemp + '''* environment*:fire:\n
Initiated by *SYSTEM* , Approved by *''' + approvedBy + '''*\n 
Git commit [*''' + COMMIT_HASH + '''*] \n
Author [*''' + COMMIT_AUTHOR + '''*]\n
Git message [*''' + COMMIT_MSG + '''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": "
\tReports \n  
\t :heavy_check_mark:Test Summary ''' + testsummary + '''<''' + testRpeortLink + '''|[*Junit Report*]>\n 
\t :heavy_check_mark:Jacoco Coverage <''' + coverageRpeortLink + '''|[*Jacoco Report*]>\n 
\t :heavy_check_mark:PMD<''' + pmdLink + '''|[*PMD Report*]>\n 
\t :heavy_check_mark:Checkstyle <''' + checkstyleLink + '''|[*Checkstyle Report*]>\n
\t :heavy_check_mark:Sonarqube <''' + sonarLink + '''|[*Sonarqube Report*]>"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
 '''
}

def autoReleaseFailedMSGBuilder(channel) {
    def branch = ""
    def envTemp = ""
    if (TYPE == "DEV_RELEASE") {
        branch = "Develop"
        envTemp = "Develop"
    } else if (TYPE == "QA_RELEASE") {
        branch = "Release"
        envTemp = "QA"
    } else if (TYPE == "PROD_RELEASE") {
        branch = "Master"
        envTemp = "Production"
    } else if (TYPE == "HOTFIX_QA_RELEASE") {
        branch = "Hotfix"
        envTemp = "QA"
    }

    return '''
{ "channel":"''' + channel + '''",
\t"blocks": [
\t\t{
\t\t\t"type": "section",
\t\t\t"text": {
\t\t\t\t"type": "mrkdwn",
\t\t\t\t"text": ":x: *Release Failed* <''' + env.RUN_DISPLAY_URL + '''|[*jenkins pipeline*]>:x:
:fire:*''' + branch + '''* branch released to *''' + envTemp + '''* environment :fire:
Initiated by *SYSTEM* 
Approved by [*''' + approvedBy + '''*]
Git commit [*''' + COMMIT_HASH + '''*] 
Author [*''' + COMMIT_AUTHOR + '''*] 
Git message[*''' + COMMIT_MSG + '''*]"
\t\t\t}
\t\t},
\t\t{
\t\t\t"type": "divider"
\t\t}
\t]
}
 '''
}
