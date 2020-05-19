/**
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
