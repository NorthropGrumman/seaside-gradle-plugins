package com.ngc.seaside.gradle.extensions.release

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
