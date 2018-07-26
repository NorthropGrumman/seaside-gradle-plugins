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
