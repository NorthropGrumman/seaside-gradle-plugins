package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class GenerateCppCheckReportsTask extends DefaultTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                    .findByType(SeasideCppCoverageExtension.class)

    @TaskAction
    def generateCppCheckReports() {
        def dir = [project.projectDir.absolutePath, "src", "main", "cpp"].join(File.separator)
        def includesDir = [project.projectDir.absolutePath, "src", "main", "include"].join(File.separator)
        def cppcheck = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_CPPCHECK_EXECUTABLE
        def cppcheckHtml = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_CPPCHECK_HTML_EXECUTABLE
        def cppcheckXmlFile = createCppCheckXmlFile()
        def cppcheckHtmlDir = createCppCheckHtmlFile()

        if (new File(dir).exists()) {
            def arguments = [
                    "--enable=all",
                    "--force", "--suppress=missingInclude",
                    "--xml", "--xml-version=2",
                    "-I", includesDir, dir,
                    "--output-file=$cppcheckXmlFile"
            ]

            project.exec {
                executable cppcheck
                args arguments
            }

            arguments = [
                "--source-encoding=\"iso8859-1\"",
                "--title=\"$project.projectDir.name\"",
                "--report-dir=$cppcheckHtmlDir",
                "--file=$cppcheckXmlFile"
            ]

            project.exec {
                executable cppcheckHtml
                args arguments
            }

        }
    }

    private File createCppCheckXmlFile() {
        def f = new File(cppCoverageExtension.cppCheckXmlPath)
        f.parentFile.mkdirs()
        return f
    }

    private File createCppCheckHtmlFile() {
        def f = new File(cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_CPPCHECK_HTML_DIR)
        f.parentFile.mkdirs()
        return f
    }
}
