package com.ngc.seaside.gradle.util.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.util.Path;
import org.mockito.stubbing.Answer;

import java.io.PrintStream;
import java.util.Arrays;

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
