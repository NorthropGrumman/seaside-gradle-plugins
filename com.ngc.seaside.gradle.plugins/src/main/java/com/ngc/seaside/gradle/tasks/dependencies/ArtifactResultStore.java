package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import org.eclipse.aether.resolution.ArtifactResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArtifactResultStore {

   private final Map<ArtifactKey, ArtifactResultRecord> mainResults = new HashMap<>();
   private final Path localRepositoryPath;

   public ArtifactResultStore(Path localRepositoryPath) {
      this.localRepositoryPath = localRepositoryPath;
   }

   public ArtifactResultStore addResult(ArtifactResult result, Path pom) {
      Preconditions.checkNotNull(result, "result may not be null!");
      Preconditions.checkNotNull(pom, "pom may not be null!");

      ArtifactResultRecord record = mainResults.computeIfAbsent(key(result),
                                                                k -> new ArtifactResultRecord(pom));
      if (isMainArtifact(result)) {
         record.mainArtifact = result;
      } else {
         record.additionalClassifiers.add(result);
      }

      return this;
   }

   public Collection<ArtifactResult> getMainResults() {
      return mainResults.values()
            .stream()
            .filter(r -> r.mainArtifact != null)
            .map(r -> r.mainArtifact)
            .collect(Collectors.toList());
   }

   public Path getRelativePathToMainArtifact(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return repositoryRelativePath(result);
   }

   public Collection<Path> getRelativePathsToOtherClassifiers(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD)
            .additionalClassifiers
            .stream()
            .map(this::repositoryRelativePath)
            .collect(Collectors.toList());
   }

   public Path getRelativePathToPom(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      Path pom = mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD).pom;
      return pom == null ? null : localRepositoryPath.relativize(pom);
   }

   public Collection<String> getOtherClassifiers(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD)
            .additionalClassifiers
            .stream()
            .map(r -> r.getArtifact().getClassifier())
            .collect(Collectors.toList());
   }

   public Collection<String> getOtherExtensions(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD)
            .additionalClassifiers
            .stream()
            .map(r -> r.getArtifact().getExtension())
            .collect(Collectors.toList());
   }

   public boolean hasOtherClassifiers(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return !mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD)
            .additionalClassifiers
            .isEmpty();
   }

   private Path repositoryRelativePath(ArtifactResult result) {
      return localRepositoryPath.relativize(result.getArtifact().getFile().toPath());
   }

   private static boolean isMainArtifact(ArtifactResult result) {
      return "".equals(result.getArtifact().getClassifier());
   }

   private static ArtifactKey key(ArtifactResult result) {
      return new ArtifactKey(result.getArtifact().getGroupId(),
                             result.getArtifact().getArtifactId(),
                             result.getArtifact().getVersion());
   }

   private static class ArtifactKey {

      final String groupId;
      final String artifactId;
      final String version;

      ArtifactKey(String groupId, String artifactId, String version) {
         this.groupId = groupId;
         this.artifactId = artifactId;
         this.version = version;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }
         if (!(o instanceof ArtifactKey)) {
            return false;
         }
         ArtifactKey that = (ArtifactKey) o;
         return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version);
      }

      @Override
      public int hashCode() {
         return Objects.hash(groupId, artifactId, version);
      }
   }

   private static class ArtifactResultRecord {

      final static ArtifactResultRecord EMPTY_RECORD = new ArtifactResultRecord(null);

      final Collection<ArtifactResult> additionalClassifiers = new ArrayList<>();
      final Path pom;
      ArtifactResult mainArtifact;

      ArtifactResultRecord(Path pom) {
         this.pom = pom;
      }
   }
}
