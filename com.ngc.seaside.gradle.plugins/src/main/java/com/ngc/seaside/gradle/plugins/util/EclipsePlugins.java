package com.ngc.seaside.gradle.plugins.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EclipsePlugins {

   private final static Pattern THREE_DIGIT_REGEX =
         Pattern.compile("([\\w|.|-]+)-(\\d+\\.\\d+\\.\\d+)(-(\\w+))?\\.jar");

   private final static Pattern TWO_DIGIT_REGEX =
         Pattern.compile("([\\w|.|-]+)-(\\d+\\.\\d+)(-(\\w+))?\\.jar");

   private EclipsePlugins() {
   }

   public static String makeEclipseCompliantJarFileName(String jarFileName) {
      String name;
      Matcher threeDigitMatcher = THREE_DIGIT_REGEX.matcher(jarFileName);
      Matcher twoDigitMatcher = TWO_DIGIT_REGEX.matcher(jarFileName);

      // Does this filename have a version with 3 digits?
      if (threeDigitMatcher.matches()) {
         String prefix = threeDigitMatcher.group(1);
         String digits = threeDigitMatcher.group(2);
         String qualifier = threeDigitMatcher.group(4);
         // Does the version have a qualifier?
         name = qualifier == null ? String.format("%s_%s.jar", prefix, digits)
                                  : String.format("%s_%s.%s.jar", prefix, digits, qualifier);
      } else if (twoDigitMatcher.matches()) {
         String prefix = twoDigitMatcher.group(1);
         String digits = twoDigitMatcher.group(2);
         String qualifier = twoDigitMatcher.group(4);
         // Does the version have a qualifier?
         name = qualifier == null ? String.format("%s_%s.0.jar", prefix, digits)
                                  : String.format("%s_%s.0.%s.jar", prefix, digits, qualifier);
      } else {
         throw new IllegalArgumentException("cannot convert JAR file name "
                                            + jarFileName
                                            + " to an Eclipse plugin file name string!");
      }

      return name;
   }
}
