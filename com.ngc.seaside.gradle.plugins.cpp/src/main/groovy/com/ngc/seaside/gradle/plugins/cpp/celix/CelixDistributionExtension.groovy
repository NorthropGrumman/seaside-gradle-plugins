/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.gradle.plugins.cpp.celix

/**
 * Used to configure a project that generates a Celix distribution ZIP.
 */
class CelixDistributionExtension {

    /**
     * The default Celix bundles that are included in a distribution.
     */
    private final static String[] DEFAULT_BUNDLE_NAMES = [
        "dm_shell",
        "shell",
        "shell_tui"
    ]

    /**
     * The name of the distribution.
     */
    String distributionName

    /**
     * The name of the distribution directory.
     */
    String distributionDir

    /**
     * The location of the run script to generate.
     */
    String runScript

    /**
     * The names of the default bundles to include.
     */
    List<String> defaultBundlesToInclude = new ArrayList<>()

    CelixDistributionExtension() {
        defaultBundlesToInclude.addAll(DEFAULT_BUNDLE_NAMES)
    }

    /**
     * Includes the given default bundle in the distribution.
     * @param bundleName
     */
    void includeDefaultBundle(String bundleName) {
        defaultBundlesToInclude.add(bundleName)
    }
}
