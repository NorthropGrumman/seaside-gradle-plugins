/**
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
package com.ngc.seaside.gradle.plugins.release;

/**
 * Defines the different types of releases that can be performed.
 */
public enum ReleaseType {

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