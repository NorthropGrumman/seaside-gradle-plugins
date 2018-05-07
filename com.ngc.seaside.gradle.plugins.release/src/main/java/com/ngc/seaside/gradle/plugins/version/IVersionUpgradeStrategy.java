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
