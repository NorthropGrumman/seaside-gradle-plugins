package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.util.FileUtil
import org.gradle.api.Project

class SeasideCppCoverageExtension {

    final String LCOV_VERSION = "1.12"
    final String LCOV_GROUP_ARTIFACT_VERSION = "lcov:lcov:${LCOV_VERSION}"
    final String LCOV_FILENAME = "lcov-${LCOV_VERSION}.zip"

    final String LCOV_COBERTURA_VERSION = "1.6"
    final String LCOV_COBERTURA_GROUP_ARTIFACT_VERSION = "lcov-cobertura:lcov-cobertura:${LCOV_COBERTURA_VERSION}"
    final String LCOV_COBERTURA_FILENAME = "lcov-cobertura-${LCOV_COBERTURA_VERSION}.zip"

    final String CPPCHECK_VERSION = "1.81"
    final String CPPCHECK_GROUP_ARTIFACT_VERSION = "cppcheck:cppcheck:${CPPCHECK_VERSION}"
    final String CPPCHECK_FILENAME = "cppcheck-${CPPCHECK_VERSION}.zip"

    final String PYGMENTS_VERSION = "2.2.0"
    final String PYGMENTS_GROUP_ARTIFACT_VERSION = "pygments:pygments:${PYGMENTS_VERSION}"
    final String PYGMENTS_FILENAME = "pygments-${PYGMENTS_VERSION}.zip"

    final String RATS_VERSION = "2.4"
    final String RATS_GROUP_ARTIFACT_VERSION = "rats:rats:${RATS_VERSION}"
    final String RATS_FILENAME = "rats-${RATS_VERSION}.zip"

    final CppCoveragePaths CPP_COVERAGE_PATHS

    String coverageFilePath
    String coverageXmlPath
    String cppCheckXmlPath
    String ratsXmlPath
    String ratsHtmlPath

    SeasideCppCoverageExtension(Project p) {
        CPP_COVERAGE_PATHS = new CppCoveragePaths(p)
        coverageFilePath = FileUtil.toPath(p.buildDir.absolutePath, "lcov", "coverage.info")
        coverageXmlPath = FileUtil.toPath(p.buildDir.absolutePath, "lcov", "coverage.xml")
        cppCheckXmlPath = FileUtil.toPath(p.buildDir.absolutePath, "cppcheck", "cppcheck.xml")
        ratsXmlPath = CPP_COVERAGE_PATHS.PATH_TO_RATS_XML
        ratsHtmlPath = CPP_COVERAGE_PATHS.PATH_TO_RATS_HTML
    }

    private class CppCoveragePaths {

        final String PATH_TO_THE_DIRECTORY_WITH_RATS
        final String PATH_TO_THE_RATS_EXECUTABLE
        final String PATH_TO_RATS_XML
        final String PATH_TO_RATS_HTML

        CppCoveragePaths(Project p) {

            PATH_TO_THE_DIRECTORY_WITH_RATS = toPath(
                    p.buildDir.name,
                    "tmp",
                    "rats")

            PATH_TO_THE_RATS_EXECUTABLE = toPath(
                    PATH_TO_THE_DIRECTORY_WITH_RATS,
                    "rats-$RATS_VERSION",
                    "rats")

            PATH_TO_RATS_HTML = toPath(
                    p.buildDir.absolutePath,
                    "reports",
                    "rats",
                    "html",
                    "rats.html")

            PATH_TO_RATS_XML = toPath(
                    p.buildDir.absolutePath,
                    "rats",
                    "rats-report.xml")

        }

    }
    private static String toPath(String... items) {
        return items.flatten().join(File.separator)
    }
}
