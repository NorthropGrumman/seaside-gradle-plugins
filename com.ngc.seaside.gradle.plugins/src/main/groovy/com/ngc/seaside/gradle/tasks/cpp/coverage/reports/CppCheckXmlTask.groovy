package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Paths

class CppCheckXmlTask extends DefaultTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                    .findByType(SeasideCppCoverageExtension.class)

    final String CPPCHECK_FILENAME = cppCoverageExtension.CPPCHECK_FILENAME

    @TaskAction
    def generateLcovXml() {

        def lcovCoberturaFiles = extractTheLcovCoberturaReleaseArchive()
        def outputDir = pathToTheDirectoryWithLcovCoberturaFiles()

        project.copy {
            from lcovCoberturaFiles
            into outputDir
        }

//        def lcovCoberturaPath = [project.buildDir.absolutePath, "tmp", "c",
// "lcov-to-cobertura-xml-${cppCoverageExtension.LCOV_COBERTURA_VERSION}", "lcov_cobertura"].join(File.separator)
//        def coverageFilePath = cppCoverageExtension.coverageFilePath
//        def coverageXmlPath = cppCoverageExtension.coverageXmlPath
//        def commandLineString = ["python", "${lcovCoberturaPath}/lcov_cobertura.py", coverageFilePath, "--demangle", "--output", coverageXmlPath]
//        def commandOutput = new ByteArrayOutputStream()
//
//        Files.createDirectories(Paths.get(cppCoverageExtension.coverageXmlPath).getParent())
//
//        project.exec {
//            commandLine commandLineString
//            standardOutput commandOutput
//        }
//
//        println commandOutput.toString()

    }

//    private FileTree extractTheLcovCoberturaReleaseArchive() {
//        return project.zipTree(pathToTheLcovCoberturaReleaseArchive())
//    }
//
//    private String pathToTheLcovCoberturaReleaseArchive() {
//        return Paths.get(lcovCoberturaReleaseArchiveFile())
//    }
//
//    private String lcovCoberturaReleaseArchiveFile() {
//        return projectClasspathConfiguration().filter { file ->
//            return file.getName().contains(LCOV_COBERTURA_FILENAME)
//        }.getAsPath()
//    }
//
//    private Configuration projectClasspathConfiguration() {
//        return project
//                .configurations
//                .getByName("compile")
//    }
//
//    private String pathToTheDirectoryWithLcovCoberturaFiles() {
//        return [project.buildDir.name, "tmp", "lcov-cobertura"].join(File.separator)
//    }
}
