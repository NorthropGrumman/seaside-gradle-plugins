package com.ngc.seaside.gradle.plugins.eclipse.feature

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.eclipse.feature.SeasideEclipseExtension
import org.junit.Assert
import org.junit.Test

class SeasideEclipseFeaturePluginTest {
    @Test
    void classExists() {
        def plugin = new SeasideEclipseFeaturePlugin()
        Assert.assertNotNull("plugin does not exist!", plugin)
    }

    @Test
    void extendsAbstractProjectPlugin() {
        def plugin = new SeasideEclipseFeaturePlugin()
        Assert.assertTrue(
              "plugin is not an instance of AbstractProjectPlugin",
              plugin instanceof AbstractProjectPlugin
        )
    }

    @Test
    void hasNeededPublicNames() {
        Assert.assertNotNull("extension name does not exist!", SeasideEclipseFeaturePlugin.ECLIPSE_EXTENSION_NAME)
        Assert.assertNotNull("task group name does not exist!", SeasideEclipseFeaturePlugin.ECLIPSE_TASK_GROUP_NAME)
        Assert.assertNotNull(
              "create jar task name does not exist!",
              SeasideEclipseFeaturePlugin.ECLIPSE_CREATE_JAR_TASK_NAME
        )
        Assert.assertNotNull(
              "copy feature file task name does not exist!",
              SeasideEclipseFeaturePlugin.ECLIPSE_COPY_FEATURE_FILE_TASK_NAME
        )
    }
}
