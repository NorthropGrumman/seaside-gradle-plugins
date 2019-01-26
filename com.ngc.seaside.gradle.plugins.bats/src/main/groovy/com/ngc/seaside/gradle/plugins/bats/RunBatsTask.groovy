/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.gradle.plugins.bats

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class RunBatsTask extends DefaultTask {

    SeasideBatsExtension batsExtension =
            project.extensions
                    .findByType(SeasideBatsExtension.class)

    @TaskAction
    def runBats() {
        println(">>>>>>>>>>>BATS:" + batsExtension.resultsFile)
        def bats = pathToTheBatsScript()
        def tests = enumerateAllBatsFilesIn(pathToTheDirectoryWithBatsTests())
        def commandOutput = new ByteArrayOutputStream()

        project.exec {
            executable bats
            args tests
            standardOutput commandOutput
        }

        print commandOutput.toString()
        writeTestResultsFile(commandOutput)
    }

    private String pathToTheBatsScript() {
        return batsExtension.BATS_PATHS.PATH_TO_THE_BATS_SCRIPT
    }

    private String pathToTheDirectoryWithBatsTests() {
        return batsExtension.batsTestsDir
    }

    private Set<File> enumerateAllBatsFilesIn(String path) {
        return project.fileTree(path).getFiles()
                .findAll { file -> file.name.endsWith(".bats") }
    }

    private void writeTestResultsFile(ByteArrayOutputStream commandOutput) {
        def f = resultsFile(pathToTheBatsResultsFile())
        f << commandOutput.toString()
    }

    private File resultsFile(String path) {
        return createTheResultsFileIfNecessary(project.file(path))
    }

    private static File createTheResultsFileIfNecessary(File f) {
        f.getParentFile().mkdirs()
        f.createNewFile()
        return f
    }

    private String pathToTheBatsResultsFile() {
        return batsExtension.resultsFile
    }
}
