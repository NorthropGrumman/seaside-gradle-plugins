package com.ngc.seaside.gradle.plugins.parent

import aQute.bnd.gradle.BundleTaskConvention
import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.tasks.dependencies.DependencyReportTask
import com.ngc.seaside.gradle.tasks.dependencies.DownloadDependenciesTask
import com.ngc.seaside.gradle.util.GradleUtil
import com.ngc.seaside.gradle.util.Versions
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.bundling.Jar

/**
 * The seaside parent plugin provides calls to common tasks, sets up the default dependencies for BLoCS and OSGi along
 * with providing nexus repository deployment settings.
 *
 * The following properties are required in your ~/.gradle/gradle.properties file to use this plugin.
 * <pre>
 *     nexusUsername     : the username to use when uploading artifacts to nexus
 *     nexusPassword     : the password to use when uploading artifacts to nexus
 *     nexusReleases     : url to the releases repository
 *     nexusSnapshots    : url to the snapshots repository
 *     nexusConsolidated : url to the maven public download site
 *                         usually a proxy to maven central and the releases and snapshots
 *     systemProp.sonar.host.url : url to the Sonarqube server
 * </pre>
 */
class SeasideParentPlugin extends AbstractProjectPlugin {

   public static final String REMOTE_MAVEN_REPOSITORY_NAME = 'NexusConsolidated'
   public static final String PARENT_TASK_GROUP_NAME = 'parent'
   public static final String SOURCE_JAR_TASK_NAME = 'sourcesJar'
   public static final String JAVADOC_JAR_TASK_NAME = 'javadocJar'
   public static final String ANALYZE_TASK_NAME = 'analyze'
   public static final String DOWNLOAD_DEPENDENCIES_TASK_NAME = 'downloadDependencies'
   public static final String DEPENDENCY_UPDATES_TASK_NAME = 'dependencyUpdates'
   public static final String DEPENDENCY_REPORT_TASK_NAME = 'dependencyReport'
   public static final String CLEANUP_DEPENDENCIES_TASK_NAME = 'cleanupDependencies'
   public static final String ALL_TASK_NAME = 'all'
   public static final String LOCAL_TAG = 'local-'
   public static final String CHECKSTYLE_TOOL_VERSION = '8.8'
   public static final String CHECKSTYLE_FAIL_ON_ERROR_PROPERTY = 'fail-on-checkstyle-error'
   public static final String CHECKSTYLE_CONFIG_FILE_NAME = 'ceacide_checks.xml'
   public static final String CHECKSTYLE_CONFIG_FILE = 'checkstyle/' + CHECKSTYLE_CONFIG_FILE_NAME
   public static final String CHECKSTYLE_SUPPRESS_FILE_NAME = 'suppressions.xml'
   public static final String CHECKSTYLE_SUPPRESS_FILE = 'checkstyle/' + CHECKSTYLE_SUPPRESS_FILE_NAME

   public static String REMOTE_TAG = ''


