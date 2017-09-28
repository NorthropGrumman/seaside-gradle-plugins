package com.ngc.seaside.gradle.plugins.Parent

import com.ngc.seaside.gradle.plugins.parent.SeasideParentPlugin
import com.ngc.seaside.gradle.plugins.util.GradleUtil
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class SeasideParentPluginIT {

    private static final String TEST_VERSION_NUMBER = " 1.2.3-SNAPSHOT     " // with whitespace

    private File projectDir
    private Project project
    private SeasideParentPlugin plugin

    @Before
    void before() {

        File source = Paths.get("src/test/resources/parent//sealion-java-hello-world").toFile()
        projectDir = Files.createDirectories(Paths.get("build/test-release-plugin/sealion-java-hello-world")).toFile()
        FileUtils.copyDirectory(source, projectDir)

        def versionFile = new File(projectDir, 'build.gradle')

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        project.setProperty('nexusConsolidated', 'http://10.207.42.137/nexus/repository/maven-public')
        project.setProperty('nexusReleases', 'http://10.207.42.137/nexus/repository/ceacide-releases')
        project.setProperty('nexusSnapshots', 'http://10.207.42.137/nexus/repository/ceacide-snapshots')
        project.setProperty('nexusUsername', 'mlacombe')
        project.setProperty('nexusPassword', '%AnnieLaC06#')

        println(System.getenv("GRADLE_USER_HOME"))

        plugin = new SeasideParentPlugin()
        plugin.apply(project)
    }

    @Test
    void doesApplyPlugin() {
        Assert.assertEquals(TEST_VERSION_NUMBER.trim(), project.version)
        Assert.assertNotNull(project.tasks.findByName(SeasideParentPlugin.PARENT_SOURCE_JAR_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(SeasideParentPlugin.PARENT_JAVADOC_JAR_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(SeasideParentPlugin.PARENT_DOWNLOAD_DEPENDENCIES_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(SeasideParentPlugin.PARENT_CLEANUP_DEPENDENCIES_TASK_NAME))
    }
}
