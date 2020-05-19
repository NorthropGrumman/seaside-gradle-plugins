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

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import java.io.File;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A factory for creating mocks of the Aether implementation of the Maven Wagon API.
 */
public class AetherMocks {

   private AetherMocks() {
   }

   public static Dependency newDependency(String group, String artifact, String version) {
      Dependency dependency = mock(Dependency.class);
      when(dependency.getGroup()).thenReturn(group);
      when(dependency.getName()).thenReturn(artifact);
      when(dependency.getVersion()).thenReturn(version);
      return dependency;
   }

   public static MavenArtifactRepository newLocalMavenRepo(File directory) {
      MavenArtifactRepository repo = mock(MavenArtifactRepository.class);
      when(repo.getUrl()).thenReturn(directory.toURI());
      return repo;
   }

   public static DependencyResult newDependencyResult() {
      return newDependencyResult(null);
   }

   public static DependencyResult newDependencyResult(File file) {
      return newDependencyResult(null, null, null, null, null, file);
   }

   public static DependencyResult newDependencyResult(String groupId,
                                                      String artifactId,
                                                      String version,
                                                      String classifier,
                                                      String extension,
                                                      File file) {
      DependencyResult dependencyResult = new DependencyResult(new DependencyRequest());
      dependencyResult.setArtifactResults(Collections.singletonList(newArtifactResult(groupId,
                                                                                      artifactId,
                                                                                      version,
                                                                                      classifier,
                                                                                      extension,
                                                                                      file)));
      return dependencyResult;
   }

   public static ArtifactResult newArtifactResult(String groupId,
                                                  String artifactId,
                                                  String version,
                                                  String classifier,
                                                  String extension,
                                                  File file) {
      Artifact artifact = mock(Artifact.class);
      when(artifact.getFile()).thenReturn(file);
      when(artifact.getGroupId()).thenReturn(groupId);
      when(artifact.getArtifactId()).thenReturn(artifactId);
      when(artifact.getVersion()).thenReturn(version);
      when(artifact.getClassifier()).thenReturn(classifier == null ? "" : classifier);
      when(artifact.getExtension()).thenReturn(extension);

      ArtifactResult artifactResult = new ArtifactResult(new ArtifactRequest());
      artifactResult.setArtifact(artifact);
      return artifactResult;
   }

   public static DependencyResolutionException newNotFoundException() {
      ArtifactResolutionException cause = new ArtifactResolutionException(Collections.emptyList());
      return new DependencyResolutionException(null, cause);
   }
}
