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
package com.ngc.seaside.gradle.util;

import org.gradle.api.Project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains utilities for dealing with converting versions to different formats.
 */
public class Versions {

   private final static Pattern THREE_DIGIT_REGEX = Pattern.compile("(\\d+\\.\\d+\\.\\d+)(-(\\w+))?");

   private final static Pattern TWO_DIGIT_REGEX = Pattern.compile("(\\d+\\.\\d+)(-(\\w+))?");

   public static final String VERSION_SUFFIX = "-SNAPSHOT";

   private Versions() {
   }

   /**
    * Transforms a Maven/Gradle like version of the format {@code digit.digit[.optionalDigit][-optionalQualifier]} to
    * version that is compliant with OSGi, which uses the format {@code digit.digit.digit[.optionalQualifier}.
    */
   public static String makeOsgiCompliantVersion(String version) {
      String v;
      Matcher threeDigitMatcher = THREE_DIGIT_REGEX.matcher(version);
      Matcher twoDigitMatcher = TWO_DIGIT_REGEX.matcher(version);

      // Does this version have 3 digits?
      if (threeDigitMatcher.matches()) {
         String digits = threeDigitMatcher.group(1);
         String qualifier = threeDigitMatcher.group(3);
         // Does this version have a qualifier?
         v = qualifier == null ? digits
                               : String.format("%s.%s", digits, qualifier);
      } else if (twoDigitMatcher.matches()) {
         // This version must have 2 digits.
         String digits = twoDigitMatcher.group(1);
         String qualifier = twoDigitMatcher.group(3);
         v = qualifier == null ? String.format("%s.0", digits)
                               : String.format("%s.0.%s", digits, qualifier);
      } else {
         throw new IllegalArgumentException(
               String.format("cannot convert version %s to an OSGi compliant version string!", version));
      }
      return v;
   }

   /**
    * Returns true if the given project's version is a snapshot, false otherwise.
    * 
    * @param project project
    * @return true if the given project's version is a snapshot
    */
   public static boolean isSnapshot(Project project) {
      return project.getVersion().toString().toUpperCase().endsWith(VERSION_SUFFIX);
   }
}
