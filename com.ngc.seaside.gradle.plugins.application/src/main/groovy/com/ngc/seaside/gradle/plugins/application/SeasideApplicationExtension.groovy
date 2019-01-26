/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
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
