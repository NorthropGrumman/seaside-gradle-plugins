package com.ngc.seaside.gradle.plugins.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionsTest {

   @Test
   public void testDoesMakeOsgiVersionWith3Numbers() throws Throwable {
      assertEquals("1.0.0",
                   Versions.makeOsgiCompliantVersion("1.0.0"));
   }

   @Test
   public void testDoesMakeOsgiVersionWith3NumbersAndQualifier() throws Throwable {
      assertEquals("1.0.0.SNAPSHOT",
                   Versions.makeOsgiCompliantVersion("1.0.0-SNAPSHOT"));
      assertEquals("1.0.0.RC1",
                   Versions.makeOsgiCompliantVersion("1.0.0-RC1"));
   }

   @Test
   public void testDoesMakeOsgiVersionWith2Numbers() throws Throwable {
      assertEquals("1.0.0",
                   Versions.makeOsgiCompliantVersion("1.0"));
   }

   @Test
   public void testDoesMakeOsgiVersionWith2NumbersAndQualifier() throws Throwable {
      assertEquals("1.0.0.SNAPSHOT",
                   Versions.makeOsgiCompliantVersion("1.0-SNAPSHOT"));
      assertEquals("1.0.0.RC1",
                   Versions.makeOsgiCompliantVersion("1.0-RC1"));
   }
}
