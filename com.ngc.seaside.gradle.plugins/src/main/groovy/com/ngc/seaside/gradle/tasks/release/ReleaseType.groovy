package com.ngc.seaside.gradle.tasks.release

/**
 * Defines the different types of releases that can be performed.
 */
enum ReleaseType {

    /**
     * A snapshot release isn't a real release at all.  During this type of release, the version number is not modified.
     */
    SNAPSHOT,

    /**
     * A patch release updates the patch version number.
     */
    PATCH,

    /**
     * A minor release updates the minor version number.
     */
    MINOR,

    /**
     * A major release updates the major version number.
     */
    MAJOR
}
