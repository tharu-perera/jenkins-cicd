#!groovy
import groovy.json.JsonOutput
def author = ""
def DEPLOY_QA = 'qa'
def deployto = {
    'qa'
}
def getGitAuthor = {
    def commit = sh(returnStdout: true, script: 'git rev-parse HEAD')
    author = sh(returnStdout: true, script: "git --no-pager show -s --format='%an' ${commit}").trim()
}

def getBuildUser() {
    return currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
}

//to do chnageset  ,  changelog
pipeline {
    agent any
    environment {
        doError = '0'
        BUILD_USER = ''
        CC = 'globvar'
        DEPLOY_TO = deployto()
    }
    options {
//        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '1'))
    }
    parameters {
        string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')

        text(name: 'BIOGRAPHY', defaultValue: '', description: 'Enter some information about the person')

        booleanParam(name: 'TOGGLE', defaultValue: true, description: 'Toggle this value')

        choice(name: 'CHOICE', choices: ['One', 'Two', 'Three'], description: 'Pick something')

        password(name: 'PASSWORD', defaultValue: 'SECRET', description: 'Enter a password')

        file(name: 'name', description: 'file to upload')

    }

    // =============== stages====================
    stages {

        stage("send slack ") {

            steps {
//                script{
//                    def slackURL = 'https://hooks.slack.com/services/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'
//                    def jenkinsIcon = 'https://wiki.jenkins-ci.org/download/attachments/2916393/logo.png'
//
//                    def payload = JsonOutput.toJson([text: "xxxxxxx",
//                                                     channel: "general",
//                                                     username: "Jenkins",
//                                                     icon_url: jenkinsIcon
//                    ])
//
//                    sh "curl -X POST --data-urlencode \'payload=${payload}\' ${slackURL}"
//
//                }
                script {
//                    BUILD_USER = getBuildUser()
                    BUILD_USER = "wde"
                }
                echo 'I will always say hello in the console.'
                slackSend channel: 'general',
                        color: 'good',
                        message: "" +
                                "result >>*${currentBuild.result} >>" +
                                "changeSets >>*${currentBuild.changeSets} >>" +
//                                "rawBuild >>*${currentBuild.rawBuild} >>" +
                                "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"

            }
        }

        stage("env varible checking") {
            environment {
                // Using returnStdout
                CC = sh(
                        returnStdout: true,
                        script: 'echo "clang"'
                )
                // Using returnStatus
                EXIT_STATUS = """${sh(
                        returnStatus: true,
                        script: 'exit 1'
                )}"""
            }
            steps {
                sh 'printenv'
            }
        }

        stage('git n env info') {
            // getGitAuthor() -- we cnnot call method outside steps
            environment {
                DEBUG_FLAGS = '-g'
            }
            steps {
                script {
                    getGitAuthor() // getGitAuthor() -- we should call method outside steps
                }
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL} cc ${env.CC} DEBUG_FLAGS ${env.DEBUG_FLAGS}  "
                echo " git branch  ${env.GIT_BRANCH}  "
                echo " git author  ${author}  "
                echo " JOB_NAME  ${env.JOB_NAME}  "
            }
            post {
                always {
                    echo ' post inside stage (first stage) section .... always'
                }
            }
        }

        stage('currentBuild  ') {
            steps {
                echo " >>>> currentBuild ${currentBuild.changeSets} "
                echo " >>>> currentBuild ${currentBuild.result} "
                echo " >>>> gettign change logs "
                script {
                    def changeLogSets = currentBuild.changeSets
                    for (int i = 0; i < changeLogSets.size(); i++) {
                        def entries = changeLogSets[i].items
                        for (int j = 0; j < entries.length; j++) {
                            def entry = entries[j]
                            echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
                            def files = new ArrayList(entry.affectedFiles)
                            for (int k = 0; k < files.size(); k++) {
                                def file = files[k]
                                echo "  ${file.editType.name} ${file.path}"
                            }
                        }
                    }
                }

            }
        }


        stage('checking env of other stage') {
            steps {
                echo " first stage specific env var DEBUG_FLAGS = ${env.DEBUG_FLAGS}  "
                echo " globale env var [env.CC] = ${env.CC} "
                echo " globale env var [CC] = $CC"

            }
        }

        stage('checking shell status with script try catch') {
            steps {
                script {
                    try {
                        sh 'exit 1'
                    } catch (Exception e) {

                        sh 'echo   exception! '
                    }
                }
            }
        }

        stage('checking shell status 0') {
            steps {
                sh 'exit 0'
            }
        }
        // this will stop whole build and global level post blocks will get executed.
        //stages after this will not get executed
//        stage('checking shell status 1') {
//            steps {
//                sh 'exit 1'
//            }
//        }

        stage('when   -is master') {
            when { branch 'master' }
            steps {
                echo " when condition branch master"
            }
        }
        stage('when   -is develop') {
            when { branch 'develop' }
            steps {
                echo " when condition branch develop"
            }
        }
        stage('when    - patter release') {
            when { branch pattern: "release-\\d+", comparator: "REGEXP" }
            steps {
                echo "when condition checking - patter release"
            }
        }
        stage('   changeset    ') {
            when {
//                changeset pattern: "*/*TEST.java", comparator: "REGEXP"
//                anyOf {
//                    changeset "src/**"
//                    changeset "test/**"
//                }
                changeset "**/*.java"

            }
            steps {
                echo "when condition checking - changeset test classes"
            }
        }
        stage('   changelog    ') {
            when {
                changelog 'TEST'
            }
            steps {
                echo "when condition checking - changelog"
            }
        }

        stage('when  changeRequest - for all requests') {
            when { changeRequest() }
            steps {
                echo "when   - changeRequest¬ for all requests"
            }
        }
        stage('when  - changeRequest target master') {
            when {
                changeRequest target: 'master'
                expression {
                    currentBuild.result == null || currentBuild.result == 'SUCCESS'
                }
            }
            steps {
                echo "when condition checking - changeRequest¬ target master"
            }
        }
        stage('when  - changeRequest target develop') {
            when { changeRequest target: 'develop' }
            steps {
                echo "when condition checking - changeRequest¬ target develop"
            }
        }
        stage('when  - SCMTrigger') {
            when { triggeredBy 'SCMTrigger' }
            steps {
                echo "when condition checking - SCMTrigger"
            }
        }
        stage('when  - UpstreamCause') {
            when { triggeredBy 'UpstreamCause' }
            steps {
                echo "when condition checking - UpstreamCause"
            }
        }
        stage('when   - TimerTrigger') {
            when { triggeredBy 'TimerTrigger' }
            steps {
                echo "when condition checking - TimerTrigger"
            }
        }
        stage('when   - UserIdCause') {
            when { triggeredBy cause: "UserIdCause", detail: "vlinde" }
            steps {
                echo "when condition checking - UserIdCause"
            }
        }
        stage(' checking production deloy wit env ') {
            when {
                environment name: 'DEPLOY_TO', value: 'production'
            }
            steps {
                echo 'Deploying to prod'
            }
        }
        stage(' checking qa deloy wit env ') {
            when {
                environment name: 'DEPLOY_TO', value: 'qa'
            }
            steps {
                echo 'Deploying to qa '
            }
        }


        stage('when condition checking succes scenario ') {
            when {
                expression {
                    currentBuild.result == 'SUCCESS'
                }
            }
            steps {
                echo " when conditio is SUCCESS"
            }
        }

        stage('when condition checking ERROR scenario ') {
            when {
                expression {
                    currentBuild.result == 'FAILED'
                }
            }
            steps {
                echo " when conditio is FAILED"
            }
        }

        stage('printing environment variables') {
            environment {
                AN_ACCESS_KEY = credentials('jen')
            }
            steps {
                echo 'printing jen pass from jenkins config == ${AN_ACCESS_KEY}'
                sh 'printenv'
                sh 'echo  ${AN_ACCESS_KEY}'
                sh 'echo  $AN_ACCESS_KEY'
            }
        }

        stage('printing all global parameters') {
            steps {
                echo "Hello ${params.PERSON}"

                echo "Biography: ${params.BIOGRAPHY}"

                echo "Toggle: ${params.TOGGLE}"

                echo "Choice: ${params.CHOICE}"

                echo "Password: ${params.PASSWORD}"
            }
        }

//        stage('Input paramter checking') {
//            input {
//                message "Should we continue?"
//                ok "Yes, we should."
//                submitter "alice,bob"
//                parameters {
//                    choice(name: 'CHOICE', choices: ['One', 'Two', 'Three'], description: 'Pick something')
//                    string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
//                }
//            }
//            steps {
//                echo "Hello, ${PERSON}, nice to meet you."
//            }
//        }

        stage('final  stage to test last step') {
            steps {
                echo 'final test'
            }
        }
    }

    post { // these post steps will get executed at the end of build
        always {
            echo ' post outside stages always '
//            sh '${currentBuild.result}'
        }
        failure {
            echo ' post outside stages failure'
        }
        unstable {
            echo ' post outside stages unstable'
        }
        aborted {
            echo ' post outside stages aborted'
        }
    }
}