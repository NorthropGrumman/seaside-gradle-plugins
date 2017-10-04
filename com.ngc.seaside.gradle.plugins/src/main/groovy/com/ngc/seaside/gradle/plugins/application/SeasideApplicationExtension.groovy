package com.ngc.seaside.gradle.plugins.application

import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

class SeasideApplicationExtension {

    static class OSType {

        String appHomeCmd
        String startScript

        OSType() {
        }
    }

    Project project

    SeasideApplicationExtension(Instantiator instantiator,
                                Project project) {
        this.project = project
    }

    String mainClassName
    String appHomeVarName
    String distributionName
    String installationDir
    List<String> includeDistributionDirs
    Map<String, String> appSystemProperties

    OSType windows, unix


    OSType windows(Closure closure) {
        windows = new OSType()
        project.configure(windows, closure)
        return windows
    }

    OSType unix(Closure closure) {
        unix = new OSType()
        project.configure(unix, closure)
        return unix
    }
}
