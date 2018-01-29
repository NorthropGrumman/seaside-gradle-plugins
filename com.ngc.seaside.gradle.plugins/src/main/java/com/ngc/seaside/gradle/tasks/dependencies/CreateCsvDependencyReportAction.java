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
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class CreateCsvDependencyReportAction extends DefaultTaskAction<PopulateMaven2Repository> {

   /**
    * The column headers.
    */
   final static String COLUMN_HEADERS =
         "Group ID\tArtifact ID\tVersion\tPOM File\tFile\tFiles (optional)\tClassifiers  (optional)\tTypes (optional)";

   /**
    * The character that delimits fields.
    */
   private final static char FIELD_SEPARATOR = '\t';

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

   static String formatLine(ArtifactResult artifactResult,
                            ArtifactResultStore store,
                            Path csvOutputFile) {
      Path pom = relativizeToParentOf(csvOutputFile, store.getRelativePathToPom(artifactResult));
      Path file = relativizeToParentOf(csvOutputFile, store.getRelativePathToMainArtifact(artifactResult));
      Stream<String> files = store.getRelativePathsToOtherClassifiers(artifactResult)
            .stream()
            .map(p -> relativizeToParentOf(csvOutputFile, p).toString());

      return artifactResult.getArtifact().getGroupId() + FIELD_SEPARATOR
             + artifactResult.getArtifact().getArtifactId() + FIELD_SEPARATOR
             + artifactResult.getArtifact().getVersion() + FIELD_SEPARATOR
             + pom + FIELD_SEPARATOR
             + file + FIELD_SEPARATOR
             + String.join(",", (Iterable<String>) files::iterator) + FIELD_SEPARATOR
             + String.join(",", store.getOtherClassifiers(artifactResult)) + FIELD_SEPARATOR
             + String.join(",", store.getOtherExtensions(artifactResult));
   }

   @Override
   protected void doExecute() {
      if (task.isCreateCsvFile()) {
         Preconditions.checkState(store != null, "store must be set!");
         createReport();
      }
   }

   private void createReport() {
      Set<String> lines = readExistingReportIfAny();

      Path csvFile = task.getDependencyInfoCsvFile().toPath();
      for (ArtifactResult mainResult : store.getMainResults()) {
         lines.add(formatLine(mainResult, store, csvFile));
      }

      writeLines(lines);
   }

   private Set<String> readExistingReportIfAny() {
      // Use a tree set which sorts the output.
      Set<String> lines = new TreeSet<>();

      Path csvFile = task.getDependencyInfoCsvFile().toPath();
      if (Files.isRegularFile(csvFile)) {
         logger.lifecycle("Updating CSV dependency report {}.", csvFile.toAbsolutePath());
         try {
            lines.addAll(Files.readAllLines(csvFile));
            // Remove the previous header because we will write it again.
            lines.remove(COLUMN_HEADERS);
         } catch (IOException e) {
            logger.error("Unexpected exception while reading existing CSV dependency report; the current report will"
                         + " be overwritten.",
                         e,
                         csvFile);
         }
      } else {
         logger.lifecycle("Creating CSV dependency report {}.", csvFile.toAbsolutePath());
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
