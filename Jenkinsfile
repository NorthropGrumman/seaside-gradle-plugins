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
                // Prepare for a release by building the plugins if necessary and set them up on disk so this
                // project's own build can reference them.
                sh './gradlew prepareForRelease'
                // Remove the version suffice.  Use -PbootstrapRelease to tell the build to put its own artifacts on
                // the classpath.
                sh './gradlew removeVersionSuffix -PbootstrapRelease'
                // Create the tag.
                sh './gradlew createReleaseTag -PbootstrapRelease'
                // Run the update.  Clean so the artifacts are built with the new version.
                sh './gradlew clean build upload -x integrationTest -x functionalTest -x test'
                // Since we just did a clean, we need to run prepareForRelease again so the next step of pushing the
                // tag will work.
                sh './gradlew prepareForRelease'
                // Bump the version to the next snapshot.
                sh './gradlew bumpTheVersion -PbootstrapRelease'

                script {
                    try {
                        // This allows us to run Git commands with the credentials from Jenkins.  See
                        // https://groups.google.com/forum/#!topic/jenkinsci-users/BPdw6EOP0fQ
                        // and https://stackoverflow.com/questions/33570075/tag-a-repo-from-a-jenkins-workflow-script
                        // for more information.
                        withCredentials([usernamePassword(credentialsId: 'ngc-github-pipelines',
                                                          passwordVariable: 'gitPassword',
                                                          usernameVariable: 'gitUsername')]) {
                            // This allows use to use a custom credential helper that uses the values from Jenkins.
                            sh "git config credential.helper '!echo password=\$gitPassword; echo username=\$gitUsername; echo'"
                            sh 'GIT_ASKPASS=true ./gradlew releasePush -PbootstrapRelease'
                        }
                    } finally {
                        sh 'git config --unset credential.helper'
                    }
                }
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
