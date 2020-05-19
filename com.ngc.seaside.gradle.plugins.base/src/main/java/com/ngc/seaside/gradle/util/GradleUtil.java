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

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.ProjectConfigurationException;

import java.util.Map;

/**
 * Contains various utilities for dealing with Gradle projects.
 */
public class GradleUtil {

   private final static String MISSING_PROPERTY_ERROR_MESSAGE =
         "the property '%s' is not set!  Please ensure this property is set.  These type of properties"
         + " can be set in $GRADLE_USER_HOME/gradle.properties.  Note that $GRADLE_USER_HOME is not necessarily"
         + " the directory where Gradle is installed.  If $GRADLE_USER_HOME is not set, gradle.properties can"
         + " usually be found in $USER/.gradle/.  You can check which properties are set by running"
         + " 'gradle properties'.";

   private final static String
         MISSING_SYSTEM_PROPERTY_ERROR_MESSAGE =
         "the system property '%s' is not set!  Please ensure this property is set.  These types of properties can be"
         + " set in $GRADLE_USER_HOME/gradle.properties.  Prefix the name of the property with 'systemProp.'.  In this"
         + " case, use 'systemProp.%s'.  Note that $GRADLE_USER_HOME is not necessarily"
         + " the directory where Gradle is installed.  If $GRADLE_USER_HOME is not set, gradle.properties can"
         + " usually be found in $USER/.gradle/.  You can check which properties are set by running"
         + " 'gradle properties'.";

   private GradleUtil() {
   }

   /**
    * Requires that all the given properties are set, throwing a {@code ProjectConfigurationException} otherwise.
    *
    * @param properties    the properties set in the project
    * @param propertyName  the name of the property that must be set
    * @param propertyNames additional names of properties that must be set (optional)
    */
   public static void requireProperties(Map<String, ?> properties, String propertyName, String... propertyNames) {
      if (!properties.containsKey(propertyName)) {
         throw new ProjectConfigurationException(String.format(MISSING_PROPERTY_ERROR_MESSAGE, propertyName),
                                                 null);
      }

      if (propertyNames != null) {
         for (String name : propertyNames) {
            if (!properties.containsKey(name)) {
               throw new ProjectConfigurationException(String.format(MISSING_PROPERTY_ERROR_MESSAGE, name),
                                                       null);
            }
         }
      }
   }

   /**
    * Requires that all the given system properties are set, throwing a {@code ProjectConfigurationException}
    * otherwise.
    *
    * @param properties    the properties set in the project
    * @param propertyName  the name of the property that must be set
    * @param propertyNames additional names of properties that must be set (optional)
    */
   public static void requireSystemProperties(Map<String, ?> properties, String propertyName, String... propertyNames) {
      if (properties.containsKey(propertyName)) {
         throw new ProjectConfigurationException(String.format(MISSING_SYSTEM_PROPERTY_ERROR_MESSAGE,
                                                               propertyName,
                                                               propertyName),
                                                 null);
      }
      if (propertyNames != null) {
         for (String name : propertyNames) {
            if (!properties.containsKey(name)) {
               throw new ProjectConfigurationException(String.format(MISSING_SYSTEM_PROPERTY_ERROR_MESSAGE,
                                                                     name,
                                                                     name),
                                                       null);
            }
         }
      }
   }

   /**
    * Verifies the given condition is true, throwing an {@code InvalidUserDataException} if it isn't.
    *
    * @param condition  the condition to test
    * @param message    the {@link String#format(String, Object...) formatted} message to include in the exception
    * @param parameters optional parameters used in the message format
    * @see com.google.common.base.Preconditions
    */
   public static void checkUserData(boolean condition, String message, Object... parameters) {
      if (!condition) {
         String m = parameters == null || parameters.length == 0 ? message : String.format(message, parameters);
         throw new InvalidUserDataException(m);
      }
   }
}
