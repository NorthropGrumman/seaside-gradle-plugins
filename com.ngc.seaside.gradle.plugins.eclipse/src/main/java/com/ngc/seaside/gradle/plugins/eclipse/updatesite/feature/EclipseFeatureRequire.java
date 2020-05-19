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
 * Structure representing an eclipse feature dependency requirement.
 */
public class EclipseFeatureRequire implements Serializable {

   private String plugin;
   private String feature;
   private String version;
   private Match match;
   private boolean patch;

   /**
    * Matching Rule
    */
   public enum Match {
      /**
       * Dependent plug-in version must match exactly the specified version.
       */
      PERFECT("perfect"),

      /**
       * Dependent plug-in version must be at least at the version specified, or at a higher service level (major and
       * minor version levels must equal the specified version).
       */
      EQUIVALENT("equivalent"),

      /**
       * Dependent plug-in version must be at least at the version specified, or at a higher service level or minor
       * level (major version level must equal the specified version).
       */
      COMPATIBLE("compatible"),

      /**
       * Dependent plug-in version must be at least at the version specified, or at a higher service, minor or major
       * level.
       */
      GREATER_OR_EQUAL("greaterOrEqual");

      private final String xmlName;

      Match(String xmlName) {
         this.xmlName = xmlName;
      }

      String getXmlName() {
         return xmlName;
      }

      static Match fromXmlName(String xmlName) {
         if (xmlName == null) {
            return defaultMatch();
         }
         for (Match m : Match.values()) {
            if (m.xmlName.equals(xmlName)) {
               return m;
            }
         }
         return defaultMatch();
      }

      static Match defaultMatch() {
         return COMPATIBLE;
      }
   }

   /**
    * Returns the identifier of dependent plug-in.
    * 
    * @return the identifier of dependent plug-in
    */
   public String getPlugin() {
      return plugin;
   }

   /**
    * Sets the identifier of the dependent plug-in. Either plugin or feature attribute must be set, but not both.
    * 
    * @param plugin plug-in identifier
    * @return this
    */
   public EclipseFeatureRequire setPlugin(String plugin) {
      this.plugin = plugin;
      return this;
   }

   /**
    * Returns the identifier of dependent feature.
    * 
    * @return the identifier of dependent feature
    */
   public String getFeature() {
      return feature;
   }

   /**
    * Sets the identifier of dependent feature. Either plugin or feature attribute must be set, but not both.
    * 
    * @param feature the identifier of dependent feature
    * @return this
    */
   public EclipseFeatureRequire setFeature(String feature) {
      this.feature = feature;
      return this;
   }

   /**
    * Returns the plug-in version specification.
    * 
    * @return the plug-in version specification
    */
   public String getVersion() {
      return version;
   }

   /**
    * Sets the plug-in version specification.
    * 
    * @param version the plug-in version specification
    * @return this
    */
   public EclipseFeatureRequire setVersion(String version) {
      if (version != null) {
         this.version = BaseEclipsePlugin.getValidEclipseVersion(version)
                  .orElseThrow(() -> new IllegalArgumentException("Invalid version: " + version));
      } else {
         this.version = version;
      }
      return this;
   }

   /**
    * Returns the matching rule.
    * 
    * @return the matching rule
    */
   public Match getMatch() {
      return match;
   }

   /**
    * Sets the matching rule.
    * 
    * @param match the matching rule
    * @return this
    */
   public EclipseFeatureRequire setMatch(Object match) {
      if (match instanceof Match) {
         this.match = (Match) match;
      } else if (match instanceof CharSequence) {
         this.match = Match.valueOf(match.toString());
      } else {
         throw new IllegalArgumentException("Invalid match: " + match);
      }
      return this;
   }

   /**
    * Returns whether or not this constraint declares the enclosing feature to be a patch for the referenced feature.
    * 
    * @return whether or not this constraint declares the enclosing feature to be a patch for the referenced feature
    */
   public boolean isPatch() {
      return patch;
   }

   /**
    * Sets whether or not this constraint declares the enclosing feature to be a patch for the referenced feature.
    * Certain rules must be followed when this attribute is set:
    * <ul>
    * <li>feature attribute must be used to identifier of feature being patched</li>
    * <li>version attribute must be set</li>
    * <li>match attribute should not be set and "perfect" value will be assumed</li>
    * <li>if other features are {@link EclipseFeature#getIncludes() included}, they must also be patches</li>
    * </ul>
    * 
    * @param patch whether or not this constraint declares the enclosing feature to be a patch for the referenced
    *           feature
    * @return this
    */
   public EclipseFeatureRequire setPatch(boolean patch) {
      this.patch = patch;
      return this;
   }

   @Override
   public int hashCode() {
      return Objects.hash(plugin, feature, version, match, patch);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof EclipseFeatureRequire)) {
         return false;
      }
      EclipseFeatureRequire that = (EclipseFeatureRequire) o;
      return Objects.equals(this.plugin, that.plugin)
               && Objects.equals(this.feature, that.feature)
               && Objects.equals(this.version, that.version)
               && Objects.equals(this.match, that.match)
               && Objects.equals(this.patch, that.patch);
   }
}
