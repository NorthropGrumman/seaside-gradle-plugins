package com.ngc.seaside.gradle.plugins.cpp.dependencies;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for Static and Shared libraries
 */
public abstract class AbstractBuildConfiguration {
    private Project project;
    private String dependency;
    private String version; //optional
    private List<String> libs = new ArrayList<>(); //optional

    /**
     * Constructor that requires the project in order to configure closures.
     *
     * @param project the project.
     */
    protected AbstractBuildConfiguration(Project project) {
        this.project = project;
    }

    /**
     * Get the dependency name.
     *
     * @return the name of the dependency.
     */
    public String getDependency() {
        return dependency;
    }

    /**
     * Set the name of the dependency. Optional way to set a property in Groovy/Gradle
     *
     * @param dependency the dependency name
     */
    public void dependency(String dependency) {
        this.dependency = dependency;
    }

    /**
     * Set the dependency
     *
     * @param dependency
     */
    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    /**
     * Get the libs. This is an optional field and may return an empty list.
     *
     * @return the libraries or empty if none set.
     */
    public List<String> getLibs() {
        return libs;
    }

    /**
     * Set the libs
     *
     * @param libs the libs
     */
    public void setLibs(List<String> libs) {
        this.libs = libs;
    }

    /**
     * Set the libs. Optional way to set a property in Groovy/Gradle
     *
     * @param libs the libs
     */
    public void libs(String... libs) {
        Collections.addAll(this.libs, libs);
    }

    /**
     * Get the version. This is an optional field and may return an empty string.
     *
     * @return the version or empty string.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the version. Optional way to set a property in Groovy/Gradle
     *
     * @param version the version.
     */
    public void version(String version) {
        this.version = version;
    }

    /**
     * Set the version.
     *
     * @param version the version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "dependency='" + dependency + '\'' +
                ", version='" + version + '\'' +
                ", libs=" + libs;
    }

    /**
     * Get the project.
     *
     * @return the project.
     */
    protected Project getProject() {
        return project;
    }
}
