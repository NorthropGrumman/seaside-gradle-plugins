package com.ngc.seaside.gradle.extensions.release

import org.gradle.api.Task

class SeasideReleaseExtension {
    private static final DEFAULT_PUSH = true
    private static final DEFAULT_UPLOAD_ARTIFACTS = true
    private static final DEFAULT_TAG_PREFIX = 'v'
    private static final DEFAULT_VERSION_SUFFIX = '-SNAPSHOT'

    boolean push = DEFAULT_PUSH
    boolean uploadArtifacts = DEFAULT_UPLOAD_ARTIFACTS
    String tagPrefix = DEFAULT_TAG_PREFIX
    String versionSuffix = DEFAULT_VERSION_SUFFIX

    def finalizedBy(String task) {
        this.finalizedBy(task)
    }
}
