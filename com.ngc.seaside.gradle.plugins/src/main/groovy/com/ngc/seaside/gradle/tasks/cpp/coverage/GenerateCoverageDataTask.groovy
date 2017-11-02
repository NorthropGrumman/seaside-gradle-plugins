package com.ngc.seaside.gradle.tasks.cpp.coverage

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.api.tasks.AbstractCoverageTask
import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.tasks.cpp.coverage.reports.GenerateCoverageXmlTask
import com.ngc.seaside.gradle.util.FileUtil
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

class GenerateCoverageDataTask extends AbstractCoverageTask {

    private SeasideCppCoverageExtension cppCoverageExtension = project.extensions
            .findByType(SeasideCppCoverageExtension.class)

    private final String LCOV_DIRECTORY_PATH = FileUtil.toPath(project.buildDir.absolutePath, "tmp", "lcov")

    private final String LCOV_EXECUTABLE_PATH = FileUtil.toPath(LCOV_DIRECTORY_PATH,
                                                                "lcov-$cppCoverageExtension.LCOV_VERSION", "bin",
                                                                "lcov")
    private final String GENHTML_EXECUTABLE_PATH = FileUtil.toPath(LCOV_DIRECTORY_PATH,
                                                                   "lcov-$cppCoverageExtension.LCOV_VERSION",
                                                                   "bin", "genhtml")
    private final String COVERAGE_HTML_DIR = FileUtil.toPath(project.buildDir.absolutePath, "reports", "lcov", "html")

    /**
     * Generate and filter coverage data then store in the specified directory
     */
    @TaskAction
    def generateCoverageData() {
        extractLcovTool()

        File coverageFile = createCoverageFile()
        if (doGenerateFilteredCoverageData(coverageFile)) {
            generateHtmlReport(coverageFile)
            GenerateCoverageXmlTask xmlTask = new GenerateCoverageXmlTask()
            xmlTask.generateLcovXml(project)
        }
    }


    /**
     * Generates lcov coverage (trace) files assuming the project contains cpp files.
     * If a coverage was generated, it will proceed to call the {@code filterCoverageData} method
     * @param coverageFile coverage file generated from gcov
     * @return true if it generates filtered coverage with no errors, false if otherwise
     */
    private boolean doGenerateFilteredCoverageData(File coverageFile) {
        String dir = project.projectDir.absolutePath
        String lcov = LCOV_EXECUTABLE_PATH

        def arguments = [
                "--no-external",
                "--base-directory", dir,
                "--directory", dir,
                "--rc", "lcov_branch_coverage=1",
                "-c", "-o", coverageFile
        ]

        project.exec { process ->
            process.executable(lcov)
            process.args(arguments)
        }.assertNormalExitValue()

        if (!deleteCoverageFileIfEmpty(coverageFile)) {
            return filterCoverageData(coverageFile)
        }

        return false
    }

    /**
     * Filters out libraries not directly associated with the current project. (e.g. file in /usr/include/c++/6.3.1/*)
     * @param lcov string of the lcov executable path
     * @param coverageFile coverage file generated from gcov
     * @return true if it filters coverage with no errors, false if otherwise
     */
    private boolean filterCoverageData(File coverageFile) {
        Preconditions.checkState(coverageFile.exists(), "$coverageFile.absolutePath does not exist!")

        String lcov = LCOV_EXECUTABLE_PATH

        def arguments = [
                "-r", coverageFile,
                "$project.projectDir.name/$project.buildDir.name/*",
                "--rc", "lcov_branch_coverage=1",
                "-o", coverageFile
        ]

        return (project.exec { process ->
            process.executable(lcov)
            process.args(arguments)
        }.exitValue == 0)

    }

    /**
     * Generates Lcov HTML report from the coverage file
     * @param coverageFile coverage file generated from gcov
     */
    private void generateHtmlReport(File coverageFile) {
        Preconditions.checkState(coverageFile.exists(), "$coverageFile.absolutePath does not exist!")
        File coverageHtmlDir = new File(COVERAGE_HTML_DIR)

        String genHtml = GENHTML_EXECUTABLE_PATH

        def arguments = [
                "-o", coverageHtmlDir.absolutePath,
                "-t", "${project.projectDir.name}",
                "--demangle-cpp",
                "--branch-coverage",
                "--function-coverage",
                "--legend",
                "--num-spaces", "4",
                coverageFile
        ]

        project.exec { process ->
            process.executable(genHtml)
            process.args(arguments)
        }.assertNormalExitValue()
    }


    /**
     * Creates coverage file directories
     * @return File handler to coverage file
     */
    private File createCoverageFile() {
        def f = new File(cppCoverageExtension.coverageFilePath)
        f.parentFile.mkdirs()
        return f
    }

    /**
     * Delete empty coverage files to prevent them from being processed by the {@code filterCoverageData} method
     * @@param coverageFile coverage file generated from gcov
     * @return true if coverage file is empty or false if otherwise
     */
    private static boolean deleteCoverageFileIfEmpty(File coverageFile) {
        if (coverageFile.text.trim().empty) {
            coverageFile.delete()
            return true
        }
        return false
    }

    /**
     * Extracts the downloaded lcov tool zip to a location where it will be ran from.
     */
    private void extractLcovTool() {
        FileTree lcovFiles = FileUtil.extractZipfile(project, pathToTheLcovReleaseArchive())
        FileUtil.copyFileTreeToDest(project, lcovFiles, LCOV_DIRECTORY_PATH)
    }

    /**
     * Finds the lcov archive location within the project classpath
     * @return path to archive file
     */
    private String pathToTheLcovReleaseArchive() {
        return findTheReleaseArchiveFile(cppCoverageExtension.LCOV_FILENAME)
    }
}
