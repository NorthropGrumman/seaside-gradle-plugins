package com.ngc.seaside.gradle.plugins.parent

import aQute.bnd.gradle.BundleTaskConvention
import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.plugins.util.GradleUtil
import com.ngc.seaside.gradle.plugins.util.PropertyUtils
import com.ngc.seaside.gradle.plugins.util.Versions
import com.ngc.seaside.gradle.tasks.dependencies.DependencyReportTask
import com.ngc.seaside.gradle.tasks.dependencies.DownloadDependenciesTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
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
 */
class SeasideParentPlugin extends AbstractProjectPlugin {

    public static final String PARENT_TASK_GROUP_NAME = 'parent'
    public static final String SOURCE_JAR_TASK_NAME = 'sourcesJar'
    public static final String JAVADOC_JAR_TASK_NAME = 'javadocJar'
    public static final String ANALYZE_TASK_NAME = 'analyze'
    public static final String DOWNLOAD_DEPENDENCIES_TASK_NAME = 'downloadDependencies'
    public static final String DEPENDENCY_UPDATES_TASK_NAME = 'dependencyUpdates'
    public static final String DEPENDENCY_REPORT_TASK_NAME = 'dependencyReport'
    public static final String CLEANUP_DEPENDENCIES_TASK_NAME = 'cleanupDependencies'
    public static final String JENKINS_TASK_GROUP_NAME = 'Jenkins'
    public static final String NOTHING_TASK_NAME = 'nothing'
    public static final String LOCAL_TAG = 'local-'
    public static String REMOTE_TAG = ''

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
            configureForCi(project)

            project.afterEvaluate {
                project.logger.
                      lifecycle(String.format("%s: Setting project version to %s", project.name, project.version))
                /**
                 * Ensure to add the doclint option to the javadoc task if using Java 8.
                 */
                if (JavaVersion.current().isJava8Compatible()) {
                    project.tasks.getByName('javadoc') {
                        options.addStringOption('Xdoclint:none', '-quiet')
                    }
                }

                /**
                 * Add the standard repositories. All of the seaside content should be downloaded from
                 * the nexus consolidated repository.
                 */
                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
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

                /*
             * Ensure we call the 2 new tasks for generating the javadoc and sources artifact jars.
             */
                artifacts {
                    archives taskResolver.findTask(SOURCE_JAR_TASK_NAME)
                    archives taskResolver.findTask(JAVADOC_JAR_TASK_NAME)
                }

                /**
                 * Configure Sonarqube to use the Jacoco code coverage reports.
                 */
                sonarqube {
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
     * Adds any necessary configuration for supporting CI.
     */
    protected void configureForCi(Project project) {
        Task task = project.task(NOTHING_TASK_NAME)
        task.enabled = false
        task.group = JENKINS_TASK_GROUP_NAME
        task.description = 'Does nothing.  This task is useful when invoked from a Jenkins script since it allows '
        +'scripts to capture the values of build script properties without doing actual work.'

        // Configure the build to handle the system properties for updating the properties of the build.
        // Note the display property work could be done in task but the update property work cannot be (easily) done in
        // a task.  To be consistent, we don't do any of the work in a task.
        configurePropertyDisplay(project)
        // This work is not done in a task because we what to do the property replacement before any actual work is
        // done, including dependency resolution.  Ensuring that a task gets run before anything else can be difficult,
        // so we do this work in a "beforeEvaluate" project callback.
        configurePropertyUpdate(project)
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
