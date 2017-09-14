package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.Project

import java.util.regex.Matcher
import java.util.regex.Pattern

class SeasideReleaseExtension {

    private final DEFAULT_VERSION_FILE = [project.rootProject.projectDir, 'build.gradle'].join(File.separator)
    private static final DEFAULT_DEPENDS_ON = Collections.singletonList('build')
    // TODO default push needs to be set to true
    private static final DEFAULT_PUSH = false

    private static final DEFAULT_TAG_PREFIX = 'v'
    private static final DEFAULT_VERSION_SUFFIX = '-SNAPSHOT'
    private static final Pattern PATTERN = Pattern.
            compile("^\\s*version\\s*=\\s*[\"']?(?!\\.)(\\d+(\\.\\d+)+)([-.][A-Z]+)?[\"']?(?![\\d.])\$",
                    Pattern.MULTILINE)

    private final Project project
    private final File versionFile

    // Configurable extensions
    List<Object> dependsOn = DEFAULT_DEPENDS_ON
    boolean push = DEFAULT_PUSH
    String tagPrefix = DEFAULT_TAG_PREFIX
    String versionSuffix = DEFAULT_VERSION_SUFFIX

    SeasideReleaseExtension(Project project) {
        this.project = project
        this.versionFile = new File(DEFAULT_VERSION_FILE)
    }

    def dependsOn(Object... paths) {
        this.dependsOn = Arrays.asList(paths)
    }

    String getTagName() {
        return tagPrefix + "$project.version" - versionSuffix
    }

    String getVersionFromFile() {
        return getSemanticVersion(versionFile.text.trim())
    }

    String getSemanticVersion(String input) {
        Matcher matcher = PATTERN.matcher(input.trim())
        StringBuilder sb = new StringBuilder()

        if (matcher.find()) {
            String version = matcher.group(1)
            String suffix = matcher.group(3)

            if (version != null) {
                sb.append(version)
                sb.append((suffix != null) ? suffix : "")
            }
            return sb
        } else {
            project.getLogger().error("\nFailed to extract version information from file contents:" + input)
            project.getLogger().error("Does the version information follow Semantic Versioning Format?\n")
            return null
        }
    }

    void setVersionOnFile(String newVersion) {
        versionFile.text = versionFile.text.replaceFirst(PATTERN, '\tversion = \'' + newVersion + '\'')
    }

    File getVersionFile() {
        return versionFile
    }
}
