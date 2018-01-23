package com.ngc.seaside.gradle.extensions.ci

import com.ngc.seaside.gradle.plugins.parent.SeasideParentPlugin

/**
 * An extension used to configure the {@link com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin}.
 */
class SeasideCiExtension {

    /**
     * The name of the remote repository to use to resolve dependencies when populating and offline maven2 directory.
     * If no repository with this name is defined by the project, dependencies are resolved from the local maven
     * directory cache.
     */
    String remoteM2RepositoryName = SeasideParentPlugin.REMOTE_MAVEN_REPOSITORY_NAME

    /**
     * Configures the output directory that will contain the dependencies of the project if an offline maven2 directory
     * is created.  If not defined a default value is used.
     */
    File m2OutputDirectory
}
