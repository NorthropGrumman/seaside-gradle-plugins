/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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

import com.google.common.base.Preconditions;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Collection;

public class PropertyUtils {

   private static final String PROPERTY_NAME_DELIMITER = ",";
   private static final String PROPERTY_VALUE_DELIMITER = PROPERTY_NAME_DELIMITER;

   private PropertyUtils() {
   }

   public static Collection<Object> getProperties(Project project, String propertyNames) {
      Preconditions.checkNotNull(project, "project may not be null!");
      Preconditions.checkNotNull(propertyNames, "propertyNames may not be null!");
      Preconditions.checkArgument(!propertyNames.trim().equals(""), "propertyNames may not be empty!");

      Collection<Object> values = new ArrayList<>();
      for (String name : propertyNames.split(PROPERTY_NAME_DELIMITER)) {
         Object value = project.findProperty(name);
         if (value != null) {
            values.add(value);
         }
      }
      return values;
   }

   public static void setProperties(Project project, String propertyNames, String propertyValues) {
      Preconditions.checkNotNull(project, "project may not be null!");
      Preconditions.checkNotNull(propertyNames, "propertyNames may not be null!");
      Preconditions.checkArgument(!propertyNames.trim().equals(""), "propertyNames may not be empty!");
      Preconditions.checkNotNull(propertyValues, "propertyValues may not be null!");
      Preconditions.checkArgument(!propertyValues.trim().equals(""), "propertyValues may not be empty!");

      String[] names = propertyNames.split(PROPERTY_NAME_DELIMITER);
      String[] values = propertyValues.split(PROPERTY_VALUE_DELIMITER);
      Preconditions.checkArgument(names.length == values.length,
                                  "must have same number of names and values, got %s names and %s values!",
                                  names.length,
                                  values.length);

      for (int i = 0; i < names.length; i++) {
         project.setProperty(names[i], values[i]);
      }
   }
}
