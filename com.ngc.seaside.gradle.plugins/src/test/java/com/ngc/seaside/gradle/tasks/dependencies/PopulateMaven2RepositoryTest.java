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

      verify(resolveDependenciesAction).execute(task);
      verify(copyDependencyFilesAction).setDependencyResults(results);
      verify(copyDependencyFilesAction).execute(task);

      verify(createDependencyReportAction).setStore(any(ArtifactResultStore.class));
      verify(createDependencyReportAction).execute(task);
   }
}
