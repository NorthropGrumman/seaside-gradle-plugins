package com.ngc.seaside.gradle.extensions.release

import javax.xml.bind.annotation.XmlType.DEFAULT


class SeasideReleaseExtension {
    private static final DEFAULT_PUSH = true
    private static final DEFAULT_UPLOAD_ARTIFACTS = true
    private static final DEFAULT_TAG_PREFIX = 'v'
    private static final DEFAULT_TAG = ''
    private static final DEFAULT_VERSION_SUFFIX = '-SNAPSHOT'
    private static final DEFAULT_COMMIT_CHANGES = true

    boolean commitChanges = DEFAULT_COMMIT_CHANGES
    boolean push = DEFAULT_PUSH
    boolean uploadArtifacts = DEFAULT_UPLOAD_ARTIFACTS
    String tagPrefix = DEFAULT_TAG_PREFIX
    String versionSuffix = DEFAULT_VERSION_SUFFIX
    String tag = DEFAULT_TAG

    def finalizedBy(String task) {
        this.finalizedBy(task)
    }
}
