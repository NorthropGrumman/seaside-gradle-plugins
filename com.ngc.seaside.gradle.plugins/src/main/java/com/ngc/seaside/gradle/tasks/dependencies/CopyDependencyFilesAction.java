package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import com.ngc.seaside.gradle.tasks.DefaultTaskAction;
import com.ngc.seaside.gradle.util.GradleUtil;

import org.apache.commons.io.FileUtils;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;
import org.gradle.api.InvalidUserDataException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Copies files from a local maven repository to directory.
 */
public class CopyDependencyFilesAction extends DefaultTaskAction<PopulateMaven2Repository> {

   /**
    * The resolved dependencies.
    */
   private Collection<DependencyResult> dependencyResults;

   @Override
   public void validate(PopulateMaven2Repository task) throws InvalidUserDataException {
      GradleUtil.checkUserData(task.isPopulateLocalRepoOnly() || task.getOutputDirectory() != null,
                               "outputDirectory must be set if populateLocalRe2poOnly is false!");
   }

   /**
    * Sets the dependency results which will be used to copy files.
    */
   public CopyDependencyFilesAction setDependencyResults(
         Collection<DependencyResult> dependencyResults) {
      this.dependencyResults = Preconditions.checkNotNull(dependencyResults, "dependencyResults may not be null!");
      return this;
   }

   @Override
   protected void doExecute() {
      if (!task.isPopulateLocalRepoOnly()) {
         Preconditions.checkState(dependencyResults != null, "dependencyResults must be set!");

         logger.lifecycle("Copying files to output directory {}.", task.getOutputDirectory());
         for (DependencyResult result : dependencyResults) {
            for (ArtifactResult localArtifact : result.getArtifactResults()) {
               // If the dependency has a scope of "system", the file path may be relative.
               // If that is the case, we want to ignore the file.
               if (!isArtifactSystemScoped(localArtifact)) {
                  File artifact = localArtifact.getArtifact().getFile();
                  copyFileToOutputDirectory(artifact.toPath());

                  // Find and copy any POM files directly since the API does not expose POMs.
                  if (artifact.getParentFile() != null) {
                     FileUtils.listFiles(artifact.getParentFile(), new String[]{"pom"}, false)
                           .forEach(f -> copyFileToOutputDirectory(f.toPath()));
                  }
               }
            }
         }
      }
   }

   /**
    * Copies a file that resides in the local repository to the output directory, maintaining the directory structure of
    * the file relative to the local repository location.  IE, this keeps the groupId/artifactId/version/ directory
    * structure for Maven M2 layouts.
    *
    * @return the relative path to the file inside the directory
    */
   private Path copyFileToOutputDirectory(Path path) {
      // The path to the artifact inside the local repository.
      path = path.toAbsolutePath();
      // The path to the local repository.
      Path localRepo = Paths.get(task.getLocalRepository().getUrl()).toAbsolutePath();
      // The path to the artifact inside the local repository that is relative to the local repository.  This
      // gives us the path that starts the group ID, then the artifact ID, then the version, etc.
      Path relativeArtifactPath = localRepo.relativize(path);
      // The destination file.  This is the relative path resolved against the output directory.  Only copy the
      // file if needed.
      Path dest = task.getOutputDirectory().toPath().resolve(relativeArtifactPath);

      if (!Files.exists(dest)) {
         // Create the directory structure if needed.
         Path parent = dest.getParent();
         try {
            if (parent != null) {
               // Note this is safe even if the directory already exists.
               Files.createDirectories(parent);
            }
            Files.copy(path, dest);
         } catch (IOException e) {
            logger.error("Unexpected error while copying {} to {}.", path, dest, e);
         }
      }

      return relativeArtifactPath;
   }

   /**
    * Returns true if the given artifact is system scoped.
    */
   private static boolean isArtifactSystemScoped(ArtifactResult artifactResult) {
      return artifactResult.getArtifact().getFile().toString().contains("..");
   }
}
