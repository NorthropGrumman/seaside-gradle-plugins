package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.tasks.cpp.coverage.GenerateCoverageDataTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.reports.GenerateCppCheckReportsTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.reports.GenerateRatsReportsTask
import org.gradle.api.Project

class SeasideCppCoveragePlugin extends AbstractProjectPlugin {

    public static final String CPP_COVERAGE_EXTENSION_NAME = "seasideCppCov"
    public static final String CPP_COVERAGE_TASK_GROUP_NAME = "cpp_analysis"

    public static final String GENERATE_COVERAGE_DATA_TASK_NAME = "coverage"
    public static final String GENERATE_CPPCHECK_REPORT_TASK_NAME = "cppCheck"
    public static final String GENERATE_RATS_REPORT_TASK_NAME = "rats"
    public static final String GENERATE_FULL_COVERAGE_REPORT_TASK_NAME = "genFullCoverageReport"

    String coverageFilePath
    String coverageXmlPath
    String cppCheckXmlPath
    String ratsXmlPath

    @Override
    void doApply(Project project) {
        project.configure(project) {
            SeasideCppCoverageExtension extension = createTheCppCoverageExtensionOnTheProject(project)

            // This has to be done in this closure else the visibility for the -P & -D is lost
            extension.coverageFilePath = coverageFilePath ?: extension.coverageFilePath
            extension.coverageXmlPath = coverageXmlPath ?: extension.coverageXmlPath
            extension.cppCheckXmlPath = cppCheckXmlPath ?: extension.cppCheckXmlPath
            extension.ratsXmlPath = ratsXmlPath ?: extension.ratsXmlPath

            createTasks(project)


            project.afterEvaluate {
                project.dependencies {
                    compile "$extension.LCOV_GROUP_ARTIFACT_VERSION"
                    compile "$extension.LCOV_COBERTURA_GROUP_ARTIFACT_VERSION"
                    compile "$extension.CPPCHECK_GROUP_ARTIFACT_VERSION"
                    compile "$extension.PYGMENTS_GROUP_ARTIFACT_VERSION"
                    compile "$extension.RATS_GROUP_ARTIFACT_VERSION"
                }
            }
        }
    }

    static createTheCppCoverageExtensionOnTheProject(Project p) {
        return p.extensions.create(CPP_COVERAGE_EXTENSION_NAME, SeasideCppCoverageExtension, p)
    }

    /**
     * Create project tasks for this plugin
     * @param project
     */
    static void createTasks(Project project) {

        project.task(GENERATE_COVERAGE_DATA_TASK_NAME, type: GenerateCoverageDataTask,
                     group: CPP_COVERAGE_TASK_GROUP_NAME,
                     description: "Generate and filter coverage data then store in the specified directory",
                     dependsOn: "build")

        project.task(GENERATE_CPPCHECK_REPORT_TASK_NAME, type: GenerateCppCheckReportsTask,
                     group: CPP_COVERAGE_TASK_GROUP_NAME,
                     description: "Generates xml and html reports for static code analysis on cpp projects.",
                     dependsOn: "build")

        project.task(GENERATE_RATS_REPORT_TASK_NAME, type: GenerateRatsReportsTask,
                     group: CPP_COVERAGE_TASK_GROUP_NAME,
                     description: "Generates xml and html reports security scans on cpp projects.",
                     dependsOn: "build")

        project.task(GENERATE_FULL_COVERAGE_REPORT_TASK_NAME, group: CPP_COVERAGE_TASK_GROUP_NAME,
                     description: "Generate the cobertura and html reports.",
                     dependsOn: [GENERATE_COVERAGE_DATA_TASK_NAME, GENERATE_CPPCHECK_REPORT_TASK_NAME,
                                 GENERATE_RATS_REPORT_TASK_NAME])
    }
}
