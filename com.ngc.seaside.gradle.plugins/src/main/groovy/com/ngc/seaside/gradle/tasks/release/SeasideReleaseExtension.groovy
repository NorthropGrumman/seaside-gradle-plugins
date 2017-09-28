package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.regex.Matcher
import java.util.regex.Pattern

class SeasideReleaseExtension {

    private final DEFAULT_VERSION_FILE = [project.rootProject.projectDir, 'build.gradle'].join(File.separator)
    private static final DEFAULT_PUSH = true
    private static final DEFAULT_UPLOAD_ARTIFACTS = true
    private static final DEFAULT_TAG_PREFIX = 'v'
    private static final DEFAULT_VERSION_SUFFIX = '-SNAPSHOT'
    private static final Pattern PATTERN = Pattern.
            compile("^\\s*version\\s*=\\s*[\"']?(?!\\.)(\\d+(\\.\\d+)+)([-.][A-Z]+)?[\"']?(?![\\d.])\$",
                    Pattern.MULTILINE)


    private final Project project
    private final File versionFile
    private String releaseVersion
    private String preReleaseVersion

    // Configurable extensions
    boolean push = DEFAULT_PUSH
    boolean uploadArtifacts = DEFAULT_UPLOAD_ARTIFACTS
    String tagPrefix = DEFAULT_TAG_PREFIX
    String versionSuffix = DEFAULT_VERSION_SUFFIX



    SeasideReleaseExtension(Project project) {
        this.project = project
        this.versionFile = new File(DEFAULT_VERSION_FILE)
    }

    /**
     * Returns the tagName that will be used for committing the released project
     * @return {@link String} of git tag name
     */
    String getTagName() {
        return tagPrefix + "$project.version" - versionSuffix
    }

    /**
     * Parses the version file to retrieve version set in file
     * @return pre-release version {@link String} from the version file
     */
    String getPreReleaseVersionFromFile() {
        return getSemanticVersion(versionFile.text.trim())
    }

    /**
     * Sets the release version of the extension instance of release plugin
     * @param version version to be set
     * @return an instance of {@link SeasideReleaseExtension}
     */
    SeasideReleaseExtension setReleaseVersion(version) {
        this.releaseVersion = version
        return this
    }

    /**
     * Returns the release version of the project
     * @return {@link String} containing release version of project
     */
    String getReleaseVersion() {
        return releaseVersion
    }

    /**
     * Returns the pre-release version of the project
     * @return {@link String} containing pre-release version of project
     */
    String getPreReleaseVersion() {
        return preReleaseVersion
    }

    /**
     * Retrieves the version attribute specified in the version file.
     * One of the acceptable formats for the version is: "version = <version>"
     * @param input contents of the version file
     * @return {@link String} of the project version specified
     */
    String getSemanticVersion(String input) {
        Matcher matcher = PATTERN.matcher(input.trim())
        StringBuilder sb = new StringBuilder()

        if (matcher.find()) {
            String version = matcher.group(1)
            String suffix = matcher.group(3)
            if (suffix != null && version != null) {
                sb.append(version).append(suffix)
                this.preReleaseVersion = sb.toString()
                return sb.toString()
            } else {
                project.getLogger().error("Missing project version (${version}${suffix})  suffix: $versionSuffix")
                throw new GradleException("Missing project version (${version}${suffix}) suffix:$versionSuffix")
            }
        } else {
            project.getLogger().error("\nFailed to extract semantic versioning information from file contents:$input")
            project.getLogger().error("Does the version information follow Semantic Versioning Format?\n")
            throw new GradleException("Version:$input \ndoes not follow semantic versioning format")
        }
    }

    /**
     * Modifies the version number on the version file
     * @param newVersion the new version to write to version file
     * @return an instance of {@link SeasideReleaseExtension}
     */
    SeasideReleaseExtension setVersionOnFile(String newVersion) {
        versionFile.text = versionFile.text.replaceFirst(PATTERN, "\tversion = \'$newVersion\'")
        return this
    }

    /**
     * Returns the file handler for the version file
     * @return {@link File} handler
     */
    File getVersionFile() {
        return versionFile
    }

    def finalizedBy(String task) {
        this.finalizedBy(task)
    }
}
