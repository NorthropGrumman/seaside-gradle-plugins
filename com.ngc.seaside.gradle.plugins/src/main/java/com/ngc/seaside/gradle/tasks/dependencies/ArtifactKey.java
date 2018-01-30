package com.ngc.seaside.gradle.tasks.dependencies;

import java.util.Objects;

public class ArtifactKey {

   private final String groupId;
   private final String artifactId;
   private final String version;

   public ArtifactKey(String groupId, String artifactId, String version) {
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.version = version;
   }

   public String getGroupId() {
      return groupId;
   }

   public String getArtifactId() {
      return artifactId;
   }

   public String getVersion() {
      return version;
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
