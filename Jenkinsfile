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

//TODO chnageset  ,  changelog, try catch bloc , send test summary, sonar summary ,
pipeline {
//    try{
    agent any
    options {
        skipDefaultCheckout()
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
                    def commit = sh(returnStdout: true, script: 'git rev-parse HEAD')
                    COMMIT_AUTHOR = sh(returnStdout: true, script: "git --no-pager show -s --format='%an' ${commit}").trim()
                    COMMIT_MSG = sh(returnStdout: true, script: "git log --format=%B -n 1  ${commit}").trim()
                    echo "BUILD_USER : ${BUILD_USER}\n"
                    echo "COMMIT_MSG: ${COMMIT_MSG}\n"
                    echo "COMMIT_AUTHOR: ${COMMIT_AUTHOR}\n"
                    echo "commit: ${commit}\n"
                }
            }

            post {
                failure {
                    slackSend channel: 'error', color: COLOR_MAP[currentBuild.currentResult], message: "setting variables error"
                }
            }
        }


        stage(" ") {
            parallel {
                stage('Branch Creation[Slack]') {
                    when {
                        expression { TYPE == "CREATE_RELEASE_BR" || TYPE == "CREATE_HOTFIX_BR" }
                    }
                    stages {
                        stage('checking build type111') {
                            steps {
                                echo 'Branch Creation'
                            }
                        }
                        stage('checking build type2222') {
                            steps {
                                echo 'Branch Creation'
                            }
                        }
                    }
                }
                stage('non Branch Creation') {
                    when {
                        expression { TYPE != "CREATE_RELEASE_BR" && TYPE != "CREATE_HOTFIX_BR" }
                    }
                    stages {
                        parallel {
                            stage('checkout code when on request release') {
                                when {
                                    expression { TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ" }
                                }
                                steps {
                                    echo 'clean ws and checkkout code '
                                }
                            }

                            stage('build code for all ') {

                                steps {
                                    echo ' build code '
                                }
                            }
                        }
                    }
                }

//                stage('Release Requests[Slack]') {
//                    when {
//                        expression { TYPE == "QA_RELEASE_REQ" || TYPE == "STAGE_RELEASE_REQ" || TYPE == "DEV_RELEASE_REQ" || TYPE == "PROD_RELEASE_REQ" || TYPE == "HOTFIX_QA_RELEASE_REQ" || TYPE == "HOTFIX_STAGING_RELEASE_REQ" }
//                    }
//                    stages {
//                        stage('checking build type111') {
//                            steps {
//                                echo 'DEV_RELEASE'
//                            }
//                        }
//                        stage('checking build type2222') {
//                            steps {
//                                echo 'DEV_RELEASE'
//                            }
//                        }
//                    }
//                }
//
//                stage('Auto Release Requests[PR Merged]') {
//                    when {
//                        expression { TYPE == "DEV_RELEASE" || TYPE == "QA_RELEASE" || TYPE == "PROD_RELEASE" || TYPE == "HOTFIX_QA_RELEASE" }
//                    }
//                    stages {
//                        stage('checking build type111') {
//                            steps {
//                                echo 'DEV_RELEASE'
//                            }
//                        }
//                        stage('checking build type2222') {
//                            steps {
//                                echo 'DEV_RELEASE'
//                            }
//                        }
//                    }
//                }
            }

        }

//
//        stage('build ') {
//            steps {
//                sh "./gradlew clean build -x test -x check"
//            }
//
//            post {
//                failure {
//                    slackSend channel: 'error',
//                            color: COLOR_MAP[currentBuild.currentResult],
//                            message: " ${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"
//
//                }
//            }
//        }
//
//        stage('test') {
//            steps('running junit') {
//                script {
//                    try {
//                        sh 'chmod +x gradlew'
//                        sh './gradlew test jacocoTestReport --no-daemon'
//                        // if in case tests fail then subsequent stages
//                        // will not run .but post block in this stage will run
//                    }
//                    catch (exception) {
//                        echo "$exception"
//                    }
//                    finally {
////                        junit '**/build/test-results/test/*.xml'
//                        summary = junit testResults: '**/build/test-results/test/*.xml'
////                        echo "test >>> ${summary.getProperties()}"
//                    }
//                }
//                echo "test >>> ${summary.getProperties()}"
//            }
//            post {
//                success {
//                    echo "printing this message even unit test ara failing"
//                    step([$class          : 'JacocoPublisher',
//                          execPattern     : '**/build/jacoco/*.exec',
//                          classPattern    : '**/build/classes',
//                          sourcePattern   : 'src/main/java',
//                          exclusionPattern: 'src/test*'
//                    ])
//                    publishHTML target: [
//                            allowMissing         : false,
//                            alwaysLinkToLastBuild: false,
//                            keepAll              : true,
//                            reportDir            : "build/reports/tests/test",
//                            reportFiles          : 'index.html',
//                            reportName           : 'Junit Report'
//                    ]
//                }
//                failure {
//                    echo 'test error'
//                    slackSend channel: 'error', color: COLOR_MAP[currentBuild.currentResult], message: "junit error"
//
//                }
//            }
//        }
//
//
//        stage('Run Tests') {
//            parallel {
//                stage('Test On Windows') {
//                    steps {
//                        echo " aaaa"
//                    }
//                }
//                stage('Test On Linux') {
//                    steps {
//                        echo " bbbb"
//                    }
//                }
//            }
//        }
//
//        stage('Checkstyle') {
//            steps {
//                script {
//                    try {
//                        sh "./gradlew checkstyleMain checkstyleTest"
//                    } catch (exception) {
//                        echo "$exception"
//                    } finally {
//                        publishHTML target: [
//                                allowMissing         : false,
//                                alwaysLinkToLastBuild: false,
//                                keepAll              : true,
//                                reportDir            : "build/reports/checkstyle",
//                                reportFiles          : '**/*',
//                                reportName           : 'Checkstyle Report'
//                        ]
////                        recordIssues(
////                                enabledForFailure: true, aggregatingResults: true,
////                                tools: [java(), checkStyle(pattern: 'build/reports/checkstyle/main.html', reportEncoding: 'UTF-8')]
////                        )
//
//                    }
//                }
//            }
//        }
//
//        stage('PMD') {
//            steps {
//                script {
//                    try {
//                        sh "./gradlew pmdmain pmdtest"
//                    } catch (exception) {
//                        echo "$exception"
//                    } finally {
//                        publishHTML target: [
//                                allowMissing         : false,
//                                alwaysLinkToLastBuild: false,
//                                keepAll              : true,
//                                reportDir            : "build/reports/pmd",
//                                reportFiles          : 'main.html,test.html',
//                                reportName           : 'PMD Report'
//                        ]
////                        recordIssues(
////                                enabledForFailure: true, aggregatingResults: true,
////                                tools: [java(), checkStyle(pattern: 'build/reports/checkstyle/main.html', reportEncoding: 'UTF-8')]
////                        )
//
//                    }
//                }
//            }
//        }
//
//
//        stage('SQ analysis') { //there are 2 ways to configure sonar in jenkins
//            //one method usingg jenkins global configuration
//            steps {
//                script {
////                    def scannerHome = tool 'SonarScanner 4.0';
////                    withSonarQubeEnv('mysona') { // If you have configured more than one global server connection, you can specify its name
////                        sh "${scannerHome}/bin/sonar-scanner"
////                    }
//
//                    //other one is using gradle build
//                    withSonarQubeEnv() { // Will pick the global server connection you have configured
//                        sh "./gradlew sonarqube -Dsonar.projectName=${TYPE}"
//                    }
//                    timeout(time: 1, unit: 'HOURS') {
//                        // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
//                        // true = set pipeline to UNSTABLE, false = don't
//                        waitForQualityGate abortPipeline: true
//                    }
//                }
//            }
//
//            post {
//                failure {
//                    echo 'Sonarqube error'
//                    slackSend channel: 'error',
//                            color: COLOR_MAP[currentBuild.currentResult],
//                            message: "Sonarqube error"
//
//                }
//            }
//        }
////
////        stage('getting approval for qa release') {
////            when { branch 'develop' }
////            steps {
////                echo 'getting approval for qa release'
////                slackSend channel: 'qa-release-approval',
////                        color: 'good',
////                        message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"
////
////            }
////        }
////
//        stage('inform  build status to slack ') {
//            steps {
//                echo 'inform  build status to slack'
//                slackSend channel: 'general', color: COLOR_MAP[currentBuild.currentResult], message: "build completed"
//
//            }
//        }
    }
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
