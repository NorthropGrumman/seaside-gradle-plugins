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
