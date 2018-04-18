package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import org.gradle.api.Project

class SeasideEclipseUpdateSitePlugin extends AbstractProjectPlugin {
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    public static final String ECLIPSE_UPDATE_SITE_EXTENSION_NAME = "eclipseUpdateSite"
    public static final String ECLIPSE_COPY_FEATURES_TASK_NAME = "copyFeatures"
    public static final String ECLIPSE_COPY_SD_PLUGINS_TASK_NAME = "copySdPlugins"
    public static final String ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME = "copyEclipsePlugins"
    public static final String ECLIPSE_CREATE_ZIP_TASK_NAME = "createZip"

    @Override
    void doApply(Project project) {
    }
}
