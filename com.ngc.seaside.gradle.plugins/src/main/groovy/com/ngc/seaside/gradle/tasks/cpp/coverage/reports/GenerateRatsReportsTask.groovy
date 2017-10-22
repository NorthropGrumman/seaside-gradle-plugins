package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateRatsReportsTask extends DefaultTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                    .findByType(SeasideCppCoverageExtension.class)

    @TaskAction
    def generateRatsReports() {
        def dir = [project.projectDir.absolutePath, "src", "main", "cpp"].join(File.separator)
        def rats = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_RATS_EXECUTABLE
        def ratsDatabase = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_RATS_DATABASE
        def ratsXmlFile = createRatsXmlFile()
        def ratsHtmlFile = createRatsHtmlFile()
        String ratsCommand = "$rats --database $ratsDatabase $dir"

        if (new File(dir).exists()) {
            def arguments = [
                    "-c", ratsCommand + " --xml > $ratsXmlFile"
            ]

            project.exec {
                executable "bash"
                args arguments
            }

            arguments = [
                "-c", ratsCommand + " --html > $ratsHtmlFile"
            ]

            project.exec {
                executable "bash"
                args arguments
            }
        }
    }

    private File createRatsXmlFile() {
        def f = new File(cppCoverageExtension.ratsXmlPath)
        f.parentFile.mkdirs()
        return f
    }

    private File createRatsHtmlFile() {
        def f = new File(cppCoverageExtension.ratsHtmlPath)
        f.parentFile.mkdirs()
        return f
    }
}
