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

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;
import com.ngc.seaside.gradle.plugins.release.ReleaseType;

import org.gradle.api.Project;

public class SeasideVersionPlugin extends AbstractProjectPlugin {

   public static final String VERSION_SETTINGS_CONVENTION_NAME = "versionSettings";

   private VersionResolver versionResolver;

   @Override
   public void doApply(Project project) {
      versionResolver = project.getExtensions()
                               .create(VERSION_SETTINGS_CONVENTION_NAME, VersionResolver.class, project);
      versionResolver.setEnforceVersionSuffix(false);
      project.setVersion(versionResolver.getUpdatedProjectVersionForRelease(ReleaseType.SNAPSHOT));
   }

}
