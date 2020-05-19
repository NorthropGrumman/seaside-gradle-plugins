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
import com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature.TextWithUrl;

import groovy.lang.Closure;

import org.eclipse.updatesite.category.Category;
import org.eclipse.updatesite.category.CategoryDef;
import org.eclipse.updatesite.category.Description;
import org.eclipse.updatesite.category.Feature;
import org.eclipse.updatesite.category.Site;
import org.gradle.api.Action;

import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Structure representing a category of features be installed.
 */
public class EclipseCategory implements Serializable {

   private String name;
   private String label;
   private final TextWithUrl description = new TextWithUrl();
   private final List<EclipseCategoryFeature> features = new ArrayList<>();

   /**
    * Returns the name for the category.
    * 
    * @return the name for the category
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name for the category.
    * 
    * @param name category name
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * Returns the label for the category.
    * 
    * @return the label for the category
    */
   public String getLabel() {
      return label;
   }

   /**
    * Sets the label for the category
    * 
    * @param label category label
    */
   public void setLabel(String label) {
      this.label = label;
   }

   /**
    * Returns the category description.
    * 
    * @return the category description
    */
   public TextWithUrl getDescription() {
      return description;
   }

   /**
    * Sets the description.
    * 
    * @param description description
    * @return the description
    */
   public TextWithUrl setDescription(Object description) {
      if (description instanceof TextWithUrl) {
         this.description.setText(((TextWithUrl) description).getText());
         this.description.setUrl(((TextWithUrl) description).getUrl());
      } else if (description instanceof CharSequence) {
         this.description.setText(description.toString());
      } else {
         throw new IllegalArgumentException("Invalid description: " + description);
      }
      return this.description;
   }

   /**
    * Applies the given action to the description.
    * 
    * @param action action
    * @return the description
    */
   public TextWithUrl description(Action<TextWithUrl> action) {
      action.execute(description);
      return description;
   }

   /**
    * Applies the given closure to the description.
    * 
    * @param closure closure
    * @return the description
    */
   public TextWithUrl description(Closure<?> closure) {
      closure.setDelegate(description);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(description);
      return description;
   }

   /**
    * Returns the collection of feature ids.
    * 
    * @return the collection of feature ids
    */
   public Collection<EclipseCategoryFeature> getFeatures() {
      return features;
   }

   /**
    * Sets the given features for the category.
    * 
    * @param features features
    * @return this
    */
   public EclipseCategory setFeatures(Iterable<Object> features) {
      this.features.clear();
      return features(features);
   }

   /**
    * Adds a feature to the category and configures it with the given action.
    * 
    * @param action action
    * @return this
    */
   public EclipseCategoryFeature feature(Action<EclipseCategoryFeature> action) {
      EclipseCategoryFeature feature = new EclipseCategoryFeature();
      action.execute(feature);
      this.features.add(feature);
      return feature;
   }

   /**
    * Adds a feature to the category and configures it with the given closure.
    * 
    * @param closure closure
    * @return this
    */
   public EclipseCategoryFeature feature(Closure<?> closure) {
      EclipseCategoryFeature feature = new EclipseCategoryFeature();
      closure.setDelegate(feature);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(feature);
      this.features.add(feature);
      return feature;
   }
   
   /**
    * Adds the given feature to the category.
    * 
    * @param feature feature
    * @return this
    */
   public EclipseCategory feature(EclipseFeature feature) {
      return features(feature);
   }

   /**
    * Adds the given features to the category.
    * 
    * @param features features
    * @return this
    */
   public EclipseCategory features(Object... features) {
      return features(Arrays.asList(features));
   }

   /**
    * Adds the given features to the category.
    * 
    * @param features features
    * @return this
    */
   public EclipseCategory features(Iterable<Object> features) {
      for (Object feature : features) {
         if (feature instanceof EclipseCategoryFeature) {
            this.features.add((EclipseCategoryFeature) feature);
         } else if (feature instanceof EclipseFeature) {
            this.features.add(new EclipseCategoryFeature((EclipseFeature) feature));
         } else {
            throw new IllegalArgumentException("Unknown feature: " + feature);
         }
      }
      return this;
   }

   @Override
   public int hashCode() {
      return Objects.hash(name, label, description, features);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof EclipseCategory)) {
         return false;
      }
      EclipseCategory that = (EclipseCategory) o;
      return Objects.equals(this.name, that.name)
               && Objects.equals(this.label, that.label)
               && Objects.equals(this.description, that.description)
               && Objects.equals(this.features, that.features);
   }

   /**
    * Writes the eclipse categories to the given writer.
    * 
    * @param writer writer
    */
   public static void toXml(Collection<EclipseCategory> categories, Writer writer) {
      Site site = toSite(categories);
      try {
         JAXBContext context = JAXBContext.newInstance(Site.class);
         Marshaller marshaller = context.createMarshaller();
         marshaller.marshal(site, writer);
      } catch (JAXBException e) {
         throw new IllegalStateException(e);
      }
   }

   private static Site toSite(Collection<EclipseCategory> categories) {
      Site site = new Site();

      Map<EclipseCategoryFeature, Set<String>> features = new LinkedHashMap<>();
      for (EclipseCategory category : categories) {
         CategoryDef def = new CategoryDef();
         def.setName(category.getName());
         def.setLabel(category.getLabel());
         if (category.description.getText() != null || category.description.getUrl() != null) {
            Description description = new Description();
            description.setUrl(category.description.getUrl());
            description.setvalue(category.description.getText());
            def.setDescription(description);
         }
         site.getCategoryDef().add(def);
         for (EclipseCategoryFeature feature : category.getFeatures()) {
            features.computeIfAbsent(feature, __ -> new LinkedHashSet<>()).add(category.getName());
         }
      }
      for (Entry<EclipseCategoryFeature, Set<String>> entry : features.entrySet()) {
         EclipseCategoryFeature categoryFeature = entry.getKey();
         Set<String> categoryNames = entry.getValue();
         Feature feature = new Feature();
         feature.setId(categoryFeature.getId());
         feature.setVersion(categoryFeature.getVersion());
         for (String categoryName : categoryNames) {
            Category category = new Category();
            category.setName(categoryName);
            feature.getCategory().add(category);
         }
         site.getFeature().add(feature);
      }

      return site;
   }
}
