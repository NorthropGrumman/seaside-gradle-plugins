package com.ngc.seaside.gradle.tasks.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

class ExtractLcovTask extends DefaultTask {
    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                   .findByType(SeasideCppCoverageExtension.class)

    @TaskAction
    def extractLcov() {
        def lcovFiles = extractTheReleaseArchive(pathToTheLcovReleaseArchive())
        def lcovCoberturaFiles = extractTheReleaseArchive(pathToTheLcovCoberturaReleaseArchive())

        copyTheFiles(lcovFiles, pathToTheDirectoryWithLcovFiles())
        copyTheFiles(lcovCoberturaFiles, pathToTheDirectoryWithLcovCoberturaFiles())
    }

    private FileTree extractTheReleaseArchive(String releaseArchive) {
        return project.zipTree(releaseArchive)
    }

    private String pathToTheLcovReleaseArchive() {
        return Paths.get(findTheReleaseArchiveFile(cppCoverageExtension.LCOV_FILENAME))
    }

    private String pathToTheLcovCoberturaReleaseArchive() {
        return Paths.get(findTheReleaseArchiveFile(cppCoverageExtension.LCOV_COBERTURA_FILENAME))
    }

    private String findTheReleaseArchiveFile(String filename) {
        return projectClasspathConfiguration().filter { file ->
            return file.name.contains(filename)
        }.getAsPath()
    }

    private Configuration projectClasspathConfiguration() {
        return project
                .configurations
                .getByName("compile")
    }

    def copyTheFiles(FileTree source, String destination) {
        project.copy {
            from source
            into destination
        }
    }

    private String pathToTheDirectoryWithLcovFiles() {
        return cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_DIRECTORY_WITH_LCOV
    }

    private String pathToTheDirectoryWithLcovCoberturaFiles() {
        return cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_DIRECTORY_WITH_LCOV_COBERTURA
    }
}
