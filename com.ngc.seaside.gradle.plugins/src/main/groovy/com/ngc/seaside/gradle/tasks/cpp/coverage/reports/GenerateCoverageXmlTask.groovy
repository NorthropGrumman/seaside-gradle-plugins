package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class GenerateCoverageXmlTask extends DefaultTask {
    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                   .findByType(SeasideCppCoverageExtension.class)

    @TaskAction
    def generateLcovXml() {
        def lcovCobertura = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_LCOV_COBERTURA_SCRIPT
        def coverageFilePath = cppCoverageExtension.coverageFilePath
        def coverageXmlPath = createCoverageXmlFile()
        def arguments = [
              lcovCobertura,
              coverageFilePath,
              "--demangle",
              "--output",
              coverageXmlPath
        ]

        if (new File(coverageFilePath).exists()) {
            project.exec {
                executable "python"
                args arguments
            }
        }
    }

    private File createCoverageXmlFile() {
        def f = new File(cppCoverageExtension.coverageXmlPath)
        f.parentFile.mkdirs()
        return f
    }
}
