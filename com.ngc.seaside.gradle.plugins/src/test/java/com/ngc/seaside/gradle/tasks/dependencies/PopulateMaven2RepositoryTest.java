package com.ngc.seaside.gradle.tasks.dependencies;

import com.ngc.seaside.gradle.util.test.GradleMocks;
import com.ngc.seaside.gradle.util.test.TaskBuilder;

import org.gradle.api.internal.project.ProjectInternal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PopulateMaven2RepositoryTest {

   private PopulateMaven2Repository task;

   private ProjectInternal project = GradleMocks.newProjectMock();

   @Before
   public void setup() {
//      task = new TaskBuilder<>(PopulateMaven2Repository.class)
//            //.setSupplier({ new MockReturningTask(repositoryFactory) })
//            .setProject(project)
//            .create();
   }

   @Test
   public void testDoesCallActions() throws Throwable
   {
      //task.populateRepository();
   }
}
