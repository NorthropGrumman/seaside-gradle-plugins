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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class CreateDependencyReportAction extends DefaultTaskAction<PopulateMaven2Repository> {

   /**
    * The column headers.
    */
   final static String COLUMN_HEADERS =
         "GroupID\tArtifactID\tVersion\tpomFile\tfile\tpackaging\tclassifier\tfiles\tclassifiers\ttypes";

   /**
    * The character that delimits fields.
    */
   private final static char FIELD_SEPARATOR = '\t';

   /**
    * The value to use if a field is blank.
    */
   private final static String DEFAULT_EMPTY_FIELD = " ";

   private ArtifactResultStore store;

   @Override
   public void validate(PopulateMaven2Repository task) throws InvalidUserDataException {
      GradleUtil.checkUserData(!task.isCreateDependencyReportFile() || task.getDependencyInfoReportFile() != null,
                               "dependency report file is not configured!");
   }

   public CreateDependencyReportAction setStore(ArtifactResultStore store) {
      this.store = Preconditions.checkNotNull(store, "store may not be null!");
      return this;
   }

   static String formatLine(ArtifactResult artifactResult,
                            ArtifactResultStore store,
                            Path outputFile) {
      Path pom = relativizeToParentOf(outputFile, store.getRelativePathToPom(artifactResult));
      Path file = relativizeToParentOf(outputFile, store.getRelativePathToMainArtifact(artifactResult));
      Stream<String> files = store.getRelativePathsToOtherClassifiers(artifactResult)
            .stream()
            .map(p -> relativizeToParentOf(outputFile, p).toString());

      return artifactResult.getArtifact().getGroupId() + FIELD_SEPARATOR
             + artifactResult.getArtifact().getArtifactId() + FIELD_SEPARATOR
             + artifactResult.getArtifact().getVersion() + FIELD_SEPARATOR
             + pom + FIELD_SEPARATOR
             + file + FIELD_SEPARATOR
             + neverEmpty(store.getMainExtension(artifactResult)) + FIELD_SEPARATOR
             + neverEmpty(store.getMainClassifier(artifactResult)) + FIELD_SEPARATOR
             + neverEmpty(String.join(",", (Iterable<String>) files::iterator)) + FIELD_SEPARATOR
             + neverEmpty(String.join(",", store.getOtherClassifiers(artifactResult))) + FIELD_SEPARATOR
             + neverEmpty(String.join(",", store.getOtherExtensions(artifactResult)));
   }

   @Override
   protected void doExecute() {
      if (task.isCreateDependencyReportFile()) {
         Preconditions.checkState(store != null, "store must be set!");
         createReport();
      }
   }

   private void createReport() {
      Set<String> lines = readExistingReportIfAny();

      Path outputFile = task.getDependencyInfoReportFile().toPath();
      for (ArtifactResult mainResult : store.getMainResults()) {
         lines.add(formatLine(mainResult, store, outputFile));
      }

      writeLines(lines);
   }

   private Set<String> readExistingReportIfAny() {
      // Use a tree set which sorts the output.
      Set<String> lines = new TreeSet<>();

      Path csvFile = task.getDependencyInfoReportFile().toPath();
      if (Files.isRegularFile(csvFile)) {
         logger.lifecycle("Updating dependency report {}.", csvFile.toAbsolutePath());
         try {
            lines.addAll(Files.readAllLines(csvFile));
            // Remove the previous header because we will write it again.
            lines.remove(COLUMN_HEADERS);
         } catch (IOException e) {
            logger.error("Unexpected exception while reading existing dependency report; the current report will"
                         + " be overwritten.",
                         e,
                         csvFile);
         }
      } else {
         logger.lifecycle("Creating dependency report {}.", csvFile.toAbsolutePath());
      }

      return lines;
   }

   private void writeLines(Collection<String> lines) {
      // Create parent directories if needed.
      File dir = task.getDependencyInfoReportFile().getParentFile();
      if (dir != null && !dir.isDirectory()) {
         dir.mkdirs();
      }

      lines = scrubLines(lines);

      try {
         Files.write(task.getDependencyInfoReportFile().toPath(),
                     Collections.singleton(COLUMN_HEADERS),
                     StandardOpenOption.WRITE,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING);
         Files.write(task.getDependencyInfoReportFile().toPath(),
                     lines,
                     StandardOpenOption.WRITE,
                     StandardOpenOption.APPEND);
      } catch (IOException e) {
         logger.error("Unexpected error while creating dependency report at {}.", task.getDependencyInfoReportFile(), e);
      }
   }

   private static Collection<String> scrubLines(Collection<String> lines) {
      List<String> list = new ArrayList<>(lines);
      List<String> scrubed = new ArrayList<>();

      for(int i = 0; i < list.size(); i++) {
         String line = list.get(i);
         String next = i < list.size() - 1 ? list.get(i + 1) : null;
         if(next == null || !isDuplicateLine(line, next)) {
            scrubed.add(line);
         }
      }

      return scrubed;
   }

   private static boolean isDuplicateLine(String line, String next) {
      String[] lineFields = line.split(String.valueOf(FIELD_SEPARATOR));
      String[] nextFields = next.split(String.valueOf(FIELD_SEPARATOR));
      return lineFields[0].equals(nextFields[0]) // group
            && lineFields[1].equals(nextFields[1]) // artifact
            && lineFields[2].equals(nextFields[2]) // version
            && lineFields[3].equals(nextFields[3]); // pom file
   }

   private static Path relativizeToParentOf(Path path, Path other) {
      Path relative = other;
      if (path.getParent() != null) {
         relative = path.getParent().relativize(other);
      }
      return relative;
   }

   private static String neverEmpty(String value) {
      return value.trim().isEmpty() ? DEFAULT_EMPTY_FIELD : value;
   }

}
