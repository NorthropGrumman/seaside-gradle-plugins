package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import com.ngc.seaside.gradle.tasks.DefaultTaskAction;
import com.ngc.seaside.gradle.util.GradleUtil;

import org.apache.commons.io.FileUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;
import org.gradle.api.InvalidUserDataException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class CreateCsvDependencyReportAction extends DefaultTaskAction<PopulateMaven2Repository> {

   /**
    * The format for the columns in the file.  This format is {@code groupId, artifactId, version, file, pom,
    * classifier, extension}.
    */
   final static String COLUMN_FORMAT = "%s,%s,%s,%s,%s,%s,%s";

   /**
    * The column headers.
    */
   final static String COLUMN_HEADERS =
         "Group ID, Artifact ID, Version, File, POM File, Classifier (optional), Extension (optional)";

   private Collection<DependencyResult> dependencyResults;

   private Path localRepositoryPath;

   @Override
   public void validate(PopulateMaven2Repository task) throws InvalidUserDataException {
      GradleUtil.checkUserData(!task.isCreateCsvFile() || task.getDependencyInfoCsvFile() != null,
                               "CSV file is not configured!");
   }

   public CreateCsvDependencyReportAction setDependencyResults(
         Collection<DependencyResult> dependencyResults) {
      this.dependencyResults = Preconditions.checkNotNull(dependencyResults, "dependencyResults may not be null!");
      return this;
   }

   static String formatLine(Artifact artifact, Path localMavenRepositoryPath, Path pom, Path csvFile) {
      String line;
      Path artifactRepositoryRelativePath = localMavenRepositoryPath.relativize(artifact.getFile().toPath());
      Path pomRepositoryRelativePath = localMavenRepositoryPath.relativize(pom);
      if ("".equals(artifact.getClassifier())) {
         line = String.format(COLUMN_FORMAT,
                              artifact.getGroupId(),
                              artifact.getArtifactId(),
                              artifact.getVersion(),
                              relativizeToParentOf(csvFile, artifactRepositoryRelativePath),
                              relativizeToParentOf(csvFile, pomRepositoryRelativePath),
                              "",
                              "");
      } else {
         line = String.format(COLUMN_FORMAT,
                              artifact.getGroupId(),
                              artifact.getArtifactId(),
                              artifact.getVersion(),
                              relativizeToParentOf(csvFile, artifactRepositoryRelativePath),
                              relativizeToParentOf(csvFile, pomRepositoryRelativePath),
                              artifact.getClassifier(),
                              artifact.getExtension());
      }
      return line;
   }

   @Override
   protected void doExecute() {
      if (task.isCreateCsvFile()) {
         Preconditions.checkState(dependencyResults != null, "dependencyResults must be set!");
         logger.lifecycle("Creating CSV dependency report.");
         localRepositoryPath = Paths.get(task.getLocalRepository().getUrl());
         createReport();
      }
   }

   private void createReport() {
      Set<String> lines = readExistingReportIfAny();

      for (DependencyResult dependencyResult : dependencyResults) {
         for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
            Optional<Path> pom = findPom(artifactResult);
            if (pom.isPresent()) {
               lines.add(formatLine(artifactResult.getArtifact(),
                                    localRepositoryPath,
                                    pom.get(),
                                    task.getDependencyInfoCsvFile().toPath()));
            } else {
               String prettyGave = String.format(
                     "%s:%s:%s%s@%s",
                     artifactResult.getArtifact().getGroupId(),
                     artifactResult.getArtifact().getArtifactId(),
                     artifactResult.getArtifact().getVersion(),
                     artifactResult.getArtifact().getClassifier() == null ? "" : ":" + artifactResult.getArtifact()
                           .getClassifier(),
                     artifactResult.getArtifact().getExtension());
               logger.warn("POM file not found for {}, artifact will not be included in CSV report.", prettyGave);
            }
         }
      }

      writeLines(lines);
   }

   private Set<String> readExistingReportIfAny() {
      // Use a tree set which sorts the output.
      Set<String> lines = new TreeSet<>();

      Path csvFile = task.getDependencyInfoCsvFile().toPath();
      if (Files.isRegularFile(csvFile)) {
         try {
            lines.addAll(Files.readAllLines(csvFile));
            // Remove the previous header because we will right it again.
            lines.remove(COLUMN_HEADERS);
         } catch (IOException e) {
            logger.error("Unexpected exception while reading existing CSV dependency report; the current report will"
                         + " be overwritten.",
                         e,
                         csvFile);
         }
      }

      return lines;
   }

   private void writeLines(Set<String> lines) {
      // Create parent directories if needed.
      File dir = task.getDependencyInfoCsvFile().getParentFile();
      if (dir != null && !dir.isDirectory()) {
         dir.mkdirs();
      }

      try {
         Files.write(task.getDependencyInfoCsvFile().toPath(),
                     Collections.singleton(COLUMN_HEADERS),
                     StandardOpenOption.WRITE,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING);
         Files.write(task.getDependencyInfoCsvFile().toPath(),
                     lines,
                     StandardOpenOption.WRITE,
                     StandardOpenOption.APPEND);
      } catch (IOException e) {
         logger.error("Unexpected error while creating dependency report at {}.", e, task.getDependencyInfoCsvFile());
      }
   }

   private static Path relativizeToParentOf(Path path, Path other) {
      Path relative = other;
      if (path.getParent() != null) {
         relative = path.getParent().relativize(other);
      }
      return relative;
   }

   private static Optional<Path> findPom(ArtifactResult artifactResult) {
      Optional<Path> path = Optional.empty();
      File artifact = artifactResult.getArtifact().getFile();
      if (artifact.getParentFile() != null) {
         Path pom = FileUtils.listFiles(artifact.getParentFile(), new String[]{"pom"}, false)
               .stream()
               .map(File::toPath)
               .findAny()
               .orElse(null);
         path = Optional.ofNullable(pom);
      }
      return path;
   }
}
