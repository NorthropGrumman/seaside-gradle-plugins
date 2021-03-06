/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

pipeline {
   agent {
      label {
         label ""
         customWorkspace "${JENKINS_HOME}/workspace/seaside-gradle-plugins/${JOB_NAME}"
      }
   }

   environment {
      // Specify the JDK required here.
      JAVA_HOME = "${JAVA_11_HOME}"
      PATH = "${JAVA_11_HOME}/bin:${PATH}"
   }

   parameters {
      booleanParam(name: 'upload',
                   description: 'If true, artifacts will be uploaded to the build\'s remote repository.  Don\'t use this option with performRelease.',
                   defaultValue: false)
      booleanParam(name: 'performRelease',
                   defaultValue: false,
                   description: 'If true, a release build will be performed.  Releases can only be performed from master.')
      booleanParam(name: 'nexusLifecycle',
                   description: 'If true, Nexus Lifecycle will scan for security issues.',
                   defaultValue: false)
   }

   stages {
      stage('Build') {
         steps {
            sh 'chmod +x gradlew && ./gradlew clean license build -xtest -xintegrationTest -xfunctionalTest -s'
         }
      }

      stage('Unit Test') {
         steps {
            sh './gradlew test -PtestIgnoreFailures=true -xintegrationTest -xfunctionalTest -s'
         }

         post {
            always {
               junit '**/build/test-results/test/*.xml'
            }
         }
      }

      stage('Integration Test') {
         steps {
            sh './gradlew integrationTest -PtestIgnoreFailures=true -xfunctionalTest -s'
         }
         post {
            always {
               junit '**/build/test-results/integrationTest/*.xml'
            }
         }
      }

      stage('Functional Test') {
         steps {
            withCredentials([usernamePassword(credentialsId: 'ngc-nexus-repo-mgr-pipelines',
                                              passwordVariable: 'nexusPassword',
                                              usernameVariable: 'nexusUsername')]) {
               sh './gradlew functionalTest -PtestIgnoreFailures=true -PnexusUsername=$nexusUsername -PnexusPassword=$nexusPassword -s'
            }
         }
         post {
            always {
               junit '**/build/test-results/functionalTest/*.xml'
            }
         }
      }

      // This stage only checks the build.gradle files.  The first build stage checks the source files for each
      // sub-project.
      stage('License Check') {
         steps {
            // This project consumes its own plugins.
            sh './gradlew prepareForRelease'
            sh './gradlew license -PbootstrapRelease'
         }
      }

      stage('Nexus Lifecycle') {
         when {
            expression { params.nexusLifecycle }
         }
         steps {
            // Evaluate the items for security, license, and other issues via Nexus Lifecycle.
            withCredentials([usernamePassword(credentialsId: 'ngc-nexus-lifecycle-pipelines',
                                              passwordVariable: 'lifecyclePassword',
                                              usernameVariable: 'lifecycleUsername')]) {
               script {
                  def policyEvaluationResult = nexusPolicyEvaluation(
                        failBuildOnNetworkError: false,
                        iqApplication: 'seaside-gradle-plugins',
                        iqStage: 'build',
                        jobCredentialsId: 'ngc-nexus-lifecycle-pipelines'
                  )
                  sh 'mkdir -p build'
                  sh "curl -s -S -L -k -u \"\$lifecycleUsername:\$lifecyclePassword\" '${policyEvaluationResult.applicationCompositionReportUrl}/pdf' > build/Nexus-Lifecycle-Report.pdf"
               }
            }
         }
      }

      stage('Upload') {
         when {
            expression { params.upload }
         }
         steps {
            withCredentials([usernamePassword(credentialsId: 'ngc-nexus-repo-mgr-pipelines',
                                              passwordVariable: 'nexusPassword',
                                              usernameVariable: 'nexusUsername')]) {
               sh './gradlew publish -PnexusUsername=$nexusUsername -PnexusPassword=$nexusPassword'
            }
         }
      }

      stage('Release') {
         when {
            expression { env.BRANCH_NAME == 'master' && params.performRelease }
         }
         steps {
            // Prepare for a release by building the plugins if necessary and set them up on disk so this
            // project's own build can reference them.  This is done in the License Check task so we don't need to do it
            // again.
            // Remove the version suffice.  Use -PbootstrapRelease to tell the build to put its own artifacts on
            // the classpath.
            sh './gradlew removeVersionSuffix -PbootstrapRelease'
            // Create the tag.
            sh './gradlew createReleaseTag -PbootstrapRelease'
            // Run the update.  Clean so the artifacts are built with the new version.
            withCredentials([usernamePassword(credentialsId: 'ngc-nexus-repo-mgr-pipelines',
                                              passwordVariable: 'nexusPassword',
                                              usernameVariable: 'nexusUsername')]) {
               sh './gradlew clean build publish -x integrationTest -x functionalTest -x test -PnexusUsername=$nexusUsername -PnexusPassword=$nexusPassword'
            }
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

      stage('Archive') {
         steps {
            archiveArtifacts allowEmptyArchive: true,
                             artifacts: 'build/Nexus-Lifecycle-Report.pdf',
                             caseSensitive: false,
                             defaultExcludes: false,
                             onlyIfSuccessful: true
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
