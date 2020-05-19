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
package com.ngc.seaside.gradle.plugins.cpp.coverage.reports

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.plugins.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.util.FileUtil
import org.gradle.api.Project
import org.gradle.api.file.FileTree

class GenerateCoverageXmlTask {


    def static generateLcovXml(Project project) {
        SeasideCppCoverageExtension extension = project.extensions.findByType(SeasideCppCoverageExtension.class)
        String coverageFilePath = extension.coverageFilePath
        Preconditions.checkState(new File(coverageFilePath).exists(), "$coverageFilePath does not exist!")


        String coberturaDirectoryPath = FileUtil.toPath(project.buildDir.name, "tmp", "lcov-cobertura")
        String lcovCobertura = FileUtil.toPath(coberturaDirectoryPath,
                                               "lcov-to-cobertura-xml-${extension.LCOV_COBERTURA_VERSION}",
                                               "lcov_cobertura", "lcov_cobertura.py")

        String archivePath = pathToTheCoberturaReleaseArchive(project, extension)
        FileTree coberturaFiles = FileUtil.extractZipfile(project, archivePath)
        copyFileTreeToDest(project, coberturaFiles, coberturaDirectoryPath)

        File coverageXmlPath = createCoverageXmlFile(extension)
        def arguments = [
                lcovCobertura,
                coverageFilePath,
                "--demangle",
                "--output",
                coverageXmlPath
        ]

        project.exec { process ->
            process.executable("python")
            process.args(arguments)
        }.assertNormalExitValue()
    }

    /**
     * Creates coverage xml file directories
     * @param extension extension container of coverageXmlPath
     * @return File handler to coverage xml file
     */
    private static File createCoverageXmlFile(SeasideCppCoverageExtension extension) {
        def f = new File(extension.coverageXmlPath)
        f.parentFile.mkdirs()
        return f
    }

    /**
     * Copies the extracted cobertura tool files to a location where it will be ran from.
     * @param project the project to run this task from
     * @param coberturaFiles {@link FileTree} containing extracted cobertura files
     * @param coberturaDirectoryPath destination where files will be placed
     */
    private static void copyFileTreeToDest(Project project, FileTree coberturaFiles, String coberturaDirectoryPath) {
        FileUtil.copyFileTreeToDest(project, coberturaFiles, coberturaDirectoryPath)
    }

    /**
     * Retrieves the path to the archive file
     * @param project the project to run this task from
     * @param extension extension container of coverageXmlPath
     * @return path to cobertura archive
     */
    private static String pathToTheCoberturaReleaseArchive(Project project, SeasideCppCoverageExtension extension) {
        return project.configurations.getByName("compile").filter { file ->
            return file.name.endsWith(extension.LCOV_COBERTURA_FILENAME)
        }.getAsPath()
    }
}
