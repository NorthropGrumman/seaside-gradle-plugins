/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
