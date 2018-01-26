package com.ngc.seaside.gradle.extensions.ci

import com.ngc.seaside.gradle.plugins.parent.SeasideParentPlugin

/**
 * An extension used to configure the {@link com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin}.
 */
class SeasideCiExtension {

    /**
     * The default name of the m2 archive.
     */
    final static String DEFAULT_M2_ARCHIVE_NAME = "dependencies-m2.zip"

    /**
     * The name of the remote repository to use to resolve dependencies when populating and offline maven2 directory.
     * If no repository with this name is defined by the project, dependencies are resolved from the local maven
     * directory cache.
     */
    String remoteM2RepositoryName = SeasideParentPlugin.REMOTE_MAVEN_REPOSITORY_NAME

    /**
     * Configures the output directory that will contain the dependencies of the project if an offline maven2 directory
     * is created.  If not defined a default value of
     * {@link com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin#DEFAULT_M2_OUTPUT_DIRECTORY_NAME} inside the
     * @code $project.buildDir} directory is used.
     */
    File m2OutputDirectory

    /**
     * Configures the output directory that will contain the archive of the m2 directory.  If not defined a default
     * value of {@code $project.buildDir} is used.
     */
    File m2ArchiveOutputDirectory

    /**
     * Configures the default name of the archive of the m2 directory.
     */
    String m2ArchiveName = DEFAULT_M2_ARCHIVE_NAME

    /**
     * Configures the output file of CSV dependency report.  If not defined a default value of
     * {@link com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin#DEFAULT_CSV_FILE_NAME} inside the
     * @code $project.buildDir} directory is used.
     */
    File dependencyInfoCsvFile

    /**
     * If true, a dependencies report in CSV format will be generated.
     */
    boolean createCsvFile = true
}
