/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
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
