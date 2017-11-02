package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.api.tasks.AbstractCoverageTask
import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.util.FileUtil
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

class GenerateCppCheckReportsTask extends AbstractCoverageTask {

    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions.findByType(SeasideCppCoverageExtension.class)

    private final String CPPCHECK_DIRECTORY_PATH = FileUtil.toPath(project.buildDir.name, "tmp", "cppcheck")
    private final String CPPCHECK_EXECUTABLE = FileUtil.toPath(CPPCHECK_DIRECTORY_PATH,
                                                               "cppcheck-$cppCoverageExtension.CPPCHECK_VERSION",
                                                               "cppcheck")
    private final String CPPCHECK_HTML_EXECUTABLE = FileUtil.toPath(CPPCHECK_DIRECTORY_PATH,
                                                                    "cppcheck-$cppCoverageExtension.CPPCHECK_VERSION",
                                                                    "htmlreport", "cppcheck-htmlreport")
    private final String CPPCHECK_HTML_DIR = FileUtil.toPath(project.buildDir.absolutePath, "reports", "cppcheck",
                                                             "html")
    private final String PYGMENTS_DIRECTORY_PATH = FileUtil.toPath(project.buildDir.name, "tmp", "pygments")

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



        String cppcheck = CPPCHECK_EXECUTABLE
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
        String cppcheckHtml = CPPCHECK_HTML_EXECUTABLE
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
        def f = new File(CPPCHECK_HTML_DIR)
        f.parentFile.mkdirs()
        return f
    }

    private void extractPygments() {
        FileTree pygmentFiles = FileUtil.extractZipfile(project, pathToThePygmentsReleaseArchive())
        FileUtil.copyFileTreeToDest(project, pygmentFiles, PYGMENTS_DIRECTORY_PATH)
    }

    private void extractCppCheckTool() {
        FileTree cppcheckFiles = FileUtil.extractZipfile(project, pathToTheCppCheckReleaseArchive())
        FileUtil.copyFileTreeToDest(project, cppcheckFiles, CPPCHECK_DIRECTORY_PATH)
    }

    private String pathToThePygmentsReleaseArchive() {
        return Paths.get(findTheReleaseArchiveFile(cppCoverageExtension.PYGMENTS_FILENAME))
    }

    private String pathToTheCppCheckReleaseArchive() {
        return Paths.get(findTheReleaseArchiveFile(cppCoverageExtension.CPPCHECK_FILENAME))
    }
}
