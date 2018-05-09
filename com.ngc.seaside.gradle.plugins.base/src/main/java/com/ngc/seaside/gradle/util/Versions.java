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
