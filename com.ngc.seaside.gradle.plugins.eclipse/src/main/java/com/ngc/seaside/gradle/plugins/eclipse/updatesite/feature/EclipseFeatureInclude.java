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
package com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature;

import com.ngc.seaside.gradle.plugins.eclipse.BaseEclipsePlugin;

import java.io.Serializable;
import java.util.Objects;

public class EclipseFeatureInclude implements Serializable {

   private String id;
   private String version;
   private boolean required;
   private String name;

   /**
    * Returns the nested feature identifier.
    * 
    * @return the nested feature identifier
    */
   public String getId() {
      return id;
   }

   /**
    * Sets the nested feature identifier.
    * 
    * @param id the nested feature identifier
    * @return this
    */
   public EclipseFeatureInclude setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Returns the nested feature version.
    * 
    * @return the nested feature version
    */
   public String getVersion() {
      return version;
   }

   /**
    * Sets the nested feature version.
    * 
    * @param version the nested feature version
    * @return this
    */
   public EclipseFeatureInclude setVersion(String version) {
      if (version != null) {
         this.version = BaseEclipsePlugin.getValidEclipseVersion(version)
                  .orElseThrow(() -> new IllegalArgumentException("Invalid version: " + version));
      } else {
         this.version = version;
      }
      return this;
   }

   /**
    * Returns whether or not the included feature is required.
    * 
    * @return whether or not the included feature is required
    */
   public boolean isRequired() {
      return required;
   }

   /**
    * Sets whether or not the included feature is required.
    * 
    * @param required whether or not the included feature is required
    * @return this
    */
   public EclipseFeatureInclude setRequired(boolean required) {
      this.required = required;
      return this;
   }

   /**
    * Returns the identifying name used if the feature is missing.
    * 
    * @return the identifying name used if the feature is missing
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the identifying name used if the feature is missing.
    * 
    * @param name the identifying name used if the feature is missing
    * @return this
    */
   public EclipseFeatureInclude setName(String name) {
      this.name = name;
      return this;
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, version, required, name);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof EclipseFeatureInclude)) {
         return false;
      }
      EclipseFeatureInclude that = (EclipseFeatureInclude) o;
      return Objects.equals(this.id, that.id)
               && Objects.equals(this.version, that.version)
               && Objects.equals(this.required, that.required)
               && Objects.equals(this.name, that.name);
   }
}
