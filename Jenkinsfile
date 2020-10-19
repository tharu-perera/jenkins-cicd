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
gitPRLink = ""

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
                    gitPRLink = env.CHANGE_URL
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
                    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
                        script {
                            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"error\", \"text\": \"Jenkins file error\"}'"
                        }
                    }
                }
            }
        }

        stage(" ") {
            parallel {
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

                        stage('build ') {
                            steps {
                                script {
                                    try {
                                        sh "./gradlew clean build -x test -x check"
                                    } catch (exception) {
                                        errorReport(TYPE)
                                        throw exception
                                    }
                                }
                            }
                        }
//
                        stage('Junit & Jacoco') {
                            steps('running junit') {
                                script {
                                    try {
                                        sh 'chmod +x gradlew'
                                        sh './gradlew test jacocoTestReport --no-daemon'
                                        // if in case tests fail then subsequent stages
                                        // will not run .but post block in this stage will run
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
                                    }
                                    catch (exception) {
                                        echo "$exception"
                                        summary = junit testResults: '**/build/test-results/test/*.xml'
                                        testsummary = summary.getProperties()
                                        testRpeortLink = env.RUN_TESTS_DISPLAY_URL
                                        coverageRpeortLink = BUILD_URL + "jacoco"
                                        errorReport(TYPE)
                                        throw exception
                                    }
                                    finally {
                                        summary = junit testResults: '**/build/test-results/test/*.xml'
                                        testsummary = summary.getProperties()
                                        testRpeortLink = env.RUN_TESTS_DISPLAY_URL
                                        coverageRpeortLink = BUILD_URL + "jacoco"
                                        step([$class          : 'JacocoPublisher',
                                              execPattern     : '**/build/jacoco/*.exec',
                                              classPattern    : '**/build/classes',
                                              sourcePattern   : 'src/main/java',
                                              exclusionPattern: 'src/test*'
                                        ])
                                        publishHTML target: [
                                                allowMissing         : false,
                                                alwaysLinkToLastBuild: false,
                                                keepAll              : true,
                                                reportDir            : "build/reports/tests/test",
                                                reportFiles          : 'index.html',
                                                reportName           : 'Junit Report'
                                        ]
                                    }
                                }
                            }
                        }

                        stage('Checkstyle') {
                            steps {
                                script {
                                    try {
                                        sh "./gradlew checkstyleMain checkstyleTest"
                                    } catch (exception) {
                                        checkstyleLink = BUILD_URL + "Checkstyle_20Report"
                                        errorReport(TYPE)
                                        throw exception
                                    } finally {
                                        checkstyleLink = BUILD_URL + "Checkstyle_20Report"
                                        publishHTML target: [
                                                allowMissing         : false,
                                                alwaysLinkToLastBuild: false,
                                                keepAll              : true,
                                                reportDir            : "build/reports/checkstyle",
                                                reportFiles          : '**/*',
                                                reportName           : 'Checkstyle Report'
                                        ]
                                    }
                                }
                            }
                        }
//
                        stage('PMD') {
                            steps {
                                script {
                                    try {
                                        sh "./gradlew pmdmain pmdtest"
                                    } catch (exception) {
                                        pmdLink = BUILD_URL + "PMD_20Report"
                                        errorReport(TYPE)
//                                        throw exception
                                    } finally {
                                        pmdLink = BUILD_URL + "PMD_20Report"
                                        publishHTML target: [
                                                allowMissing         : false,
                                                alwaysLinkToLastBuild: false,
                                                keepAll              : true,
                                                reportDir            : "build/reports/pmd",
                                                reportFiles          : 'main.html,test.html',
                                                reportName           : 'PMD Report'
                                        ]
                                    }
                                }
                            }
                        }
//
//
                        stage('SQ analysis') { //there are 2 ways to configure sonar in jenkins
                            //one method usingg jenkins global configuration
                            steps {
                                script {
//                    def scannerHome = tool 'SonarScanner 4.0';
//                    withSonarQubeEnv('mysona') { // If you have configured more than one global server connection, you can specify its name
//                        sh "${scannerHome}/bin/sonar-scanner"
//                    }
                                    //other one is using gradle build
                                    withSonarQubeEnv() {
                                        // Will pick the global server connection you have configured
                                        sh "./gradlew sonarqube -Dsonar.projectName=${TYPE}"
                                    }
                                    timeout(time: 1, unit: 'HOURS') {
                                        // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                                        // true = set pipeline to UNSTABLE, false = don't
                                        waitForQualityGate abortPipeline: true
                                    }
                                }
                            }
                            post {
                                always {
                                    steps {
                                        step {
                                            script {
                                                sonarLink = "http://localhost:9000/dashboard?id=${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}"
                                            }
                                        }
                                    }
                                }
                                unstable {
                                    errorReport(TYPE)
                                }
                            }
                        }


                        stage('Release Request[Manual]') {
                            when {
                                expression { TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ" }
                            }
                            steps {

                                script {
                                    try {
                                        timeout(time: 1, unit: "HOURS") {
                                            getApproval(TYPE)
                                            successReport(TYPE)
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
                                echo 'deployment for PR merge commits and release requests'
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
    if (TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR") {
        branchCreationSuccessful()
    } else if (TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        notifySlack(successLayoutSlack('general'))
    } else if (TYPE == "DEV_PR" || TYPE == "RELEASE_PR" || TYPE == "HOTFIX_PR" || TYPE == "PROD_PR" || TYPE == "HOTFIX_PROD_PR") {
        notifySlack(successLayoutSlack('pull-request'))
    } else if (TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE") {
        notifySlack(successLayoutSlack('general'))
    }
}

def errorReport(TYPE) {
    if (TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR") {
        branchCreationError()
    } else if (TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        notifySlack(errorLayoutSlack("admin"))
    } else if (TYPE == "DEV_PR" || TYPE == "RELEASE_PR" || TYPE == "HOTFIX_PR" || TYPE == "PROD_PR" || TYPE == "HOTFIX_PROD_PR") {
        notifySlack(errorLayoutSlack("pull-request"))
    } else if (TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE") {
        notifySlack(errorLayoutSlack("general"))
    }
}

def notifySlack(body) {
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '$body'"
        }
    }
}

def getApproval(type) {
    def channel = "release-approval"
    def msg = "need approval for release"
    withCredentials([string(credentialsId: 'slack-token', variable: 'st')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\"  : \"msg=${msg}   type= ${type} \"}'"
        }
    }
}

def notifyApproval(type) {
    def channel = "general"
    def msg = "approved for release ${approvedBy}"
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\" :  \"msg=${msg}   type= ${type} \"}'"
        }
    }
}

def notifyReject(type, user) {
    def channel = "general"
    def msg = "rejected release $user"
    withCredentials([string(credentialsId: 'slack-token', variable: 'st'), string(credentialsId: 'jen', variable: 'jenn')]) {
        script {
            sh "curl --location --request POST '$st'  --header 'Content-Type: application/json' --data-raw '{ \"channel\": \"${channel}\", \"text\" :  \"msg=${msg}   type= ${type} \"}'"
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

def errorLayoutSlack(channel) {
    return ' {"channel":"' + channel + '","blocks": [ ' + getBuildStatusError() + getHeader() + getDivider() + ' ] }'
}

def successLayoutSlack(channel) {
    return ' {"channel":"' + channel + '","blocks": [ ' + getBuildStatusSuccess() + getHeader() + getDetail("Test Summary " + testsummary) + getDivider() + getDetailWithLink("SonarQube", sonarLink) + getDetailWithLink("PMD", pmdLink) + getDetailWithLink("Checkstyle", checkstyleLink) + getDetailWithLink("Coverage", coverageRpeortLink) + ' ] }'
}

def getBuildStatusError() {
    return '{ "type": "section" ,"text": {"type": "mrkdwn", "text": ":no_entry_sign: *Build Failed*  <' + env.RUN_DISPLAY_URL + '|Pipeline>" }}'
}

def getBuildStatusSuccess() {
    return '{ "type": "section" ,"text": {"type": "mrkdwn", "text": ":white_check_mark: *Build Successful*  <' + env.RUN_DISPLAY_URL + '|Pipeline>" }}'
}

def getHeader() {
    if (TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ") {
        return ',{"type": "section","text": {"type": "mrkdwn","text": "Action: *' + slackUserRequestedReleaseType + '*"}},' +
                '{"type": "section","text": {"type": "mrkdwn","text": "Requested by *' + SLACK_USER + '*"}}'
    } else if (TYPE == "DEV_PR" || TYPE == "RELEASE_PR" || TYPE == "HOTFIX_PR" || TYPE == "PROD_PR" || TYPE == "HOTFIX_PROD_PR") {
        return ',{"type": "section","text": {"type": "mrkdwn","text": "*PR Request* by *' + COMMIT_AUTHOR + '*"}}' +
                ',{"type": "section","text": {"type": "mrkdwn","text": "*Commit Message*  *' + COMMIT_MSG + '*"}}' +
                ',{"type": "section","text": {"type": "mrkdwn","text": "*GitHub* <' + gitPRLink + '|link>"}}'
    } else if (TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE") {
        return ',{"type": "section","text": {"type": "mrkdwn","text": "Commits merged to  *' + autoTriggeredGitBranch + ' branch*"}}'
    }
}

def getDetailWithLink(msg, link) {
    return ',{"type": "section","text": {"type": "mrkdwn","text": "' + msg + ' <' + link + '|Report>"}}'
}

def getDetail(msg) {
    return ',{"type": "section","text": {"type": "mrkdwn","text": "' + msg + '"}}'
}

def getDivider() {
    return ' ,{  "type": "divider" }'
}