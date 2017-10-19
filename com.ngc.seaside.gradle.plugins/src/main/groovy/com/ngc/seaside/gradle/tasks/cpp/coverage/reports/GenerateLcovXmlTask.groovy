package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

class GenerateLcovXmlTask extends DefaultTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                    .findByType(SeasideCppCoverageExtension.class)

    @TaskAction
    def generateLcovXml() {

        def lcovCoberturaFiles = extractTheLcovCoberturaReleaseArchive()
        def outputDir = pathToTheDirectoryWithLcovCoberturaFiles()

        project.copy {
            from lcovCoberturaFiles
            into outputDir
        }

        def lcovCoberturaPath = [project.buildDir.absolutePath, "tmp", "lcov-cobertura"].join(File.separator)
        def coverageFilePath = cppCoverageExtension.coverageFilePath
        def coverageXmlPath = cppCoverageExtension.coverageXmlPath
        def commandLineString = ["python", "${lcovCoberturaPath}/lcov-to-cobertura-xml-1.6/lcov_cobertura.py", coverageFilePath, "--demangle", "--output", coverageXmlPath]
        def commandOutput = new ByteArrayOutputStream()

        project.exec {
            commandLine commandLineString
            standardOutput commandOutput
        }

        println commandOutput.toString()

    }

    private FileTree extractTheLcovCoberturaReleaseArchive() {
        return project.zipTree(pathToTheLcovCoberturaReleaseArchive())
    }

    private String pathToTheLcovCoberturaReleaseArchive() {
        return Paths.get(lcovCoberturaReleaseArchiveFile())
    }

    private String lcovCoberturaReleaseArchiveFile() {
        return projectClasspathConfiguration().filter { file ->
            return file.getName().contains("lcov-cobertura")
        }.getAsPath()
    }

    private Configuration projectClasspathConfiguration() {
        return project
                .rootProject
                .buildscript
                .configurations
                .getByName("classpath")
    }

    private String pathToTheDirectoryWithLcovCoberturaFiles() {
        return [project.buildDir.name, "tmp", "lcov-cobertura"].join(File.separator)
    }

}