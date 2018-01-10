pipeline {
    agent {
        label {
            label ""
            customWorkspace "${JENKINS_HOME}/workspace/${JOB_NAME}"
        }
    }

    stages {
        stage('Build') {
            steps {
                sh 'chmod +x gradlew && ./gradlew clean build -xtest -xintegrationTest -xfunctionalTest'
            }
        }

        stage('Unit Test') {
            steps {
                sh './gradlew test -PtestIgnoreFailures=true -xintegrationTest -xfunctionalTest'
            }

            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }

        }

        stage('Integration Test') {
            steps {
                sh './gradlew integrationTest -PtestIgnoreFailures=true -xfunctionalTest'
            }
            post {
                always {
                    junit '**/build/test-results/integrationTest/*.xml'
                }
            }
        }

        stage('Functional Test') {
            steps {
                sh './gradlew functionalTest -PtestIgnoreFailures=true'
            }
            post {
                always {
                    junit '**/build/test-results/functionalTest/*.xml'
                }
            }
        }

        stage('Release') {
            when {
                branch 'master'
            }
            steps {
                sh './gradlew clean prepareForRelease'
                sh './gradlew release -x integrationTest -x functionalTest -x test'
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
