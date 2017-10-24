package com.ngc.seaside.gradle.plugins.cpp.parent

import com.ngc.seaside.gradle.plugins.cpp.coverage.SeasideCppCoveragePlugin
import com.ngc.seaside.gradle.plugins.parent.SeasideParentPlugin
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.plugins.util.TaskResolver
import com.ngc.seaside.gradle.tasks.cpp.dependencies.BuildingExtension
import com.ngc.seaside.gradle.tasks.cpp.dependencies.StaticBuildConfiguration
import com.ngc.seaside.gradle.tasks.cpp.dependencies.UnpackCppDistributionsTask
import com.ngc.seaside.gradle.tasks.dependencies.DownloadDependenciesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.resolve.ProjectModelResolver
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.language.cpp.tasks.CppCompile
import org.gradle.nativeplatform.NativeLibrarySpec
import org.gradle.nativeplatform.PrebuiltLibraries
import org.gradle.nativeplatform.test.tasks.RunTestExecutable
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.VisualCpp
import org.gradle.platform.base.BinaryContainer

import java.util.regex.Matcher

/**
 * The Seaside wrapper for the Native plugin in Gradle.
 * This sets common attributes such as google-test-test-suite and provides a mechanism for unpacking
 * dependencies.
 *
 * The dependencies must have the following structure within a zip file.
 * <pre>
 * ${artifactId}-${version}.zip
 *   - include
 *      - ${artifactId}
 *        - *.h
 *   - lib
 *      - ${os}_${arch}
 *        - *.so / *.dll
 *        - *.a / *.lib
 *  </pre>
 *  <br>Example: <br>
 *  <pre>
 *   celix-2.0.0.zip
 *     include
 *       celix
 *         *.h
 *     lib
 *       linux_x86_64
 *         *.a
 *         *.so
 *
 *  </pre>
 *
 *  The configuration {@link BuildingExtension} allows for different library configurations.
 *  Dependencies that have the artifactId and the library as the same name don't have to specify the
 *  libs option for the shared or statically configurations.
 */
class SeasideCppParentPlugin implements Plugin<Project> {

    public static final String ANALYZE_TASK_NAME = 'analyze'
    public static final String DOWNLOAD_DEPENDENCIES_TASK_NAME = 'downloadDependencies'
    public static final String CLEANUP_DEPENDENCIES_TASK_NAME = 'cleanupDependencies'
    TaskResolver resolver

