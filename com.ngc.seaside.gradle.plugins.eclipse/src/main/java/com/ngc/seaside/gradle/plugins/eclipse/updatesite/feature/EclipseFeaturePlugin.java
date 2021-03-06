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

/**
 * Structure representing a plug-in in an eclipse feature.
 */
public class EclipseFeaturePlugin implements Serializable {

   private String id;
   private String version;
   private boolean fragment = false;
   private boolean unpack = true;

   /**
    * Returns the plug-in identifier.
    * 
    * @return the plug-in identifier
    */
   public String getId() {
      return id;
   }

   /**
    * Sets the plug-in identifier.
    * 
    * @param id the plug-in identifier
    * @return this
    */
   public EclipseFeaturePlugin setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Returns the plug-in version.
    * 
    * @return the plug-in version
    */
   public String getVersion() {
      return version;
   }

   /**
    * Sets the plug-in version.
    * 
    * @param version the plug-in version
    * @return this
    */
   public EclipseFeaturePlugin setVersion(String version) {
      if (version != null) {
         this.version = BaseEclipsePlugin.getValidEclipseVersion(version)
                  .orElseThrow(() -> new IllegalArgumentException("Invalid version: " + version));
      } else {
         this.version = version;
      }
      return this;
   }

   /**
    * Returns whether the plug-in is a fragment.
    * 
    * @return whether the plug-in is a fragment
    */
   public boolean isFragment() {
      return fragment;
   }

   /**
    * Sets whether the plug-in is a fragment.
    * 
    * @param fragment whether the plug-in is a fragment
    * @return this
    */
   public EclipseFeaturePlugin setFragment(boolean fragment) {
      this.fragment = fragment;
      return this;
   }

   /**
    * Returns whether the plug-in jar should be unpacked or not.
    * 
    * @return whether the plug-in jar should be unpacked or not
    */
   public boolean getUnpack() {
      return unpack;
   }

   /**
    * Sets whether the plug-in jar should be unpacked or not.
    * 
    * @param unpacked whether the plug-in jar should be unpacked or not
    * @return this
    */
   public EclipseFeaturePlugin setUnpack(boolean unpacked) {
      this.unpack = unpacked;
      return this;
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, version, fragment, unpack);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof EclipseFeaturePlugin)) {
         return false;
      }
      EclipseFeaturePlugin that = (EclipseFeaturePlugin) o;
      return Objects.equals(this.id, that.id)
               && Objects.equals(this.version, that.version)
               && Objects.equals(this.fragment, that.fragment)
               && Objects.equals(this.unpack, that.unpack);
   }
}