   @Override
   void doApply(Project project) {
      project.configure(project) {
         applyPlugins(project)

         // Make sure that all required properties are set.
         doRequireGradleProperties(project,
                                   'nexusConsolidated',
                                   'nexusReleases',
                                   'nexusSnapshots',
                                   'nexusUsername',
                                   'nexusPassword')
         doRequireSystemProperties(project)
         createTasks(project)

         /**
          * Add the standard repositories. All of the seaside content should be downloaded from
          * the nexus consolidated repository.
          */
         repositories {
            mavenLocal()

            maven {
               credentials {
                  username nexusUsername
                  password nexusPassword
               }
               name REMOTE_MAVEN_REPOSITORY_NAME
               url nexusConsolidated
            }
         }

         project.afterEvaluate {
            project.logger.
                  lifecycle(String.format("%s: Setting project version to %s", project.name, project.version))
            /**
             * Ensure to add the doclint option to the javadoc task if using Java 8.
             */
            if (JavaVersion.current().isJava8Compatible()) {
               taskResolver.findTask('javadoc') {
                  options.addStringOption('Xdoclint:none', '-quiet')
               }
            }

            /**
             *
             */
            ext {
               // The default name of the bundle.
               bundleName = project.group + '.' + project.name
            }

            /**
             * Configure the dependencyUpdates task report path
             */
            taskResolver.findTask(DEPENDENCY_UPDATES_TASK_NAME) {
               outputDir = ["build", DEPENDENCY_UPDATES_TASK_NAME].join(File.separator)
            }

            /**
             * Augment the jar name to be $groupId.$project.name).
             */
            taskResolver.findTask(SOURCE_JAR_TASK_NAME) { jar ->
               archiveName = "${project.group}.${project.name}-${project.version}-${classifier}.jar"
            }

            taskResolver.findTask(JAVADOC_JAR_TASK_NAME) { jar ->
               archiveName = "${project.group}.${project.name}-${project.version}-${classifier}.jar"
            }

            /**
             * Augment the jar to be OSGi compatible.
             */
            taskResolver.findTask('jar') { jar ->
               convention.plugins.bundle = new BundleTaskConvention((Jar) jar)

               archiveName = "${project.group}.${project.name}-${project.version}.jar"

               doLast {
                  buildBundle()
               }

               manifest {
                  attributes('Bundle-Name': "$bundleName",
                             'Bundle-SymbolicName': "$bundleName",
                             'Bundle-Version': Versions.makeOsgiCompliantVersion("${project.version}"))
               }
            }

            /**
             * The nexus upload configuration.
             */
            uploadArchives {
               repositories {
                  mavenDeployer {
                     // Use the main repo for full releases.
                     repository(url: nexusReleases) {
                        // Make sure that nexusUsername and nexusPassword are in your
                        // ${gradle.user.home}/gradle.properties file.
                        authentication(userName: nexusUsername, password: nexusPassword)
                     }
                     // If the version has SNAPSHOT in the name, use the snapshot repo.
                     snapshotRepository(url: nexusSnapshots) {
                        authentication(userName: nexusUsername, password: nexusPassword)
                     }
                  }
               }
            }

            /**
             * Configure artifacts to be uploaded to Nexus.  We only configure these if the upload task is run.
             * This saves some time on the build.
             */
            def taskNames = project.gradle.startParameter.taskNames
            if (taskNames.contains('upload') || taskNames.contains('uploadArchives')) {
               project.artifacts {
                  archives taskResolver.findTask(SOURCE_JAR_TASK_NAME)
                  archives taskResolver.findTask(JAVADOC_JAR_TASK_NAME)
               }
            }

            /**
             * Ensure that checkstyleMain can only be called when running the task from the command line
             * We don't want checkstyle to run on ever build due to time.
             */
            tasks.withType(Checkstyle) {
               task -> enabled = project.gradle.startParameter.taskNames.contains(task.name)

               if(project.gradle.startParameter.taskNames.contains(task.name)) {
                  CheckstyleExtension extension = project.getExtensions().getByType(CheckstyleExtension.class)
                  File tempDir = File.createTempDir()

                  def resource = SeasideParentPlugin.class
                        .getClassLoader()
                        .getResourceAsStream(CHECKSTYLE_CONFIG_FILE)
                        .text
                  def suppressionResource = SeasideParentPlugin.class
                        .getClassLoader()
                        .getResourceAsStream(CHECKSTYLE_SUPPRESS_FILE)
                        .text

                  File output = new File(tempDir, CHECKSTYLE_CONFIG_FILE_NAME)
                  output << resource

                  File suppressionsOutput = new File(tempDir, CHECKSTYLE_SUPPRESS_FILE_NAME)
                  suppressionsOutput << suppressionResource

                  extension.configFile = output
                  extension.configProperties = [ "suppressionFile" : suppressionsOutput.getAbsolutePath() ]
               }
            }

            /**
             * The checkstyle configuration creates a temporary directory and stores the configuration from
             * this projects resources into it for each project being built.
             */
            checkstyle {
               toolVersion CHECKSTYLE_TOOL_VERSION

               //determine if the fail on error property has been set and set the appropriate
               //configuration options if it is set to 'true'
               //by default, this will not fail the build.
               if (project.hasProperty(CHECKSTYLE_FAIL_ON_ERROR_PROPERTY)) {
                  if (project.findProperty(CHECKSTYLE_FAIL_ON_ERROR_PROPERTY) == "true") {
                     ignoreFailures = false
                     maxErrors = 0
                     maxWarnings = 0
                  }
               }
            }

            /**
             * Configure Sonarqube to use the Jacoco code coverage reports.
             */
            sonarqube {
               check
               properties {
                  if (new File("${project.buildDir}/jacoco/test.exec").exists()) {
                     property 'sonar.jacoco.reportPaths', ["${project.buildDir}/jacoco/test.exec"]
                  }
                  property 'sonar.projectName', "${bundleName}"
                  property 'sonar.branch', getBranchName()
               }
            }
         }
         project.defaultTasks = ['build']
      }
   }

   /**
    * This will get the current working branch to pass to sonarqube
    * @return String with of the Git Branch you are on otherwise
    * an empty string
    */
   static String getBranchName() {

      def command = "git rev-parse --abbrev-ref HEAD"
      StringBuilder branchName = new StringBuilder()
      def process = command.execute()
      //append local to branch name
      if (isBuildLocal()) {
         branchName.append(LOCAL_TAG)
      } else {
         branchName.append(REMOTE_TAG)
      }

      branchName.append(process.text.trim())

      return branchName.toString().trim()
   }

   /**
    *
    * @return true if local and false if not
    */
   static boolean isBuildLocal() {
      boolean isLocal = true

      if (System.getenv('JENKINS_HOME') != null) {
         REMOTE_TAG = 'jenkins-'
         isLocal = false
      }

      return isLocal
   }

