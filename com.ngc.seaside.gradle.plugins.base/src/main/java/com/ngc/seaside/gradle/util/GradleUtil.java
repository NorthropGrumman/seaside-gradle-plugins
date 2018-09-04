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
