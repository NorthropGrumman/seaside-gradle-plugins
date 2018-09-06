/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.util;

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
