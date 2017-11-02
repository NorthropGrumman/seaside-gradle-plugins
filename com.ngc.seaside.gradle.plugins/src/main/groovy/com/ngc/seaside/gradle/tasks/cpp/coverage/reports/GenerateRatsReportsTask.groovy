package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.ngc.seaside.gradle.api.tasks.AbstractCoverageTask
import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
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
        def ratsXmlFile = createRatsXmlFile()
        String ratsCommand = "$rats --database $ratsDatabasePath $dir"

        def arguments = [
                "-c", ratsCommand + " --xml > $ratsXmlFile"
        ]

        project.exec { process ->
            process.executable("bash")
            process.args(arguments)
        }

        arguments = [
                "-c", ratsCommand + " --html > $ratsHtmlFile"
        ]

        project.exec {
            executable "bash"
            args arguments
        }
    }

    private void doGenRatsHtml(String dir) {
        String rats = ratsExecutable

        String ratsCommand = "$rats --database $ratsDatabasePath $dir"
        def ratsHtmlFile = createRatsHtmlFile()


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
        FileTree cppcheckFiles = FileUtil.extractZipfile(project, archivePath)
        FileUtil.copyFileTreeToDest(project, cppcheckFiles, ratsDirectoryPath)
    }
}
