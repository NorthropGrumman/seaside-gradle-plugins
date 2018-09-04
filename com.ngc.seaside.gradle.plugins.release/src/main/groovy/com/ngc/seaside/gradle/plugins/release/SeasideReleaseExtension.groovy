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
package com.ngc.seaside.gradle.plugins.release

class SeasideReleaseExtension {
    private static final boolean DEFAULT_PUSH = true
    private static final boolean DEFAULT_UPLOAD_ARTIFACTS = true
    private static final boolean DEFAULT_COMMIT_CHANGES = true
    private static final String DEFAULT_TAG_PREFIX = 'v'
    private static final String DEFAULT_TAG = ''
    private static final String DEFAULT_VERSION_SUFFIX = '-SNAPSHOT'

    boolean push = DEFAULT_PUSH
    boolean uploadArtifacts = DEFAULT_UPLOAD_ARTIFACTS
    boolean commitChanges = DEFAULT_COMMIT_CHANGES
    String tagPrefix = DEFAULT_TAG_PREFIX
    String tag = DEFAULT_TAG
    String versionSuffix = DEFAULT_VERSION_SUFFIX

    def finalizedBy(String task) {
        this.finalizedBy(task)
    }
}
