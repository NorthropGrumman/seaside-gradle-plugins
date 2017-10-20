package com.ngc.seaside.gradle.extensions.cpp.coverage

import org.gradle.api.Project

class SeasideCppCoverageExtension {

    final String LCOV_VERSION = "1.13"
    final String LCOV_GAV = "lcov:lcov:${LCOV_VERSION}"
    final String LCOV_FILENAME = "lcov-${LCOV_VERSION}.zip"

    final String LCOV_COBERTURA_VERSION = "1.6"
    final String LCOV_COBERTURA_GAV = "lcov-cobertura:lcov-cobertura:${LCOV_COBERTURA_VERSION}"
    final String LCOV_COBERTURA_FILENAME = "lcov-cobertura-${LCOV_COBERTURA_VERSION}.zip"

    final CppCoveragePaths CPP_COVERAGE_PATHS

    String coverageFilePath
    String coverageXmlPath

    SeasideCppCoverageExtension(Project p) {
        CPP_COVERAGE_PATHS = new CppCoveragePaths(p, LCOV_VERSION)
        coverageFilePath = CPP_COVERAGE_PATHS.PATH_TO_THE_COVERAGE_FILE
        coverageXmlPath = CPP_COVERAGE_PATHS.PATH_TO_LCOV_COBERTURA_XML
    }

    private class CppCoveragePaths {

        final String PATH_TO_THE_DIRECTORY_WITH_LCOV
        final String PATH_TO_THE_LCOV_EXECUTABLE
        final String PATH_TO_THE_COVERAGE_FILE
        final String PATH_TO_LCOV_COBERTURA_XML
        final String PATH_TO_THE_GENHTML_EXECUTABLE
        final String PATH_TO_THE_COVERAGE_HTML_DIR

        CppCoveragePaths(Project p, String lcovVersion) {
            PATH_TO_THE_DIRECTORY_WITH_LCOV = toPath(
                    p.buildDir.absolutePath,
                    "tmp",
                    "lcov")

            PATH_TO_THE_LCOV_EXECUTABLE = toPath(
                    PATH_TO_THE_DIRECTORY_WITH_LCOV,
                    "lcov-$lcovVersion",
                    "bin",
                    "lcov")

            PATH_TO_THE_GENHTML_EXECUTABLE = toPath(
                    PATH_TO_THE_DIRECTORY_WITH_LCOV,
                    "lcov-$lcovVersion",
                    "bin",
                    "genhtml")

            PATH_TO_THE_COVERAGE_FILE = toPath(
                    p.buildDir.absolutePath,
                    "lcov",
                    "coverage.info")

            PATH_TO_LCOV_COBERTURA_XML = toPath(
                    p.buildDir.absolutePath,
                    "lcov",
                    "coverage.xml")

            PATH_TO_THE_COVERAGE_HTML_DIR = toPath(
                    p.buildDir.absolutePath,
                    "reports",
                    "lcov",
                    "html",
                    p.projectDir.name)
        }

        private static String toPath(String... items) {
            return items.flatten().join(File.separator)
        }
    }
}
