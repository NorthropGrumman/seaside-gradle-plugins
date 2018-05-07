package com.ngc.seaside.gradle.plugins.checkstyle

import com.ngc.seaside.gradle.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SeasideCheckstylePluginTest {

   private Project project

   @Before
   void before() {
      project = ProjectBuilder.builder().build()
      project.plugins.apply(SeasideCheckstylePlugin)
   }

   @Test
   void doesApplyPlugin() {
      TaskResolver resolver = new TaskResolver(project)
      Assert.assertNotNull(resolver.findTask("checkstyleMain"))
   }

}
