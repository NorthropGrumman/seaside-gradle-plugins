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
package com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature;

import java.io.Serializable;
import java.util.Objects;

public class TextWithUrl implements Serializable {

   private String text;
   private String url;

   public String getText() {
      return text;
   }

   public TextWithUrl setText(String text) {
      this.text = text;
      return this;
   }

   public String getUrl() {
      return url;
   }

   public TextWithUrl setUrl(String url) {
      this.url = url;
      return this;
   }

   @Override
   public int hashCode() {
      return Objects.hash(text, url);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof TextWithUrl)) {
         return false;
      }
      TextWithUrl that = (TextWithUrl) o;
      return Objects.equals(this.text, that.text)
               && Objects.equals(this.url, that.url);
   }

}