    @Override
    void apply(Project project) {
        project.configure(project) {
            resolver  = new TaskResolver(project)
            project.extensions.create("building", BuildingExtension, project)

            project.configurations {
                compile
                testCompile
                distribution
            }

            applyPlugins(project)
            createTasks(project)

            afterEvaluate {

                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
                    }
                }

                ext {
                    // The default name of the bundle.
                    bundleName = "$group" + '.' + "$project.name"
                }

                model {
                    repositories {
                        libs(PrebuiltLibraries) {
                        }
                    }

                    platforms {
                        windows_x86_64 {
                            operatingSystem 'windows'
                            architecture 'x64'
                        }
                        cygwin_x86_64 {
                            operatingSystem "windows"
                            architecture "x64"
                        }
                        linux_x86_64 {
                            operatingSystem 'linux'
                            architecture 'x64'
                        }
                    }

                    toolChains {
                        visualCpp(VisualCpp) {
                            eachPlatform {
                                linker.withArguments { args ->
                                    filterLinkerArgs(project.extensions.building, args)
                                }
                            }
                        }
                        gcc(Gcc) {
                            eachPlatform {
                                linker.withArguments { args ->
                                    filterLinkerArgs(project.extensions.building, args)
                                }
                            }
                        }
                    }

                    components {
                        main(NativeLibrarySpec) {
                            baseName = "${project.name}"
                            sources {
                                cpp {
                                    source {
                                        srcDirs 'src/main/cpp'
                                    }
                                    exportedHeaders {
                                        srcDirs 'src/main/include'
                                    }
                                }
                            }
                        }

                        all {
                            ["windows_x86_64", "cygwin_x86_64", "linux_x86_64"].each {
                                targetPlatform it
                            }
                        }
                    }
                }

                artifacts {
                    distribution createDistributionZip
                }

                sonarqube {
                    properties {
                        if (new File("${project.buildDir.absolutePath}/lcov/coverage.xml").exists()) {
                            property 'sonar.cxx.coverage.reportPath',"${project.buildDir.absolutePath}/lcov/coverage.xml"
                        }

                        if (new File("${project.buildDir.absolutePath}/test-results/mainTest/linux_x86_64/report.xml").exists()) {
                            property 'sonar.cxx.xunit.reportPath',
                                     "${project.buildDir.absolutePath}/test-results/mainTest/linux_x86_64/report.xml"
                        }

                        if (new File("${project.buildDir.absolutePath}/cppcheck/cppcheck.xml").exists()) {
                            property 'sonar.cxx.cppcheck.reportPath',
                                     "${project.buildDir.absolutePath}/cppcheck/cppcheck.xml"
                        }

                        if (new File("${project.buildDir.absolutePath}/rats/rats-report.xml").exists()) {
                            property 'sonar.cxx.rats.reportPath',
                                     "${project.buildDir.absolutePath}/rats/rats-report.xml"
                        }

                        property 'sonar.branch', SeasideParentPlugin.getBranchName()

                        if (new File("${project.projectDir}/src/main/cpp").exists()) {
                            property 'sonar.sources', "${project.projectDir}/src/main/cpp"
                        }

                        if (new File("${project.projectDir}/src/test/cpp").exists()) {
                            property 'sonar.tests', "${project.projectDir}/src/test/cpp"
                        }

                        if (new File("${project.projectDir}/src/main/include").exists()) {
                            property 'sonar.cxx.includeDirectories', "${project.projectDir}/src/main/include"
                        }
                    }
                }

                project.tasks.withType(RunTestExecutable) {
                    args "--gtest_output=xml:report.xml"
                }

                resolver.findTask('createDistributionZip')
                        .archiveName = "${project.name}-${project.version}.zip"

                resolver.findTask('copySharedLib').onlyIf {
                    file("${project.buildDir}/libs/main/shared").isDirectory()
                }

                resolver.findTask('copyStaticLib').onlyIf {
                    file("${project.buildDir}/libs/main/static").isDirectory()
                }

                resolver.findTask('unpackCompileDependencies').dependenciesDirectory =
                        file("${project.buildDir}/dependencies")
                resolver.findTask('unpackTestCompileDependencies').dependenciesDirectory =
                        file("${project.buildDir}/testDependencies")
                project.tasks.withType(CppCompile, { task ->
                    task.dependsOn([unpackCompileDependencies, unpackTestCompileDependencies])
                })

                def binaries = project.getServices()
                        .get(ProjectModelResolver)
                        .resolveProjectModel(project.path)
                        .find('binaries', BinaryContainer)
                        .findAll { b -> b.buildable }
                resolver.findTask('copySharedLib').dependsOn(binaries)
                resolver.findTask('copyStaticLib').dependsOn(binaries)
            }
        }
    }


    /**
     * This method will search the already defined linker arguments and wrap any static libraries that have
     * been specified in the 'building' configuration with the 'withArgs' option
     *
     * @param buildingExtension the building configuration
     * @param linkerArgs the already existing linker arguments. This method will mutate this parameter by
     *                          adding arguments.
     */
    private void filterLinkerArgs(BuildingExtension buildingExtension, List<String> linkerArgs) {
        for (String file : buildingExtension.getStorage().getFilesWithLinkerArgs()) {
            if (linkerArgs.contains(file)) {
                int index = linkerArgs.indexOf(file)
                StaticBuildConfiguration.WithArgs withArgs = buildingExtension.storage.getLinkerArgs(file)
                linkerArgs.addAll(index, withArgs.before)
                linkerArgs.addAll(index + 1 + (withArgs.before.size()), withArgs.after)
            }
        }
    }

    protected void createTasks(Project project) {

        TaskResolver resolver = new TaskResolver(project)
        /**
         * analyzeBuild task for sonarqube
         */
        def buildTask = resolver.findTask("build")
        def coverageReportTask = resolver.findTask("genFullCoverageReport")
        def sonarqubeTask = resolver.findTask("sonarqube")
        project.task(ANALYZE_TASK_NAME) {
        }
        project.tasks.getByName(ANALYZE_TASK_NAME).setGroup("analysis")
        project.tasks.getByName(ANALYZE_TASK_NAME).dependsOn([buildTask, coverageReportTask, sonarqubeTask])
        project.tasks.getByName(ANALYZE_TASK_NAME).setDescription('Runs build and sonarqube')

        project.task('copyCompileDependencies', type: Copy) {
            from project.configurations.compile
            into { "${project.buildDir}/dependencies" }
        }

        project.task('copyTestCompileDependencies', type: Copy) {
            from project.configurations.testCompile
            into { "${project.buildDir}/testDependencies" }
        }


        def copyCompileDependenciesTask = resolver.findTask("copyCompileDependencies")
        project.task('unpackCompileDependencies', type: UnpackCppDistributionsTask, dependsOn: copyCompileDependenciesTask) {
            componentName = 'main'
            componentSourceSetName = 'cpp'
        }

        def copyTestCompileDependenciesTask = resolver.findTask("copyTestCompileDependencies")
        project.task('unpackTestCompileDependencies', type: UnpackCppDistributionsTask,
            dependsOn: copyTestCompileDependenciesTask) {
            testDependencies = true
        }

        project.task('copyExportedHeaders', type: Copy) {
            from 'src/main/include'
            into { "${project.distsDir}/${project.group}.${project.name}-${project.version}/include" }
        }

        project.task('copySharedLib', type: Copy) {
            from "${project.buildDir}/libs/main/shared"
            into { "${project.distsDir}/${project.group}.${project.name}-${project.version}/lib/" }
            rename { name ->
                Matcher m = name.toString() =~ /main\.(.*)/
                if (m.matches()) {
                    return "${project.group}.${project.name}-${project.version}." + m.group(1)
                }
                return null
            }
        }

        project.task('copyStaticLib', type: Copy) {
            from "${project.buildDir}/libs/main/static"
            into { "${project.distsDir}/${project.group}.${project.name}-${project.version}/lib/" }
            rename { name ->
                Matcher m = name.toString() =~ /main\.(.*)/
                if (m.matches()) {
                    return "${project.group}.${project.name}-${project.version}." + m.group(1)
                }
                return null
            }
        }

        def copyExportedHeadersTask = resolver.findTask("copyExportedHeaders")
        def copySharedLibTask = resolver.findTask("copySharedLib")
        def copyStaticLibTask = resolver.findTask("copyStaticLib")
        project.task('createDistributionZip', type: Zip, dependsOn: [copyExportedHeadersTask, copySharedLibTask, copyStaticLibTask]) {
            from { "${project.distsDir}/${project.group}.${project.name}-${project.version}" }
        }

        /**
         * downloadDependencies task
         */
        project.task(DOWNLOAD_DEPENDENCIES_TASK_NAME, type: DownloadDependenciesTask) {}
        resolver.findTask(DOWNLOAD_DEPENDENCIES_TASK_NAME).setGroup("dependencies")
        resolver.findTask(DOWNLOAD_DEPENDENCIES_TASK_NAME).setDescription(
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
        resolver.findTask(CLEANUP_DEPENDENCIES_TASK_NAME).setGroup("dependencies")
        resolver.findTask(CLEANUP_DEPENDENCIES_TASK_NAME).setDescription('Remove unused dependencies from dependencies folder.')

    }

    /**
     * This plugin requires the java and maven plugins
     * @param project
     */
    protected void applyPlugins(Project project) {
        project.getPlugins().apply('cpp')
        project.getPlugins().apply('maven')
        project.getPlugins().apply('google-test-test-suite')
        project.getPlugins().apply('org.sonarqube')
        project.getPlugins().apply(SeasideCppCoveragePlugin)
        project.getPlugins().apply(SeasideReleasePlugin)
    }
}
