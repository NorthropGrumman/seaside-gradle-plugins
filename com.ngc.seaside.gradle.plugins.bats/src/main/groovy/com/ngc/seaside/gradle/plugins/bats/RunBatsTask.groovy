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
