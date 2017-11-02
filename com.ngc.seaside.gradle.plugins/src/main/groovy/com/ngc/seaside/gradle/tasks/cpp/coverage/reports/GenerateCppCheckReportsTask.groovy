package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.api.tasks.AbstractCoverageTask
import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.util.FileUtil
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

class GenerateCppCheckReportsTask extends AbstractCoverageTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions.findByType(SeasideCppCoverageExtension.class)

    private final String cppcheckDirectoryPath = FileUtil.toPath(project.buildDir.name, "tmp", "cppcheck")
    private final String cppcheckFolder = FileUtil.toPath(cppcheckDirectoryPath,
                                                          "cppcheck-$cppCoverageExtension.CPPCHECK_VERSION")
    private final String cppcheckExecutable = FileUtil.toPath(cppcheckFolder, "cppcheck")
    private final String cppcheckHtmlExecutable = FileUtil.toPath(cppcheckFolder, "htmlreport", "cppcheck-htmlreport")
    private final String cppcheckHtmlDir = FileUtil.toPath(project.buildDir.absolutePath, "reports", "cppcheck", "html")
    private final String pygmentsDirectoryPath = FileUtil.toPath(project.buildDir.name, "tmp", "pygments")

    @TaskAction
    def generateCppCheckReports() {
        extractCppCheckTool()


        String dir = [project.projectDir.absolutePath, "src", "main", "cpp"].join(File.separator)
        if (new File(dir).exists()) {
            doCppCheck(dir)
        }
    }


    private void doCppCheck(String dir) {
        String includesDir = [project.projectDir.absolutePath, "src", "main", "include"].join(File.separator)



        String cppcheck = cppcheckExecutable
        File cppcheckXmlFile = createCppCheckXmlFile()

        def arguments = [
                "--enable=all",
                "--force", "--suppress=missingInclude",
                "--xml", "--xml-version=2",
                "-I", includesDir, dir,
                "--output-file=$cppcheckXmlFile"
        ]

        if (project.exec { process ->
            process.executable(cppcheck)
            process.args(arguments)
        }.getExitValue() == 0) {
            genCppCheckHtml(cppcheckXmlFile)
        }
    }

    private void genCppCheckHtml(File cppcheckXmlFile) {
        Preconditions.checkState(cppcheckXmlFile.exists(), "$cppcheckXmlFile.absolutePath does not exist!")
        extractPygments()
        String cppcheckHtml = cppcheckHtmlExecutable
        File cppcheckHtmlDir = createCppCheckHtmlFile()

        def arguments = [
                "--source-encoding=\"iso8859-1\"",
                "--title=\"$project.projectDir.name\"",
                "--report-dir=$cppcheckHtmlDir",
                "--file=$cppcheckXmlFile"
        ]

        project.exec { process ->
            process.executable(cppcheckHtml)
            process.args(arguments)
        }
    }

    private File createCppCheckXmlFile() {
        def f = new File(cppCoverageExtension.cppCheckXmlPath)
        f.parentFile.mkdirs()
        return f
    }

    private File createCppCheckHtmlFile() {
        def f = new File(cppcheckHtmlDir)
        f.parentFile.mkdirs()
        return f
    }

    private void extractPygments() {
        String archivePath = findTheReleaseArchiveFile(cppCoverageExtension.PYGMENTS_FILENAME)
        FileTree pygmentFiles = FileUtil.extractZipfile(project, archivePath)
        FileUtil.copyFileTreeToDest(project, pygmentFiles, pygmentsDirectoryPath)
    }

    private void extractCppCheckTool() {
        String archivePath = findTheReleaseArchiveFile(cppCoverageExtension.CPPCHECK_FILENAME)
        FileTree cppcheckFiles = FileUtil.extractZipfile(project, archivePath)
        FileUtil.copyFileTreeToDest(project, cppcheckFiles, cppcheckDirectoryPath)
    }
}
