#!groovy
def author = ""
def DEPLOY_QA = 'qa'


//to do chnageset  ,  changelog
pipeline {
    agent any
    environment {
        BUILD_USER = ''
    }
    options {
//        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '1'))
    }
    // =============== stages====================
    stages {
        stage('preparation') {
            steps {
                script {
                    // get build cause (time triggered vs. SCM change)
                    def buildCause = currentBuild.getBuildCauses()[0].shortDescription
                    echo "Current build was caused by: ${buildCause}\n"

                }
            }
        }

        stage('build & test') {
            steps {
                sh "./gradlew clean build test"

                step $class: 'JUnitResultArchiver', testResults: '**/TEST-*.xml'
                echo " ${reportOnTestsForBuild()}"
            }
        }

        stage("send slack ") {
            steps {
                script {
                    BUILD_USER = getBuildUser()
//                    BUILD_USER = "wde"
                }
                echo 'I will always say hello in the console.'
                slackSend channel: 'general',
                        color: 'good',
                        message: "" +
                                "result >>*${currentBuild.result} >>" +
                                "changeSets >>*${currentBuild.changeSets} >>" +
                                "rawBuild >>*${currentBuild.rawBuild} >>" +
                                "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"

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


    }

    post { // these post steps will get executed at the end of build
        always {
            echo ' post outside stages always '
//            sh '${currentBuild.result}'
        }

    }
}
