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