   /**
    *
    * @param project
    * @param propertyName
    * @param propertyNames
    */
   protected static void doRequireGradleProperties(Project project, String propertyName, String... propertyNames) {
      GradleUtil.requireProperties(project.properties, propertyName, propertyNames)
   }

   /**
    *
    * @param project
    */
   protected static void doRequireSystemProperties(Project project) {
      GradleUtil.requireSystemProperties(project.properties, 'sonar.host.url')

   }

   /**
    * Applies additional plugins to the project the project
    * @param project
    */
   private static void applyPlugins(Project project) {
      project.logger.info(String.format("Applying plugins for %s", project.name))
      project.getPlugins().apply('java')
      project.getPlugins().apply('maven')
      project.getPlugins().apply('eclipse')
      project.getPlugins().apply('jacoco')
      project.getPlugins().apply('org.sonarqube')
      project.getPlugins().apply('com.github.ben-manes.versions')
      project.getPlugins().apply('com.github.ksoichiro.console.reporter')
      project.getPlugins().apply(SeasideReleasePlugin)
      project.getPlugins().apply(SeasideCiPlugin)
      project.getPlugins().apply('checkstyle')
   }

   /**
    * Create project tasks that are exposed by applying this plugin
    * @param project
    */
   private void createTasks(Project project) {
      /**
       * Create a task for generating the source jar. This will also be uploaded to Nexus.
       */
      def classesTask = taskResolver.findTask("classes")
      project.task(SOURCE_JAR_TASK_NAME, type: Jar) {
         classifier = 'sources'
         from project.sourceSets.main.allSource
      }
      taskResolver.findTask(SOURCE_JAR_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
      taskResolver.findTask(SOURCE_JAR_TASK_NAME).dependsOn(classesTask)

      /**
       * Create a task for generating the javadoc jar. This will also be uploaded to Nexus.
       */
      def javadocsTask = taskResolver.findTask("javadoc")
      project.task(JAVADOC_JAR_TASK_NAME, type: Jar) {
         classifier = 'javadoc'
         from javadocsTask.destinationDir
      }
      taskResolver.findTask(JAVADOC_JAR_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
      taskResolver.findTask(JAVADOC_JAR_TASK_NAME).dependsOn([classesTask, javadocsTask])

      /**
       * analyzeBuild task for sonarqube
       */
      def buildTask = taskResolver.findTask("build")
      def jacocoReportTask = taskResolver.findTask("jacocoTestReport")
      def sonarqubeTask = taskResolver.findTask("sonarqube")
      project.task(ANALYZE_TASK_NAME)
      taskResolver.findTask(ANALYZE_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
      taskResolver.findTask(ANALYZE_TASK_NAME).dependsOn([buildTask, jacocoReportTask, sonarqubeTask])
      taskResolver.findTask(ANALYZE_TASK_NAME).setDescription('Runs jacocoTestReport and sonarqube')

      /**
       * downloadDependencies task
       */
      project.task(DOWNLOAD_DEPENDENCIES_TASK_NAME, type: DownloadDependenciesTask) {}
      taskResolver.findTask(DOWNLOAD_DEPENDENCIES_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
      taskResolver.findTask(DOWNLOAD_DEPENDENCIES_TASK_NAME).setDescription(
            'Downloads all dependencies into the build/dependencies/ folder using maven2 layout.')

      /**
       * cleanupDependencies task
       */
      project.task(CLEANUP_DEPENDENCIES_TASK_NAME, type: DownloadDependenciesTask) {
         customRepo = project.getProjectDir().path + "/build/dependencies-tmp"
         doLast {
            ext.actualRepository = project.downloadDependencies.localRepository ?
                                   project.downloadDependencies.localRepository : project.file(
                  [project.buildDir, 'dependencies'].join(File.separator))

            logger.info("Moving cleaned up repository from ${localRepository.absolutePath} to " +
                        "${actualRepository.absolutePath}.")
            project.delete(actualRepository)
            project.copy {
               from localRepository
               into actualRepository
            }
            project.delete(localRepository)
         }
      }
      taskResolver.findTask(CLEANUP_DEPENDENCIES_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
      taskResolver.findTask(CLEANUP_DEPENDENCIES_TASK_NAME).
            setDescription('Remove unused dependencies from dependencies folder.')

      /**
       * dependencyReport task
       */
      project.task(DEPENDENCY_REPORT_TASK_NAME, type: DependencyReportTask,
                   description: 'Lists all dependencies. Use -DshowTransitive=<bool> to show/hide transitive dependencies')

      /**
       * Configure a task that does everything.
       */
      def javadocJarTask = taskResolver.findTask(JAVADOC_JAR_TASK_NAME)
      def sourceJarTask = taskResolver.findTask(SOURCE_JAR_TASK_NAME)
      project.task(ALL_TASK_NAME,
                   dependsOn: [buildTask, javadocJarTask, sourceJarTask],
                   description: 'Performs a full build and generates all artifacts including Javadoc and source JARs.')
      taskResolver.findTask(ALL_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)


   }
}
