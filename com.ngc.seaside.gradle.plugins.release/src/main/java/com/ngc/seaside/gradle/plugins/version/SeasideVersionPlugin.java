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
