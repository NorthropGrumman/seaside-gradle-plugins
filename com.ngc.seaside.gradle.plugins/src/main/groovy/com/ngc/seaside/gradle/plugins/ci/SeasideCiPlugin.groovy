package com.ngc.seaside.gradle.plugins.ci

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.ci.SeasideCiExtension
import com.ngc.seaside.gradle.tasks.dependencies.PopulateMaven2Repository
import com.ngc.seaside.gradle.util.PropertyUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.bundling.Zip

/**
 * This plugin is applied to projects to make CI easier.
 *
 * <p/>
 *
 * This plugin also supports the {@code display.property.name} system property.  If If set, the value of the build
 * script property will be displayed.  The value can be a comma delimited list of properties.  Each property will be
 * printed on a separate line.  This is useful for Jenkins scripts which want to capture the value of some property.
 * A task named {@code nothing} is configured that allows a script to invoke Gradle with the system properties but
 * no part of the build is actually performed.  For example,
 * <pre>
 *     fooVersion=$(gradle -q nothing -Ddisplay.property.name=version)
 * </pre>
 * If the plugin is applied to mutiple projects in the same build, use
 * <pre>
 *     fooVersion=$(gradle -q nothing -Ddisplay.property.name=version | head -1)
 * </pre>
 *
 * <p/>
 *
 * The system properties {@code update.property.name} and {@code update.property.value} can also be used to replace
 * build script properties before the build is started.  These values can be a comma delimited list of properties and
 * values.  The property names and values should be listed in the same order.  For example,
 * <pre>
 *     gradle clean build -Dupdate.property.name=version,barVersion -Dupdate.property.value=1.0-SNAPSHOT,2.3
 * </pre>
 *
 * <p/>
 *
 * This plugin also applies the {@link PopulateMaven2Repository} task.
 */
class SeasideCiPlugin extends AbstractProjectPlugin {

   public static final String JENKINS_TASK_GROUP_NAME = 'Jenkins'
   public static final String AUDITING_TASK_GROUP_NAME = 'Auditing'
   public static final String NOTHING_TASK_NAME = 'nothing'
   public static final String POPULATE_M2_REPO_TASK_NAME = 'populateM2repo'
   public static final String CREATE_M2_REPO_ARCHIVE_TASK_NAME = 'm2repo'
   public static final String CONTINUOUS_INTEGRATION_TASK_NAME = 'ci'

   /**
    * The default name of the directory that will contain the m2 dependencies.
    */
   final static String DEFAULT_M2_OUTPUT_DIRECTORY_NAME = 'dependencies-m2'

   /**
    * The default name of the dependencies report file.
    */
   final static String DEFAULT_DEPENDENCY_REPORT_FILE_NAME = 'dependencies.tsv'

   /**
    * The default name of the M2 deployment script.
    */
   final static String DEFAULT_M2_DEPLOYMENT_SCRIPT_NAME = "deploy.sh"

   /**
    * The name of the system property used when printing the value of a property.
    */
   private final static String DISPLAY_PROPERTY_NAME = 'display.property.name'

   /**
    * The name of the system property used when updating a property of the build.  The value may be a comma delimited
    * list of property names.
    */
   private final static String UPDATE_PROPERTY_NAME = 'update.property.name'

   /**
    * The name of the system property used when updating a property of the build.  The value may be a comma delimited
    * list of values for properties.
    */
   private final static String UPDATE_PROPERTY_VALUE = 'update.property.value'

   /**
    * The CI extension that the user can use to customize the plugin.
    */
   private SeasideCiExtension ciExtension

   @Override
   void doApply(Project project) {
      project.getPlugins().apply('checkstyle')
      project.configure(project) {
         configureExtensions(project)
         createTasks(project)

         // This work is not done in a task because we what to do the property replacement before any actual work is
         // done, including dependency resolution.  Ensuring that a task gets run before anything else can be difficult,
         // so we do this work in a "beforeEvaluate" project callback.
         configurePropertyUpdate(project)
         // Configure the build to handle the system properties for updating the properties of the build.
         // Note the display property work could be done in task but the update property work cannot be (easily) done in
         // a task.  To be consistent, we don't do any of the work in a task.
         configurePropertyDisplay(project)
         // Configure the m2 repo task from values of the extension.
         configureAuditingTasks(project)
      }
   }

