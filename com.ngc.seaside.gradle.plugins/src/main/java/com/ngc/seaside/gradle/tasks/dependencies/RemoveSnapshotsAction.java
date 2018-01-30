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
                     logger.lifecycle("Removing {} from local maven repository to avoid polluting the repository.",
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
