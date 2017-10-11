package com.ngc.seaside.gradle.plugins.bats

import com.ngc.seaside.gradle.plugins.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class SeasideBatsPluginIT {

    private SeasideBatsPlugin plugin
    private Project project

    @Before
    void before() {
        def testProjectDir = createTheTestProjectDirectory()
        copyTheTestProjectInto(testProjectDir)
        project = createTheTestProjectUsing(testProjectDir)
        plugin = new SeasideBatsPlugin()
        plugin.apply(project)
    }

    @Test
    void appliesPlugin() {
        Assert.assertNotNull(project.extensions.findByName(SeasideBatsPlugin.BATS_EXTENSION_NAME))
        Assert.assertNotNull(TaskResolver.findTask(project, SeasideBatsPlugin.EXTRACT_BATS_TASK_NAME))
        Assert.assertNotNull(TaskResolver.findTask(project, SeasideBatsPlugin.RUN_BATS_TASK_NAME))
    }

    private static File createTheTestProjectDirectory() {
        def dir = pathToTheDestinationProjectDirectory()
        return Files.createDirectories(dir.toPath()).toFile()
    }

    private static File pathToTheDestinationProjectDirectory() {
        return turnListIntoPath(
                "build", "integrationTest", "bats",
                "sealion-java-hello-world"
        )
    }

    private static void copyTheTestProjectInto(File dir) {
        FileUtils.copyDirectory(sourceDirectoryWithTheTestProject(), dir)
    }

    private static File sourceDirectoryWithTheTestProject() {
        return turnListIntoPath(
                "src", "integrationTest", "resources",
                "sealion-java-hello-world"
        )
    }

    private static Project createTheTestProjectUsing(File dir) {
        return ProjectBuilder.builder().withProjectDir(dir).build()
    }

    private static File turnListIntoPath(String... list) {
        return Paths.get(list.flatten().join(File.separator)).toFile()
    }
}
