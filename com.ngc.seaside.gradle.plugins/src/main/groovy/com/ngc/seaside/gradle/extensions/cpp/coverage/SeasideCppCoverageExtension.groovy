package com.ngc.seaside.gradle.extensions.cpp.coverage

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

    final CppCoveragePaths CPP_COVERAGE_PATHS

    String coverageFilePath
    String coverageXmlPath
    String cppCheckXmlPath

    SeasideCppCoverageExtension(Project p) {
        CPP_COVERAGE_PATHS = new CppCoveragePaths(p)
        coverageFilePath = CPP_COVERAGE_PATHS.PATH_TO_THE_COVERAGE_FILE
        coverageXmlPath = CPP_COVERAGE_PATHS.PATH_TO_LCOV_COBERTURA_XML
        cppCheckXmlPath = CPP_COVERAGE_PATHS.PATH_TO_CPPCHECK_XML
    }

    private class CppCoveragePaths {
        final String PATH_TO_THE_DIRECTORY_WITH_LCOV
        final String PATH_TO_THE_LCOV_EXECUTABLE
        final String PATH_TO_THE_CPPCHECK_EXECUTABLE
        final String PATH_TO_THE_COVERAGE_FILE
        final String PATH_TO_THE_GENHTML_EXECUTABLE
        final String PATH_TO_THE_COVERAGE_HTML_DIR
        final String PATH_TO_THE_DIRECTORY_WITH_LCOV_COBERTURA
        final String PATH_TO_THE_LCOV_COBERTURA_SCRIPT
        final String PATH_TO_LCOV_COBERTURA_XML
        final String PATH_TO_THE_DIRECTORY_WITH_CPPCHECK
        final String PATH_TO_CPPCHECK_XML

        CppCoveragePaths(Project p) {
            PATH_TO_THE_DIRECTORY_WITH_LCOV = toPath(
                    p.buildDir.absolutePath,
                    "tmp",
                    "lcov")

            PATH_TO_THE_DIRECTORY_WITH_LCOV_COBERTURA = toPath(
                    p.buildDir.name,
                    "tmp",
                    "lcov-cobertura")

            PATH_TO_THE_DIRECTORY_WITH_CPPCHECK = toPath(
                    p.buildDir.name,
                    "tmp",
                    "cppcheck")

            PATH_TO_THE_LCOV_EXECUTABLE = toPath(
                    PATH_TO_THE_DIRECTORY_WITH_LCOV,
                    "lcov-$LCOV_VERSION",
                    "bin",
                    "lcov")

            PATH_TO_THE_GENHTML_EXECUTABLE = toPath(
                    PATH_TO_THE_DIRECTORY_WITH_LCOV,
                    "lcov-$LCOV_VERSION",
                    "bin",
                    "genhtml")

            PATH_TO_THE_LCOV_COBERTURA_SCRIPT = toPath(
                    PATH_TO_THE_DIRECTORY_WITH_LCOV_COBERTURA,
                    "lcov-to-cobertura-xml-${LCOV_COBERTURA_VERSION}",
                    "lcov_cobertura",
                    "lcov_cobertura.py")

            PATH_TO_THE_CPPCHECK_EXECUTABLE = toPath(
                    PATH_TO_THE_DIRECTORY_WITH_CPPCHECK,
                    "cppcheck-$CPPCHECK_VERSION",
                    "cppcheck")

            PATH_TO_THE_COVERAGE_FILE = toPath(
                    p.buildDir.absolutePath,
                    "lcov",
                    "coverage.info")

            PATH_TO_THE_COVERAGE_HTML_DIR = toPath(
                    p.buildDir.absolutePath,
                    "reports",
                    "lcov",
                    "html",
                    p.projectDir.name)

            PATH_TO_LCOV_COBERTURA_XML = toPath(
                    p.buildDir.absolutePath,
                    "lcov",
                    "coverage.xml")

            PATH_TO_CPPCHECK_XML = toPath(
                    p.buildDir.absolutePath,
                    "cppcheck",
                    "cppcheck.xml")

        }

        private static String toPath(String... items) {
            return items.flatten().join(File.separator)
        }
    }
}
