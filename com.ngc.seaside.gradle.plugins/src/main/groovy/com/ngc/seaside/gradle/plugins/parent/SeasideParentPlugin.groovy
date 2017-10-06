package com.ngc.seaside.gradle.plugins.parent

import aQute.bnd.gradle.BundleTaskConvention
import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.plugins.util.GradleUtil
import com.ngc.seaside.gradle.plugins.util.TaskResolver
import com.ngc.seaside.gradle.plugins.util.Versions
import com.ngc.seaside.gradle.tasks.dependencies.DependencyReportTask
import com.ngc.seaside.gradle.tasks.dependencies.DownloadDependenciesTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

/**
 * The seaside parent plugin provides calls to common task, sets up the default dependencies for BLoCS and OSGi along
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
class SeasideParentPlugin implements Plugin<Project> {

    public static final String PARENT_TASK_GROUP_NAME = 'MainBuild'
    public static final String PARENT_SOURCE_JAR_TASK_NAME = 'sourcesJar'
    public static final String PARENT_JAVADOC_JAR_TASK_NAME = 'javadocJar'
    public static final String PARENT_ANALYZE_TASK_NAME = 'analyze'
    public static final String PARENT_DOWNLOAD_DEPENDENCIES_TASK_NAME = 'downloadDependencies'
    public static final String PARENT_CLEANUP_DEPENDENCIES_TASK_NAME = 'cleanupDependencies'
    public static final String LOCAL_TAG = 'local-'
    public static String REMOTE_TAG = ''

    @Override
    void apply(Project project) {

        project.configure(project) {

            // Make sure that all required properties are set.
            doRequiredGradleProperties(project,
                                       'nexusConsolidated',
                                       'nexusReleases',
                                       'nexusSnapshots',
                                       'nexusUsername',
                                       'nexusPassword')
            doRequiredSystemProperties(project)
            applyPlugins(project)
            createTasks(project)

            afterEvaluate {
                /**
                 * Ensure to add the doclint option to the javadoc task if using Java 8.
                 */
                if (JavaVersion.current().isJava8Compatible()) {
                    tasks.getByName('javadoc') { doc ->
                        options.addStringOption('Xdoclint:none', '-quiet')
                    }
                }

                project.tasks.getByName('build') {
                    SeasideReleaseExtension releaseExtension = project.getExtensions().
                            getByType(SeasideReleaseExtension.class)
                    project.version = releaseExtension.getReleaseVersion()
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
                    bundleName = "$group" + '.' + "$project.name"
                }

                /**
                 * Configure the dependencyUpdates task report path
                 */
                tasks.getByName('dependencyUpdates') {
                    outputDir = ["build", "dependencyUpdates"].join(File.separator)
                }

                /**
                 * Augment the jar name to be $groupId.$project.name).
                 */
                tasks.getByName(PARENT_SOURCE_JAR_TASK_NAME) { jar ->
                    archiveName = "${project.group}.${project.name}-${project.version}-${classifier}.jar"
                }

                tasks.getByName(PARENT_JAVADOC_JAR_TASK_NAME) { jar ->
                    archiveName = "${project.group}.${project.name}-${project.version}-${classifier}.jar"
                }

                /**
                 * Augment the jar to be OSGi compatible.
                 */
                tasks.getByName('jar') { jar ->
                    convention.plugins.bundle = new BundleTaskConvention(jar)

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
                    archives tasks.getByName(PARENT_SOURCE_JAR_TASK_NAME)
                    archives tasks.getByName(PARENT_JAVADOC_JAR_TASK_NAME)
                }

                /**
                 * Configure Sonarqube to use the Jacoco code coverage reports.
                 */
                sonarqube {
                    properties {
                        property 'sonar.jacoco.reportPaths', ["${project.buildDir}/jacoco/test.exec"]
                        property 'sonar.projectName', "${bundleName}"
                        property 'sonar.branch', getBranchName()
                    }
                }
            }

            task('dependencyReport', type: DependencyReportTask,
                 description: 'Lists all dependencies. Use -DshowTransitive=<bool> to show/hide transitive dependencies') {
            }

            defaultTasks = ['build']
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
        if(isBuildLocal()) {
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

        if (System.getenv('JENKINS_HOME') != null ){
            REMOTE_TAG = 'jenkins-
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
    protected void doRequiredGradleProperties(Project project, String propertyName, String... propertyNames) {
        GradleUtil.requireProperties(project.properties, propertyName, propertyNames)
    }

    /**
     *
     * @param project
     */
    protected void doRequiredSystemProperties(Project project) {
        GradleUtil.requireSystemProperties(project.properties,
                                           'sonar.host.url')

    }

    /**
     * This plugin requires the java and maven plugins
     * @param project
     */
    protected void applyPlugins(Project project) {
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
     *
     * @param project
     */
    protected void createTasks(Project project) {

        /**
         * Create a task for generating the source jar. This will also be uploaded to Nexus.
         */
        def classesTask = project.tasks.getByName("classes")
        project.task(PARENT_SOURCE_JAR_TASK_NAME, type: Jar) {
            classifier = 'sources'
            from project.sourceSets.main.allSource
        }
        project.tasks.getByName(PARENT_SOURCE_JAR_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
        project.tasks.getByName(PARENT_SOURCE_JAR_TASK_NAME).dependsOn(classesTask)

        /**
         * Create a task for generating the javadoc jar. This will also be uploaded to Nexus.
         */
        def javadocsTask = project.tasks.getByName("javadoc")
        project.task(PARENT_JAVADOC_JAR_TASK_NAME, type: Jar) {
            classifier = 'javadoc'
            from javadocsTask.destinationDir
        }
        project.tasks.getByName(PARENT_JAVADOC_JAR_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
        project.tasks.getByName(PARENT_JAVADOC_JAR_TASK_NAME).dependsOn([classesTask, javadocsTask])

        /**
         * analyzeBuild task for sonarqube
         */
        def buildTask = project.tasks.getByName("build")
        def jacocoTaskReportTask = project.tasks.getByName("jacocoTestReport")
        def sonarqubeTask = project.tasks.getByName("sonarqube")
        project.task(PARENT_ANALYZE_TASK_NAME) {
        }
        project.tasks.getByName(PARENT_ANALYZE_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
        project.tasks.getByName(PARENT_ANALYZE_TASK_NAME).dependsOn([buildTask, jacocoTaskReportTask, sonarqubeTask])
        project.tasks.getByName(PARENT_ANALYZE_TASK_NAME).setDescription('Runs jacocoTestReport and sonarqube')

        /**
         * downloadDependencies task
         */
        project.task(PARENT_DOWNLOAD_DEPENDENCIES_TASK_NAME, type: DownloadDependenciesTask) {}
        project.tasks.getByName(PARENT_DOWNLOAD_DEPENDENCIES_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
        project.tasks.getByName(PARENT_DOWNLOAD_DEPENDENCIES_TASK_NAME).setDescription(
                'Downloads all dependencies into the build/dependencies/ folder using maven2 layout.')

        /**
         * cleanupDependencies task
         */
        project.task(PARENT_CLEANUP_DEPENDENCIES_TASK_NAME, type: DownloadDependenciesTask) {
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
        project.tasks.getByName(PARENT_CLEANUP_DEPENDENCIES_TASK_NAME).setGroup(PARENT_TASK_GROUP_NAME)
        project.tasks.getByName(PARENT_DOWNLOAD_DEPENDENCIES_TASK_NAME).
                setDescription('Remove unused dependencies from repository.')
    }
}
