/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.gradle.plugins.ci

import com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryExtension
import org.gradle.api.artifacts.Configuration

/**
 * An extension used to configure the {@link com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin}.
 */
class SeasideCiExtension {

    /**
     * The default name of the m2 archive.
     */
    final static String DEFAULT_M2_ARCHIVE_NAME = "dependencies-m2.zip"

    /**
     * The names of the configurations that must be resolved when populating the M2 repository.
     */
    private final Collection<String> configurationsToResolve = new HashSet<>()

    /**
     * The name of the remote repository to use to resolve dependencies when populating and offline maven2 directory.
     * If no repository with this name is defined by the project, dependencies are resolved from the local maven
     * directory cache.
     */
    String remoteM2RepositoryName = SeasideRepositoryExtension.DEFAULT_REMOTE_MAVEN_CONSOLIDATED_NAME

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
     * Configures the output file of dependency report.  If not defined a default value of
     * {@link com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin#DEFAULT_DEPENDENCY_REPORT_FILE_NAME} inside the
     * @code $project.buildDir} directory is used.
     */
    File dependencyInfoReportFile

    /**
     * Configures the output file of the M2 dependency deployment script.  If not defined a default of
     * {@link com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin#DEFAULT_M2_DEPLOYMENT_SCRIPT_NAME} inside the
     * @code $project.buildDir} directory is used.
     */
    File deploymentScriptFile

    /**
     * Configure the
     */
    List<Configuration> configs

    /**
     * If true, a dependencies report will be generated.  If this is turned off, the generated deployment script will
     * not upload artifacts.
     */
    boolean createDependencyReportFile = true

    /**
     * Forces the early explicit resolution of the given configuration before attempting to determine its dependencies
     * when populating the M2 repository.
     */
    SeasideCiExtension forceResolutionOf(Configuration config) {
        return forceResolutionOf(config.getName())
    }

    /**
     * Forces the early explicit resolution of the configuration with the given name before attempting to determine its
     * dependencies when populating the M2 repository.
     */
    SeasideCiExtension forceResolutionOf(String configurationName) {
        configurationsToResolve.add(configurationName)
        return this
    }

    /**
     * Gets the names of the configurations that must be resolved when populating the M2 repository.
     */
    Collection<String> getConfigurationsToResolve() {
        return configurationsToResolve
    }
}