   /**
    * Adds additional CI based tasks.
    */
   protected void createTasks(Project project) {
      Task doNothing = project.task(NOTHING_TASK_NAME)
      doNothing.enabled = false
      doNothing.group = JENKINS_TASK_GROUP_NAME
      doNothing.description =
            'Does nothing.  This task is useful when invoked from a Jenkins script since it allows scripts to capture the values of build script properties without doing actual work.'

      Task populateM2Repo = project.task(
            POPULATE_M2_REPO_TASK_NAME,
            type: PopulateMaven2Repository,
            group: AUDITING_TASK_GROUP_NAME,
            description: 'Creates a directory which contains all dependencies in a maven2 layout which can be used for offline use.') {
         localRepository = mavenLocal()
      }

      project.task(
            CREATE_M2_REPO_ARCHIVE_TASK_NAME,
            dependsOn: populateM2Repo,
            type: Zip,
            group: AUDITING_TASK_GROUP_NAME,
            description: 'Creates a ZIP archive of the populated m2 repository.'
      )

      project.tasks.withType(Checkstyle) { task ->
         enabled = project.gradle.startParameter.taskNames.contains(task.name) }

      // do not make clean a dependency it seems to run after all the other task have been run
      def buildTask = taskResolver.findTask("build")
      def cleanTask = taskResolver.findTask("clean")
      def installTask = taskResolver.findTask("install")
      def checkStyleMain = taskResolver.findTask("checkstyleMain")
      def checkStyleTest = taskResolver.findTask("checkstyleTest")
      Task ci = project.task(
            CONTINUOUS_INTEGRATION_TASK_NAME,
            dependsOn: [buildTask, checkStyleMain, checkStyleTest, installTask],
            type: DefaultTask,
            group: AUDITING_TASK_GROUP_NAME,
            description: 'does the checkStyleMain and checkStyleTest'
      )

   }

   /**
    * Configures extensions for the plugin.
    * @param project
    */
   private void configureExtensions(Project project) {
      ciExtension = project.extensions.create("seasideCi", SeasideCiExtension)
   }

   /**
    * Configures the tasks related to auditing and security.  Applies the configuration after the project has been evaluated
    * so the user has a chance to set the extensions.
    */
   private void configureAuditingTasks(Project project) {
      // Configure the task with values from the extension after the project is evaluated so the user has a chance
      // to override the settings.
      project.afterEvaluate {
         File m2Directory = ciExtension.m2OutputDirectory ?: new File(project.buildDir,
                                                                      DEFAULT_M2_OUTPUT_DIRECTORY_NAME)

         getTaskResolver().findTask(POPULATE_M2_REPO_TASK_NAME) {
            // Note findByName returns null if the repo could not be found.  It is okay if the repo is not
            // defined.  In this case, the task will just resolve dependencies from the local maven repository
            // directory.
            remoteRepository = project.repositories.findByName(ciExtension.remoteM2RepositoryName)
            // Configure the output directory using $buildDir/m2 as the default.
            outputDirectory = m2Directory
            // Setup the configurations.
            configurationsToResolve = ciExtension.getConfigurationsToResolve()
            // Configure the dependencies report.
            createDependencyReportFile = ciExtension.createDependencyReportFile
            dependencyInfoReportFile = ciExtension.dependencyInfoReportFile ?:
                                       new File(project.buildDir, DEFAULT_DEPENDENCY_REPORT_FILE_NAME)
            // Configure the deployment script.
            deploymentScriptFile = ciExtension.deploymentScriptFile ?:
                                   new File(project.buildDir, DEFAULT_M2_DEPLOYMENT_SCRIPT_NAME)
            configurations = ciExtension.configs

         }

         getTaskResolver().findTask(CREATE_M2_REPO_ARCHIVE_TASK_NAME) {
            from m2Directory
            destinationDir = ciExtension.m2ArchiveOutputDirectory ?: project.buildDir
            archiveName = ciExtension.m2ArchiveName
         }
      }
   }

   /**
    * Sets up a callback that is invoked before the project is update.d  The callback will display the values of all
    * properties named via the {@link #DISPLAY_PROPERTY_NAME} system property.
    * @param project
    */
   private static void configurePropertyDisplay(Project project) {
      project.afterEvaluate {
         String displayPropertyName = System.getProperty(DISPLAY_PROPERTY_NAME)
         if (displayPropertyName != null) {
            PropertyUtils.getProperties(project, displayPropertyName).forEach({ v ->
               // Use the quiet log level so that the output is always printed.  This makes parsing the output
               // within a shell script easier.
               project.logger.quiet(v.toString())
            })
         }
      }
   }

   /**
    * Sets up a callback that is invoked before the project is updated.  The callback will update any properties
    * configured for the build if the {@link #UPDATE_PROPERTY_NAME} and {@link #UPDATE_PROPERTY_VALUE} system
    * properties are set.
    */
   private static void configurePropertyUpdate(Project project) {
      project.beforeEvaluate {
         String updatePropertyName = System.getProperty(UPDATE_PROPERTY_NAME)
         String updatePropertyValue = System.getProperty(UPDATE_PROPERTY_VALUE)
         if (updatePropertyName != null && updatePropertyValue != null) {
            PropertyUtils.setProperties(project, updatePropertyName, updatePropertyValue)
         }
      }
   }
}
