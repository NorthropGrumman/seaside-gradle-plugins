/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import com.ngc.seaside.gradle.tasks.DefaultTaskAction;

import org.apache.commons.io.FileUtils;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class RemoveSnapshotsAction extends DefaultTaskAction<PopulateMaven2Repository> {

   private Collection<DependencyResult> dependencyResults;

   public RemoveSnapshotsAction setDependencyResults(
         Collection<DependencyResult> dependencyResults) {
      this.dependencyResults = Preconditions.checkNotNull(dependencyResults, "dependencyResults may not be null!");
      return this;
   }

   @Override
   protected void doExecute() {
      if (task.isRemoveSnapshots()) {
         Preconditions.checkState(dependencyResults != null, "dependencyResults must be set!");

         for (DependencyResult dependencyResult : dependencyResults) {
            for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
               if (artifactResult.getArtifact().isSnapshot()) {
                  File parentDir = artifactResult.getArtifact().getFile().getParentFile();
                  if (parentDir.isDirectory()) {
                     logger.info("Removing {} from local maven repository to avoid polluting the repository.",
                                 parentDir);
                     // Delete the entire directory contents of the dependency.
                     try {
                        FileUtils.deleteDirectory(parentDir);
                     } catch (IOException e) {
                        logger.error("Unexpected exception while deleting {}.", parentDir, e);
                     }
                  }
               }
            }
         }
      }
   }
}
