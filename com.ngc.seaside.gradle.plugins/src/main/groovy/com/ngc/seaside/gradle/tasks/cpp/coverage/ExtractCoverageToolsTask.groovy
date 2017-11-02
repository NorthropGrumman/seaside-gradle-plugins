package com.ngc.seaside.gradle.tasks.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

class ExtractCoverageToolsTask extends DefaultTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                    .findByType(SeasideCppCoverageExtension.class)

    @TaskAction
    def extractLcov() {
        def ratsFiles = extractTheReleaseArchive(pathToTheRatsReleaseArchive())


        copyTheFiles(ratsFiles, pathToTheDirectoryWithRatsFiles())
    }

    private FileTree extractTheReleaseArchive(String releaseArchive) {
        return project.zipTree(releaseArchive)
    }

    private String pathToTheRatsReleaseArchive() {
        return Paths.get(findTheReleaseArchiveFile(cppCoverageExtension.RATS_FILENAME))
    }



    private String findTheReleaseArchiveFile(String filename) {
        return projectClasspathConfiguration().filter { file ->
            return file.name.endsWith(filename)
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

    private String pathToTheDirectoryWithRatsFiles() {
        return cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_DIRECTORY_WITH_RATS
    }
}
