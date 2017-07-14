package com.ngc.seaside.gradle.plugins.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EclipsePluginsTest {

   @Test
   public void testDoesMakeJarFileNameWith3Digits() throws Throwable {
      assertEquals("com.ngc.seaside.systemdescriptor.model.impl.xtext_1.3.0.jar",
                   EclipsePlugins.makeEclipseCompliantJarFileName(
                         "com.ngc.seaside.systemdescriptor.model.impl.xtext-1.3.0.jar"));
   }

   @Test
   public void testDoesMakeJarFileNameWith3DigitsWithQualifier() throws Throwable {
      assertEquals("com.ngc.seaside.systemdescriptor.model.impl.xtext_1.3.0.SNAPSHOT.jar",
                   EclipsePlugins.makeEclipseCompliantJarFileName(
                         "com.ngc.seaside.systemdescriptor.model.impl.xtext-1.3.0-SNAPSHOT.jar"));
   }

   @Test
   public void testDoesMakeJarFileNameWith2Digits() throws Throwable {
      assertEquals("com.ngc.seaside.systemdescriptor.model.impl.xtext_1.3.0.jar",
                   EclipsePlugins.makeEclipseCompliantJarFileName(
                         "com.ngc.seaside.systemdescriptor.model.impl.xtext-1.3.jar"));
   }

   @Test
   public void testDoesMakeJarFileNameWith2DigitsWithQualifier() throws Throwable {
      assertEquals("com.ngc.seaside.systemdescriptor.model.impl.xtext_1.3.0.SNAPSHOT.jar",
                   EclipsePlugins.makeEclipseCompliantJarFileName(
                         "com.ngc.seaside.systemdescriptor.model.impl.xtext-1.3-SNAPSHOT.jar"));
   }
}
