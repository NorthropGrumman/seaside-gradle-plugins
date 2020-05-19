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

import com.ngc.seaside.gradle.util.test.GradleMocks;
import com.ngc.seaside.gradle.util.test.TaskBuilder;

import org.eclipse.aether.resolution.DependencyResult;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.internal.project.ProjectInternal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PopulateMaven2RepositoryTest {

   private PopulateMaven2Repository task;

   private ProjectInternal project = GradleMocks.newProjectMock();

   @Mock
   private MavenArtifactRepository localRepository;

   @Mock
   private ResolveDependenciesAction resolveDependenciesAction;

   @Mock
   private CopyDependencyFilesAction copyDependencyFilesAction;

   @Mock
   private CreateDependencyReportAction createDependencyReportAction;

   @Mock
   private CreateDeploymentScriptAction createDeploymentScriptAction;

   @Mock
   private BaseRepositoryFactory baseRepositoryFactory;

   @Before
   public void setup() {
      when(localRepository.getUrl()).thenReturn(Paths.get("m2repo").toUri());

      task = new TaskBuilder<>(PopulateMaven2Repository.class)
            .setSupplier(() -> new PopulateMaven2Repository(baseRepositoryFactory) {
               @Override
               protected ResolveDependenciesAction newResolveDependenciesAction() {
                  return resolveDependenciesAction;
               }

               @Override
               protected CopyDependencyFilesAction newCopyDependencyFilesAction() {
                  return copyDependencyFilesAction;
               }

               @Override
               protected CreateDependencyReportAction newCreateDependencyReportAction() {
                  return createDependencyReportAction;
               }

               @Override
               protected CreateDeploymentScriptAction newCreateDeploymentScriptAction() {
                  return createDeploymentScriptAction;
               }
            })
            .setProject(project)
            .create();

      task.setOutputDirectory(new File("output"));
      task.setLocalRepository(localRepository);
   }

   @Test
   public void testDoesCallActions() {
      Collection<DependencyResult> results = Collections.emptyList();
      when(resolveDependenciesAction.getDependencyResults()).thenReturn(results);

      task.populateRepository();

      verify(resolveDependenciesAction).validate(task);
      verify(copyDependencyFilesAction).validate(task);
      verify(createDependencyReportAction).validate(task);
      verify(createDeploymentScriptAction).validate(task);

      verify(resolveDependenciesAction).execute(task);
      verify(copyDependencyFilesAction).setDependencyResults(results);
      verify(copyDependencyFilesAction).execute(task);

      verify(createDependencyReportAction).setStore(any(ArtifactResultStore.class));
      verify(createDependencyReportAction).execute(task);
      verify(createDeploymentScriptAction).execute(task);
   }
}
