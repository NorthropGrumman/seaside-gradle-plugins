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
    * @return
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
