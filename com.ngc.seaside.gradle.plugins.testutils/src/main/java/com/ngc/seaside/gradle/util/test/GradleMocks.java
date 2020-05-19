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
package com.ngc.seaside.gradle.util.test;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.util.Path;
import org.mockito.stubbing.Answer;

import java.io.PrintStream;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Contains factory methods for creating mocked Gradle API elements.
 */
public class GradleMocks {

   private GradleMocks() {
   }

   /**
    * Creates a new Gradle {@code Project} that contains a mocked service registry and configuration container.
    */
   public static ProjectInternal newProjectMock() {
      // We have to reference the internal Gradle API ProjectInternal because that is what is needed to
      // create tasks outside of the DSL.
      ServiceRegistry registry = mock(ServiceRegistry.class);

      Path projectPath = Path.ROOT;

      ProjectInternal project = mock(ProjectInternal.class);
      when(project.getServices()).thenReturn(registry);
      when(project.getProjectPath()).thenReturn(projectPath);
      when(project.getIdentityPath()).thenReturn(projectPath);

      GradleInternal gradle = mock(GradleInternal.class);
      when(project.getGradle()).thenReturn(gradle);
      when(project.getGradle().getIdentityPath()).thenReturn(projectPath);

      ConfigurationContainer configs = mock(ConfigurationContainer.class);
      when(project.getConfigurations()).thenReturn(configs);

      return project;
   }

   public static Configuration newConfiguration(String name) {
      Configuration config = mock(Configuration.class);
      when(config.getName()).thenReturn(name);
      when(config.getDependencies()).thenReturn(new MockedDependencySet());
      return config;
   }

   public static Logger mockedStdoutLogger() {
      return mockedLogger(System.out);
   }

   public static Logger mockedLogger(PrintStream stream) {
      Logger logger = mock(Logger.class);

      Answer<Object> answer = invocation -> {
         if (invocation.getArguments().length == 1) {
            stream.println(invocation.getArgument(0).toString());
         } else if (invocation.getArguments().length > 1) {
            Object[] params = Arrays.copyOfRange(invocation.getArguments(), 1, invocation.getArguments().length);
            stream.println(String.format(invocation.getArgument(0).toString().replace("{}", "%s"), params));
         }
         return null;
      };

      doAnswer(answer).when(logger).error(anyString(), (Object[]) any());
      doAnswer(answer).when(logger).lifecycle(anyString(), (Object[]) any());
      doAnswer(answer).when(logger).warn(anyString(), (Object[]) any());
      doAnswer(answer).when(logger).info(anyString(), (Object[]) any());
      doAnswer(answer).when(logger).debug(anyString(), (Object[]) any());
      doAnswer(answer).when(logger).trace(anyString(), (Object[]) any());
      doAnswer(answer).when(logger).error(anyString());
      doAnswer(answer).when(logger).lifecycle(anyString());
      doAnswer(answer).when(logger).warn(anyString());
      doAnswer(answer).when(logger).info(anyString());
      doAnswer(answer).when(logger).debug(anyString());
      doAnswer(answer).when(logger).trace(anyString());

      return logger;
   }

   private static class MockedDependencySet extends DefaultDomainObjectSet<Dependency> implements DependencySet {

      private MockedDependencySet() {
         super(Dependency.class);
      }

      @Override
      public TaskDependency getBuildDependencies() {
         throw new UnsupportedOperationException("not implemented");
      }
   }
}
