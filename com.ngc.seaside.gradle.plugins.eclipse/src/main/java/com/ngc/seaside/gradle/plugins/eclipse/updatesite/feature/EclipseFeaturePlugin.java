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
