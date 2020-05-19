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
package com.ngc.seaside.gradle.plugins.eclipse.updatesite.category;

import com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature.EclipseFeature;

import java.io.Serializable;
import java.util.Objects;

public class EclipseCategoryFeature implements Serializable {

   private String id;
   private String version;

   public EclipseCategoryFeature() {
   }

   /**
    * Constructs from an {@link EclipseFeature}.
    * 
    * @param feature feature
    */
   public EclipseCategoryFeature(EclipseFeature feature) {
      this.id = feature.getId();
      this.version = feature.getVersion();
   }

   public String getId() {
      return id;
   }

   public String getVersion() {
      return version;
   }
   
   @Override
   public int hashCode() {
      return Objects.hash(id, version);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof EclipseCategoryFeature)) {
         return false;
      }
      EclipseCategoryFeature that = (EclipseCategoryFeature) o;
      return Objects.equals(this.id, that.id)
               && Objects.equals(this.version, that.version);
   }
}
