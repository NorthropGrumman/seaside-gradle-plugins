package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import com.ngc.seaside.gradle.tasks.DefaultTaskAction;
import com.ngc.seaside.gradle.util.GradleUtil;

import org.eclipse.aether.resolution.ArtifactResult;
import org.gradle.api.InvalidUserDataException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class CreateCsvDependencyReportAction extends DefaultTaskAction<PopulateMaven2Repository> {

   /**
    * The column headers.
    */
   final static String COLUMN_HEADERS =
         "Group ID, Artifact ID, Version, File, POM File, Classifier (optional), Extension (optional)";

   /**
    * The format for the columns in the file.  This format is {@code groupId, artifactId, version, file, pom,
    * classifier, extension}.
    */
   private final static String COLUMN_FORMAT = "%s,%s,%s,%s,%s,%s,%s";


   private ArtifactResultStore store;

   @Override
   public void validate(PopulateMaven2Repository task) throws InvalidUserDataException {
      GradleUtil.checkUserData(!task.isCreateCsvFile() || task.getDependencyInfoCsvFile() != null,
                               "CSV file is not configured!");
   }

   public CreateCsvDependencyReportAction setStore(ArtifactResultStore store) {
      this.store = Preconditions.checkNotNull(store, "store may not be null!");
      return this;
   }

   @Override
   protected void doExecute() {
      if (task.isCreateCsvFile()) {
         Preconditions.checkState(store != null, "store must be set!");
         logger.lifecycle("Creating CSV dependency report.");
         createReport();
      }
   }

   static String formatLineForMainArtifact(ArtifactResult artifactResult,
                                           ArtifactResultStore store,
                                           Path csvOutputFile) {
      return String.format(COLUMN_FORMAT,
                           artifactResult.getArtifact().getGroupId(),
                           artifactResult.getArtifact().getArtifactId(),
                           artifactResult.getArtifact().getVersion(),
                           relativizeToParentOf(csvOutputFile, store.getRelativePathToMainArtifact(artifactResult)),
                           relativizeToParentOf(csvOutputFile, store.getRelativePathToPom(artifactResult)),
                           "", // no classifier for main artifact
                           ""); // no extension for main artifact
   }

   static String formatLineForClassifier(ArtifactResult artifactResult,
                                         ArtifactResultStore store,
                                         String classifier,
                                         String extension,
                                         Path pathToArtifact,
                                         Path csvOutputFile) {
      return String.format(COLUMN_FORMAT,
                           artifactResult.getArtifact().getGroupId(),
                           artifactResult.getArtifact().getArtifactId(),
                           artifactResult.getArtifact().getVersion(),
                           relativizeToParentOf(csvOutputFile, pathToArtifact),
                           relativizeToParentOf(csvOutputFile, store.getRelativePathToPom(artifactResult)),
                           classifier,
                           extension);
   }

   private void createReport() {
      Set<String> lines = readExistingReportIfAny();

      Path csvFile = task.getDependencyInfoCsvFile().toPath();
      for (ArtifactResult mainResult : store.getMainResults()) {
         // Add the line for the main artifact.
         lines.add(formatLineForMainArtifact(mainResult, store, csvFile));
         // Add the lines for the extra classifiers (if any).
         if (store.hasOtherClassifiers(mainResult)) {
            Iterator<String> classifiers = store.getOtherClassifiers(mainResult).iterator();
            Iterator<String> extensions = store.getOtherExtensions(mainResult).iterator();
            Iterator<Path> paths = store.getRelativePathsToOtherClassifiers(mainResult).iterator();

            while (classifiers.hasNext()) {
               lines.add(formatLineForClassifier(mainResult,
                                                 store,
                                                 classifiers.next(),
                                                 extensions.next(),
                                                 paths.next(),
                                                 csvFile));
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
}
