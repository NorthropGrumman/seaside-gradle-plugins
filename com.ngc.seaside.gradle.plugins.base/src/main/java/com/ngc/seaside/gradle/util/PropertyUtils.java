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
