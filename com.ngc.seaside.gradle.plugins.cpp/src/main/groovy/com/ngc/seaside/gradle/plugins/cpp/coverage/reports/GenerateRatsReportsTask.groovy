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

import com.ngc.seaside.gradle.api.AbstractCoverageTask
import com.ngc.seaside.gradle.plugins.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.util.FileUtil

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

class GenerateRatsReportsTask extends AbstractCoverageTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions.findByType(SeasideCppCoverageExtension.class)

    private final String ratsDirectoryPath = FileUtil.toPath(project.buildDir.name, "tmp", "rats")
    private final String ratsFolder = FileUtil.toPath(ratsDirectoryPath, "rats-$cppCoverageExtension.RATS_VERSION")
    private final String ratsDatabasePath = FileUtil.toPath(ratsFolder, "rats-c.xml")
    private final String ratsExecutable = FileUtil.toPath(ratsFolder, "rats")

    @TaskAction
    def generateRatsReports() {
        extractRatsTool()
        def dir = [project.projectDir.absolutePath, "src", "main", "cpp"].join(File.separator)


        if (new File(dir).exists()) {
            doGenRatsXml(dir)
            doGenRatsHtml(dir)
        }
    }

    private void doGenRatsXml(String dir) {
        String rats = ratsExecutable
        String ratsCommand = "$rats --database $ratsDatabasePath $dir"
        File ratsXmlFile = createRatsXmlFile()

        def arguments = [
                "-c", ratsCommand + " --xml > $ratsXmlFile"
        ]

        project.exec { process ->
            process.executable("bash")
            process.args(arguments)
        }.assertNormalExitValue()
    }

    private void doGenRatsHtml(String dir) {
        String rats = ratsExecutable
        String ratsCommand = "$rats --database $ratsDatabasePath $dir"
        File ratsHtmlFile = createRatsHtmlFile()

        def arguments = [
                "-c", ratsCommand + " --html > $ratsHtmlFile"
        ]

        project.exec { process ->
            process.executable("bash")
            process.args(arguments)
        }.assertNormalExitValue()
    }

    private File createRatsXmlFile() {
        def f = new File(cppCoverageExtension.ratsXmlPath)
        f.parentFile.mkdirs()
        return f
    }

    private File createRatsHtmlFile() {
        def f = new File(cppCoverageExtension.ratsHtmlPath)
        f.parentFile.mkdirs()
        return f
    }

    private void extractRatsTool() {
        String archivePath = findTheReleaseArchiveFile(cppCoverageExtension.RATS_FILENAME)
        FileTree ratsFiles = FileUtil.extractZipfile(project, archivePath)
        FileUtil.copyFileTreeToDest(project, ratsFiles, ratsDirectoryPath)
    }
}
