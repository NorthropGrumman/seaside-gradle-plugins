package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Paths

class GenerateCppCheckXmlTask extends DefaultTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                    .findByType(SeasideCppCoverageExtension.class)

    @TaskAction
    def generateCppCheckXml() {
        def dir = [project.projectDir.absolutePath, "src", "main", "cpp"].join(File.separator)
        def includesDir = [project.projectDir.absolutePath, "src", "main", "include"].join(File.separator)
        def cppcheck = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_CPPCHECK_EXECUTABLE
        def cppcheckFile = createCppCheckXmlFile()

        if (new File(dir).exists()) {
            def arguments = [
                    "--enable=all",
                    "--inconclusive",
                    "--force",
                    "--library=windows,posix,gnu",
                    "--xml-version=2", "-I", includesDir, dir,
                    "--output-file=$cppcheckFile"
            ]

            project.exec {
                executable cppcheck
                args arguments
            }
        }
    }

    private File createCppCheckXmlFile() {
        def f = new File(cppCoverageExtension.cppCheckXmlPath)
        f.parentFile.mkdirs()
        return f
    }
}
