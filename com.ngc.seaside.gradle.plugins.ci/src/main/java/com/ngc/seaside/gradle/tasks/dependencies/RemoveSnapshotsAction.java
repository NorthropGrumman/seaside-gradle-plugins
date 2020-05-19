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
