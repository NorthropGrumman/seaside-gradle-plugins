/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.api.AbstractCoverageTask
import com.ngc.seaside.gradle.plugins.cpp.coverage.reports.GenerateCoverageXmlTask
import com.ngc.seaside.gradle.util.FileUtil

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

class GenerateCoverageDataTask extends AbstractCoverageTask {

    private SeasideCppCoverageExtension cppCoverageExtension = project.extensions
            .findByType(SeasideCppCoverageExtension.class)

    private final String lcovDirectoryPath = FileUtil.toPath(project.buildDir.absolutePath, "tmp", "lcov")
    private final String lcovFolder = FileUtil.toPath(lcovDirectoryPath, "lcov-$cppCoverageExtension.LCOV_VERSION")
    private final String lcovExecutablePath = FileUtil.toPath(lcovFolder, "bin", "lcov")
    private final String genHtmlExecutablePath = FileUtil.toPath(lcovFolder, "bin", "genhtml")
    private final String coverageHtmlDir = FileUtil.toPath(project.buildDir.absolutePath, "reports", "lcov", "html")

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
        String lcov = lcovExecutablePath

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

        String lcov = lcovExecutablePath

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
        File coverageHtmlDir = new File(coverageHtmlDir)

        String genHtml = genHtmlExecutablePath

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
        String archivePath = findTheReleaseArchiveFile(cppCoverageExtension.LCOV_FILENAME)
        FileTree lcovFiles = FileUtil.extractZipfile(project, archivePath)
        FileUtil.copyFileTreeToDest(project, lcovFiles, lcovDirectoryPath)
    }
}
