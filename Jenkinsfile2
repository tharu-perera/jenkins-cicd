def COLOR_MAP = ['SUCCESS': 'good', 'FAILURE': 'danger', ]
blocks = [["type": "section", "text": ["type": "mrkdwn", "text": "Hello, Assistant to the Regional Manager Dwight! *Michael Scott* wants to know where you'd like to take the Paper Company investors to dinner tonight.\n\n *Please select a restaurant:*"]], ["type": "divider"], ["type": "section", "text": ["type": "mrkdwn", "text": "*Farmhouse Thai Cuisine*\n:star::star::star::star: 1528 reviews\n They do have some vegan options, like the roti and curry, plus they have a ton of salad stuff and noodles can be ordered without meat!! They have something for everyone here"], "accessory": ["type": "image", "image_url": "https://s3-media3.fl.yelpcdn.com/bphoto/c7ed05m9lC2EmA3Aruue7A/o.jpg", "alt_text": "alt text for image"]]]
def attachments = [[
text: 'I find your lack of faith disturbing!', fallback: 'Hey, Vader seems to be mad at you.', color: 'green']]

def getBuildUser() {
  return currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
}

pipeline {

  agent {
    node {
      label 'master'
    }
  }

  options {
    buildDiscarder logRotator(
    daysToKeepStr: '16', numToKeepStr: '10')
  }
  environment {
    APP_NAME = "DCUBE_APP"
    APP_ENV = "DEV"
  }
  stages {

    stage('Cleanup Workspace') {
      steps {
        cleanWs()
        sh """
                echo "
        Cleaned Up Workspace For Project "
                """
      }
    }
    stage('Checkout') {
      steps { //Checking out the repo
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '3f16424e-a2b9-4bd4-b787-5a3987dfc84c', url: 'https://github.com/tharindu-perera/jenkins-cicd.git']]])

        script {
          try {
            sh 'chmod +x gradlew'

            sh './gradlew build -x test --no-daemon'
                        sh './gradlew test jacocoTestReport  jacocoTestCoverageVerification --no-daemon'
            sh './gradlew test jacocoTestReport    --no-daemon'
          } finally {
            print('xxxxxxxxxxxxxx')
            //                         junit '**/build/test-results/test/*.xml' //make
            //                         the junit test results available in any case
            //                         (success & failure)
          }
        }

        junit '**/build/test-results/test/*.xml'
        // step( [ $class: 'JacocoPublisher' ] )

         script {

               def scannerHome = tool 'sonarqube';

                   withSonarQubeEnv("sonarqube-container") {

                   sh "${tool("sonarqube")}/bin/sonar-scanner  -Dsonar.projectKey=test-node-js  -Dsonar.sources=. -Dsonar.css.node=.  -Dsonar.host.url=http://localhost:9000   -Dsonar.login=your-generated-token-from-sonarqube-container"

                       }

                   }



//         jacoco deltaLineCoverage: '50', exclusionPattern: '**/*Test*.class' , inclusionPattern: '**/*.class',   maximumLineCoverage: '90', changeBuildStatus: true

        slackSend(channel: "#general", color: COLOR_MAP[currentBuild.currentResult], message: "*${currentBuild.currentResult} * Deploy Approval: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.JOB_DISPLAY_URL})", attachments: attachments, blocks: blocks)
        // script {
        //   try {
        //     timeout(time: 30, unit: 'MINUTES') {
        //       env.APPROVE_PROD = input message: 'Deploy to Production',
        //       ok: 'Continue',
        //       parameters: [choice(name: 'APPROVE_PROD', choices: 'YES\nNO', description: 'Deploy from STAGING to PRODUCTION?')]
        //       if (env.APPROVE_PROD == 'YES') {
        //         env.DPROD = true
        //       } else {
        //         env.DPROD = false
        //       }
        //     }
        //   } catch(error) {
        //     env.DPROD = true
        //     echo 'Timeout has been reached! Deploy to PRODUCTION automatically activated'
        //   }
        // }
      }
    }

  }

}