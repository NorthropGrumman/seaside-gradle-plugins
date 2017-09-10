package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.tasks.release.publish.PublishChannelContainer
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class SeasideReleaseExtension {

    private static final DEFAULT_VERSION_FILE = 'version.txt'
    private static final DEFAULT_DEPENDS_ON = Collections.singletonList('build')
    private static final DEFAULT_PUSH = false
    private static final DEFAULT_TAG_PREFIX = 'v'
    private static final DEFAULT_VERSION_SUFFIX = '-SNAPSHOT'

    private final Project project
    private final PublishChannelContainer channelContainer
    private final File versionFile

    List<Object> dependsOn = DEFAULT_DEPENDS_ON
    boolean push = DEFAULT_PUSH
    String tagPrefix = DEFAULT_TAG_PREFIX
    String versionSuffix = DEFAULT_VERSION_SUFFIX

    SeasideReleaseExtension(Project project) {
        this.project = project
        this.channelContainer = new PublishChannelContainer()
        this.versionFile = project.file(DEFAULT_VERSION_FILE)
    }

    def dependsOn(Object... paths) {
        this.dependsOn = Arrays.asList(paths)
    }

    public PublishChannelContainer publish(Closure closure) {
        ConfigureUtil.configure(closure, channelContainer)
        return channelContainer
    }

    public PublishChannelContainer getPublishChannels() {
        return channelContainer
    }

    public String getTagName() {
        return tagPrefix + "$project.version" - versionSuffix
    }

    public File getVersionFile() {
        return versionFile
    }
}
