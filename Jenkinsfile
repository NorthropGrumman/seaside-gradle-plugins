pipeline {
    agent {
        label {
            label ""
            customWorkspace "${JENKINS_HOME}/workspace/seaside-gradle-plugins/${JOB_NAME}"
        }
    }

    stages {
        stage('Build') {
            steps {
                sh 'chmod +x gradlew && ./gradlew clean build -xtest -xintegrationTest -xfunctionalTest'
            }
        }

//        stage('Unit Test') {
//            steps {
//                sh './gradlew test -PtestIgnoreFailures=true -xintegrationTest -xfunctionalTest'
//            }
//
//           post {
//                always {
//                    junit '**/build/test-results/test/*.xml'
//                }
//            }
//
//        }

//        stage('Integration Test') {
//            steps {
//                sh './gradlew integrationTest -PtestIgnoreFailures=true -xfunctionalTest'
//            }
//            post {
//                always {
//                    junit '**/build/test-results/integrationTest/*.xml'
//                }
//            }
//        }

//        stage('Functional Test') {
//            steps {
//                sh './gradlew functionalTest -PtestIgnoreFailures=true'
//            }
//            post {
//                always {
//                    junit '**/build/test-results/functionalTest/*.xml'
//                }
//            }
//        }

        stage('Release') {
            when {
                branch 'experimental-pipeline'
            }
            steps {
                sh './gradlew clean prepareForRelease'
                sh 'GIT_ASKPASS=true ./gradlew release -x integrationTest -x functionalTest -x test'
//                withCredentials([usernamePassword(credentialsId: 'ngc-github-pipelines',
//                                                  passwordVariable: 'gitPassword',
//                                                  usernameVariable: 'gitUsername')]) {
//                    try {
//                        sh "git config credential.username $gitUsername"
//                        sh "git config credential.helper 'echo password=$gitPassword; echo'"
//                        sh 'GIT_ASKPASS=true ./gradlew release -x integrationTest -x functionalTest -x test'
//                    } finally {
//                        sh 'git config --unset credential.username'
//                        sh 'git config --unset credential.helper'
//                    }
//                }
            }
        }
    }

    // The options directive is for configuration that applies to the whole job.
    options {
        // For example, we'd like to make sure we only keep 10 builds at a time, so
        // we don't fill up our storage!
        buildDiscarder(logRotator(numToKeepStr: '5'))

        // And we'd really like to be sure that this build doesn't hang forever, so
        // let's time it out after an hour.
        timeout(time: 60, unit: 'MINUTES')
    }
}
