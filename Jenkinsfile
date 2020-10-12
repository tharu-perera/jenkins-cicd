#!groovy
def author = ""
def DEPLOY_QA = 'qa'
def buildCause = ""

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
                    buildCause = currentBuild.getBuildCauses()[0].shortDescription
                    echo "Current build was caused by: ${buildCause}\n"

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

        stage('inform  build status to slack ') {
            steps {
                echo 'inform  build status to slack'
                slackSend channel: 'general',
                        color: 'good',
                        message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"

            }
        }
    }

    post { // these post steps will get executed at the end of build
        always {
            echo ' post outside stages always '
            sh '${currentBuild.result}'
        }
        failure {
            echo ' post outside stages failure '
            sh '${currentBuild.result}'
        }

    }
}
