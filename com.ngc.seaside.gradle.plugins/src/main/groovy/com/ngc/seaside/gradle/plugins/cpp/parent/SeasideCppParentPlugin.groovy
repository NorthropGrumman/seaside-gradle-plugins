package com.ngc.seaside.gradle.plugins.cpp.parent

import com.ngc.seaside.gradle.plugins.util.GradleUtil
import com.ngc.seaside.gradle.tasks.cpp.dependencies.BuildingExtension
import com.ngc.seaside.gradle.tasks.cpp.dependencies.StaticBuildConfiguration
import com.ngc.seaside.gradle.tasks.cpp.dependencies.UnpackCppDistributionsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.resolve.ProjectModelResolver
import org.gradle.api.tasks.Copy
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

    public final static String CREATE_DISTRIBUTION_ZIP_TASK_NAME = "createDistributionZip"

    @Override
    void apply(Project p) {
        p.configure(p) {
            // Make sure that all required properties are set.
            doRequiredGradleProperties(project,
                                       'nexusConsolidated',
                                       'nexusReleases',
                                       'nexusSnapshots',
                                       'nexusUsername',
                                       'nexusPassword')

            plugins.apply 'cpp'
            plugins.apply 'maven'
            plugins.apply 'google-test-test-suite'

            project.extensions.create("building", BuildingExtension, p)

            configurations {
                compile
                testCompile
                distribution
            }

            task('copyCompileDependencies', type: Copy) {
                from configurations.compile
                into { "${project.buildDir}/dependencies" }
            }

            task('copyTestCompileDependencies', type: Copy) {
                from configurations.testCompile
                into { "${project.buildDir}/testDependencies" }
            }

            task('unpackCompileDependencies', type: UnpackCppDistributionsTask, dependsOn: copyCompileDependencies) {
                componentName = 'main'
                componentSourceSetName = 'cpp'
            }

            task('unpackTestCompileDependencies', type: UnpackCppDistributionsTask,
                 dependsOn: copyTestCompileDependencies) {
                testDependencies = true
            }

            task('copyExportedHeaders', type: Copy) {
                from 'src/main/include'
                into { "${project.distsDir}/${project.group}.${project.name}-${project.version}/include" }
            }

            task('copySharedLib', type: Copy) {
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

            task('copyStaticLib', type: Copy) {
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

            task(CREATE_DISTRIBUTION_ZIP_TASK_NAME, type: Zip, dependsOn: [copyExportedHeaders, copySharedLib, copyStaticLib]) {
                from { "${project.distsDir}/${project.group}.${project.name}-${project.version}" }
            }

            afterEvaluate {

                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
                    }
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
                                    filterLinkerArgs(p.extensions.building, args)
                                }
                            }
                        }
                        gcc(Gcc) {
                            eachPlatform {
                                linker.withArguments { args ->
                                    filterLinkerArgs(p.extensions.building, args)
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
                    distribution p[CREATE_DISTRIBUTION_ZIP_TASK_NAME]
                }

                tasks.withType(RunTestExecutable) {
                    args "--gtest_output=xml:report.xml"
                }

                tasks.getByName(CREATE_DISTRIBUTION_ZIP_TASK_NAME)
                      .archiveName = "${project.name}-${project.version}.zip"

                tasks.getByName('copySharedLib').onlyIf { file("${project.buildDir}/libs/main/shared").isDirectory() }
                tasks.getByName('copyStaticLib').onlyIf { file("${project.buildDir}/libs/main/static").isDirectory() }

                tasks.getByName('unpackCompileDependencies').dependenciesDirectory =
                        file("${project.buildDir}/dependencies")
                tasks.getByName('unpackTestCompileDependencies').dependenciesDirectory =
                        file("${project.buildDir}/testDependencies")
                tasks.withType(CppCompile, { task ->
                    task.dependsOn([unpackCompileDependencies, unpackTestCompileDependencies])
                })

                def binaries = p.getServices()
                        .get(ProjectModelResolver)
                        .resolveProjectModel(p.path)
                        .find('binaries', BinaryContainer)
                        .findAll { b -> b.buildable }
                tasks.getByName('copySharedLib').dependsOn(binaries)
                tasks.getByName('copyStaticLib').dependsOn(binaries)
            }
        }
    }

    protected void doRequiredGradleProperties(Project project, String propertyName, String... propertyNames) {
        GradleUtil.requireProperties(project.properties, propertyName, propertyNames)
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
}
