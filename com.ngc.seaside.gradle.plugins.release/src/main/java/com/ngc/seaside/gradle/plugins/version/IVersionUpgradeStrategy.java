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
package com.ngc.seaside.gradle.plugins.version;

/**
 * The following interface is responsible for retrieving the correct
 * release version of a project from its pre-release version
 */
public interface IVersionUpgradeStrategy {

   /**
    * Retrieves the release version from its pre-release version of
    * a version based on the version upgrade strategy
    * 
    * @param currentVersion the pre-release version
    * @return {@link String} containing the release version
    */
   String getVersion(String currentVersion);

}
