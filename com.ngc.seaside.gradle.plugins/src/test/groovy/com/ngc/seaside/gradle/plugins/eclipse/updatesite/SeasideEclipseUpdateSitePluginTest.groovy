package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import org.junit.Assert
import org.junit.Test

class SeasideEclipseUpdateSitePluginTest {
    @Test
    void classExists() {
        def plugin = new SeasideEclipseUpdateSitePlugin()
        Assert.assertNotNull("plugin does not exist!", plugin)
    }

    @Test
    void extendsAbstractProjectPlugin() {
        def plugin = new SeasideEclipseUpdateSitePlugin()
        Assert.assertTrue(
              "plugin is not an instance of AbstractProjectPlugin",
              plugin instanceof AbstractProjectPlugin
        )
    }

    @Test
    void hasNeededPublicNames() {
        Assert.assertNotNull("extension name does not exist!", SeasideEclipseUpdateSitePlugin.ECLIPSE_EXTENSION_NAME)
        Assert.assertNotNull("task group name does not exist!", SeasideEclipseUpdateSitePlugin.ECLIPSE_TASK_GROUP_NAME)
    }
}
