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
