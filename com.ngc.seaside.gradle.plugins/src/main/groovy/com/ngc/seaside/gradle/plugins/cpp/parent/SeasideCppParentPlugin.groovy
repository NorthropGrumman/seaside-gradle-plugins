package com.ngc.seaside.gradle.plugins.cpp.parent

import com.ngc.seaside.gradle.tasks.cpp.dependencies.BuildingExtension
import com.ngc.seaside.gradle.tasks.cpp.dependencies.SharedBuildConfiguration
import com.ngc.seaside.gradle.tasks.cpp.dependencies.StaticBuildConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.resolve.ProjectModelResolver
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.gradle.language.cpp.tasks.CppCompile
import org.gradle.nativeplatform.NativeLibrarySpec
import org.gradle.nativeplatform.PrebuiltLibraries
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.VisualCpp
import org.gradle.platform.base.BinaryContainer

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 */
class SeasideCppParentPlugin implements Plugin<Project> {


    @Override
    void apply(Project p) {

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

        task('createDistributionZip', type: Zip, dependsOn: [copyExportedHeaders, copySharedLib, copyStaticLib]) {
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
                               // filterLinkerArgs(p.extensions.linking, args)
                            }
                        }
                    }
//                        cygwin(Gcc) {
//                            path new File("C:/cygwin/bin")
//                            target("cygwin")
//                        }
                    gcc(Gcc) {
                        eachPlatform {
                            linker.withArguments { args ->
                               // filterLinkerArgs(p.extensions.linking, args)
                            }
                        }
                    }
//                        clang(Clang)
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

            tasks.getByName('createDistributionZip').archiveName = "${project.group}.${project.name}-${project.version}.zip"

            tasks.getByName('copySharedLib').onlyIf { file("${project.buildDir}/libs/main/shared").isDirectory() }
            tasks.getByName('copyStaticLib').onlyIf { file("${project.buildDir}/libs/main/static").isDirectory() }

            tasks.getByName('unpackCompileDependencies').dependenciesDirectory = file("${project.buildDir}/dependencies")
            tasks.getByName('unpackTestCompileDependencies').dependenciesDirectory = file("${project.buildDir}/testDependencies")
            tasks.withType(CppCompile, { task -> task.dependsOn([unpackCompileDependencies, unpackTestCompileDependencies]) })

            def binaries = p.getServices()
                    .get(ProjectModelResolver)
                    .resolveProjectModel(p.path)
                    .find('binaries', BinaryContainer)
                    .findAll { b -> b.buildable }
            tasks.getByName('copySharedLib').dependsOn(binaries)
            tasks.getByName('copyStaticLib').dependsOn(binaries)
        }



        p.task('displayBuilding') << {
            println "\tHeaders: $project.building.headers"


            Collection<String> configuredStaticDeps = project.building.storage.getStaticDependencies()
            println "\tStatic $configuredStaticDeps"
            for(String dep : configuredStaticDeps) {
                Collection< StaticBuildConfiguration> configurations = project.building.storage.getStaticBuildConfigurations(dep)
                for(StaticBuildConfiguration config : configurations) {
                    println "\t  $config"
                }
            }

            Collection<String> configuredSharedDeps = project.building.storage.getSharedDependencies()
            println "\tShared $configuredSharedDeps"
            for(String dep : configuredSharedDeps) {
                Collection<SharedBuildConfiguration> configurations = project.building.storage.getSharedBuildConfigurations(dep)
                for(SharedBuildConfiguration config : configurations) {
                    println "\t $config"
                }
            }
        }
    }

//    private void filterLinkerArgs(BuildingExtension buildingExtension, List<String> linkerArgs) {
//        Collection<String> artifactsWithArgs = new HashSet<>()
//
//        artifactsWithArgs.addAll(linkingConfig.getPrefixedLinkingArgs().keySet())
//        artifactsWithArgs.addAll(linkingConfig.getSuffixedLinkingArgs().keySet())
//
//        for (String artifact : artifactsWithArgs) {
//            boolean foundArg = false
//            Pattern p = Pattern.compile(artifact + '\\.(so|a|dll|lib)$')
//            for (int i = 0; i < linkerArgs.size() && !foundArg; i++) {
//                String arg = linkerArgs.get(i)
//                if (p.matcher(arg)) {
//                    foundArg = true
//
//                    List<String> preArgs = linkingConfig.getPrefixedLinkingArgs().getOrDefault(
//                            artifact,
//                            Collections.emptyList())
//                    List<String> postArgs = linkingConfig.getSuffixedLinkingArgs().getOrDefault(
//                            artifact,
//                            Collections.emptyList())
//
//                    linkerArgs.addAll(i, preArgs)
//                    i += preArgs.size() + 1
//                    linkerArgs.addAll(i, postArgs)
//                }
//            }
//        }
//
//        System.out.println("Filtered args = " + linkerArgs)
//    }
}
