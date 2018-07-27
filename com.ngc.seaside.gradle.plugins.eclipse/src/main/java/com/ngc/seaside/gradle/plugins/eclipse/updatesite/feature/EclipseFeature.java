package com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature;

import com.ngc.seaside.gradle.plugins.eclipse.BaseEclipsePlugin;

import groovy.lang.Closure;

import org.eclipse.updatesite.feature.Copyright;
import org.eclipse.updatesite.feature.Description;
import org.eclipse.updatesite.feature.Feature;
import org.eclipse.updatesite.feature.Import;
import org.eclipse.updatesite.feature.Includes;
import org.eclipse.updatesite.feature.License;
import org.eclipse.updatesite.feature.Plugin;
import org.eclipse.updatesite.feature.Requires;
import org.gradle.api.Action;
import org.gradle.util.GUtil;

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Structure representing an eclipse feature.
 */
public class EclipseFeature implements Serializable {

   private String id;
   private String label;
   private String version;
   private String providerName;
   private final TextWithUrl description = new TextWithUrl();
   private final TextWithUrl copyright = new TextWithUrl();
   private final TextWithUrl license = new TextWithUrl();
   private final List<EclipseFeatureInclude> includes = new ArrayList<>();
   private final List<EclipseFeatureRequire> requires = new ArrayList<>();
   private final List<EclipseFeaturePlugin> plugins = new ArrayList<>();

   String getName() {
      return id;
   }

   void setName(String name) {
      setId(name);
   }

   /**
    * Returns the feature identifier (eg. com.xyz.myfeature).
    * 
    * @return the feature identifier
    */
   public String getId() {
      return id;
   }

   /**
    * Sets the feature identifier (eg. com.xyz.myfeature).
    * 
    * @param id the feature identifier
    * @return this
    */
   public EclipseFeature setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Returns the displayable label (name).
    * 
    * @return the displayable label (name)
    */
   public String getLabel() {
      return label;
   }

   /**
    * Sets the displayable label (name).
    * 
    * @param label displayable label
    * @return this
    */
   public EclipseFeature setLabel(String label) {
      this.label = label;
      return this;
   }

   /**
    * Returns component version (eg. 1.0.3).
    * 
    * @return component version
    */
   public String getVersion() {
      return version;
   }

   /**
    * Sets the component version (eg. 1.0.3).
    * 
    * @param version component version
    * @return this
    */
   public EclipseFeature setVersion(String version) {
      if (version != null) {
         this.version = BaseEclipsePlugin.getValidEclipseVersion(version)
                  .orElseThrow(() -> new IllegalArgumentException("Invalid version: " + version));
      } else {
         this.version = version;
      }
      return this;
   }

   /**
    * Returns the display label identifying the organization providing this component.
    * 
    * @return the display label identifying the organization providing this component
    */
   public String getProviderName() {
      return providerName;
   }

   /**
    * Sets the display label identifying the organization providing this component.
    * 
    * @param providerName the display label identifying the organization providing
    * @return this
    */
   public EclipseFeature setProviderName(String providerName) {
      this.providerName = providerName;
      return this;
   }

   /**
    * Returns the brief component description as simple text.
    * 
    * @return the brief component description as simple text
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
    * Returns the feature copyright as simple text.
    * 
    * @return the feature copyright as simple text
    */
   public TextWithUrl getCopyright() {
      return copyright;
   }

   /**
    * Sets the feature copyright.
    * 
    * @param copyright feature copyright
    * @return the feature copyright
    */
   public TextWithUrl setCopyright(Object copyright) {
      if (copyright instanceof TextWithUrl) {
         this.copyright.setText(((TextWithUrl) copyright).getText());
         this.copyright.setUrl(((TextWithUrl) copyright).getUrl());
      } else if (copyright instanceof CharSequence) {
         this.copyright.setText(copyright.toString());
      } else {
         throw new IllegalArgumentException("Invalid copyright: " + copyright);
      }
      return this.copyright;
   }

   /**
    * Applies the given action to the copyright.
    * 
    * @param action action
    * @return the copyright
    */
   public TextWithUrl copyright(Action<TextWithUrl> action) {
      action.execute(copyright);
      return copyright;
   }

   /**
    * Applies the given closure to the copyright.
    * 
    * @param closure closure
    * @return the copyright
    */
   public TextWithUrl copyright(Closure<?> closure) {
      closure.setDelegate(copyright);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(copyright);
      return copyright;
   }

   /**
    * Returns the feature "click-through" license as simple text.
    * 
    * @return the feature "click-through" license as simple text
    */
   public TextWithUrl getLicense() {
      return license;
   }

   /**
    * Sets the feature "click-through" license.
    * 
    * @param license license
    * @return the license
    */
   public TextWithUrl setLicense(Object license) {
      if (license instanceof TextWithUrl) {
         this.license.setText(((TextWithUrl) license).getText());
         this.license.setUrl(((TextWithUrl) license).getUrl());
      } else if (license instanceof CharSequence) {
         this.license.setText(license.toString());
      } else {
         throw new IllegalArgumentException("Invalid license: " + license);
      }
      return this.license;
   }

   /**
    * Applies the given action to the license.
    * 
    * @param action action
    * @return the license
    */
   public TextWithUrl license(Action<TextWithUrl> action) {
      action.execute(license);
      return license;
   }

   /**
    * Applies the given closure to the license.
    * 
    * @param closure closure
    * @return the license
    */
   public TextWithUrl license(Closure<?> closure) {
      closure.setDelegate(license);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(license);
      return license;
   }

   /**
    * Returns the nested features included in this feature and whether or not they are required.
    * 
    * @return the nested features included in this feature
    */
   public Collection<EclipseFeatureInclude> getIncludes() {
      return includes;
   }

   /**
    * Sets the nested features included in this feature and whether or not they are required.
    * 
    * @param includes nested features included in this feature
    * @return this
    */
   @SuppressWarnings("unchecked")
   public EclipseFeature setIncludes(Iterable<EclipseFeatureInclude> includes) {
      this.includes.clear();
      GUtil.addToCollection(this.includes, includes);
      return this;
   }

   /**
    * Adds a new nested feature to be included for this feature and configures it with the given action.
    * 
    * @param action action
    * @return the include configuration
    */
   public EclipseFeatureInclude include(Action<EclipseFeatureInclude> action) {
      EclipseFeatureInclude include = new EclipseFeatureInclude();
      action.execute(include);
      this.includes.add(include);
      return include;
   }

   /**
    * Adds a new nested feature to be included for this feature and configures it with the given closure.
    * 
    * @param closure closure
    * @return the include configuration
    */
   public EclipseFeatureInclude include(Closure<?> closure) {
      EclipseFeatureInclude include = new EclipseFeatureInclude();
      closure.setDelegate(include);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(include);
      this.includes.add(include);
      return include;
   }

   /**
    * Returns this feature's required dependencies.
    * 
    * @return this feature's required dependencies
    */
   public Collection<EclipseFeatureRequire> getRequires() {
      return requires;
   }

   /**
    * Sets this feature's required dependencies.
    * 
    * @param requires this feature's required dependencies
    * @return this
    */
   @SuppressWarnings("unchecked")
   public EclipseFeature setRequires(Iterable<EclipseFeatureRequire> requires) {
      this.requires.clear();
      GUtil.addToCollection(this.requires, requires);
      return this;
   }

   /**
    * Adds a new required dependency for this feature and configures it with the given action.
    * 
    * @param action action
    * @return the require configuration
    */
   public EclipseFeatureRequire requires(Action<EclipseFeatureRequire> action) {
      EclipseFeatureRequire require = new EclipseFeatureRequire();
      action.execute(require);
      this.requires.add(require);
      return require;
   }

   /**
    * Adds a new required dependency for this feature and configures it with the given closure.
    * 
    * @param closure closure
    * @return the require configuration
    */
   public EclipseFeatureRequire requires(Closure<?> closure) {
      EclipseFeatureRequire require = new EclipseFeatureRequire();
      closure.setDelegate(require);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(require);
      this.requires.add(require);
      return require;
   }

   /**
    * Returns the collection of referenced plug-ins.
    * 
    * @return the collection of referenced plug-ins
    */
   public Collection<EclipseFeaturePlugin> getPlugins() {
      return plugins;
   }

   /**
    * Sets the collection of referenced plug-ins.
    * 
    * @param plugins the collection of referenced plug-ins
    * @return this
    */
   @SuppressWarnings("unchecked")
   public EclipseFeature setPlugins(Iterable<EclipseFeaturePlugin> plugins) {
      this.plugins.clear();
      GUtil.addToCollection(this.plugins, plugins);
      return this;
   }

   /**
    * Adds a new referenced plug-in for this feature and configures it with the given action.
    * 
    * @param action action
    * @return the plugin configuration
    */
   public EclipseFeaturePlugin plugin(Action<EclipseFeaturePlugin> action) {
      EclipseFeaturePlugin plugin = new EclipseFeaturePlugin();
      action.execute(plugin);
      this.plugins.add(plugin);
      return plugin;
   }

   /**
    * Adds a new referenced plug-in for this feature and configures it with the given closure.
    * 
    * @param closure closure
    * @return the plugin configuration
    */
   public EclipseFeaturePlugin plugin(Closure<?> closure) {
      EclipseFeaturePlugin plugin = new EclipseFeaturePlugin();
      closure.setDelegate(plugin);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(plugin);
      this.plugins.add(plugin);
      return plugin;
   }

   /**
    * Parses the xml file from the given reader and returns the eclipse feature.
    * 
    * @param reader reader
    * @return eclipse feature
    */
   public static EclipseFeature fromXml(Reader reader) {
      Feature feature;
      try {
         JAXBContext context = JAXBContext.newInstance(Feature.class);
         Unmarshaller unmarshaller = context.createUnmarshaller();
         feature = (Feature) unmarshaller.unmarshal(reader);
      } catch (JAXBException e) {
         throw new IllegalStateException(e);
      }
      return fromFeature(feature);
   }

   /**
    * Writes the eclipse feature to the given writer.
    * 
    * @param writer writer
    */
   public void toXml(Writer writer) {
      Feature feature = toFeature();
      try {
         JAXBContext context = JAXBContext.newInstance(Feature.class);
         Marshaller marshaller = context.createMarshaller();
         marshaller.marshal(feature, writer);
      } catch (JAXBException e) {
         throw new IllegalStateException(e);
      }
   }

   private Feature toFeature() {
      Feature feature = new Feature();
      feature.setId(getId());
      feature.setLabel(getLabel());
      feature.setVersion(getVersion());
      feature.setProviderName(getProviderName());
      if (copyright.getText() != null || copyright.getUrl() != null) {
         Copyright c = new Copyright();
         c.setvalue(copyright.getText());
         c.setUrl(copyright.getUrl());
         feature.getInstallHandlerOrDescriptionOrCopyrightOrLicenseOrUrlOrIncludesOrRequiresOrPluginOrData().add(c);
      }
      if (description.getText() != null || description.getUrl() != null) {
         Description d = new Description();
         d.setvalue(description.getText());
         d.setUrl(description.getUrl());
         feature.getInstallHandlerOrDescriptionOrCopyrightOrLicenseOrUrlOrIncludesOrRequiresOrPluginOrData().add(d);
      }
      if (license.getText() != null || license.getUrl() != null) {
         License l = new License();
         l.setvalue(license.getText());
         l.setUrl(license.getUrl());
         feature.getInstallHandlerOrDescriptionOrCopyrightOrLicenseOrUrlOrIncludesOrRequiresOrPluginOrData().add(l);
      }
      for (EclipseFeatureInclude include : includes) {
         Includes i = new Includes();
         i.setId(include.getId());
         i.setName(include.getName());
         i.setOptional(Boolean.toString(!include.isRequired()));
         i.setVersion(include.getVersion());
         feature.getInstallHandlerOrDescriptionOrCopyrightOrLicenseOrUrlOrIncludesOrRequiresOrPluginOrData().add(i);
      }
      for (EclipseFeaturePlugin plugin : plugins) {
         Plugin p = new Plugin();
         p.setId(plugin.getId());
         p.setVersion(plugin.getVersion());
         p.setFragment(Boolean.toString(plugin.isFragment()));
         p.setUnpack(Boolean.toString(plugin.getUnpack()));
         feature.getInstallHandlerOrDescriptionOrCopyrightOrLicenseOrUrlOrIncludesOrRequiresOrPluginOrData().add(p);
      }
      if (!requires.isEmpty()) {
         Requires r = new Requires();
         for (EclipseFeatureRequire require : requires) {
            Import i = new Import();
            i.setFeature(require.getFeature());
            i.setMatch(require.getMatch().getXmlName());
            i.setPatch(Boolean.toString(require.isPatch()));
            i.setPlugin(require.getPlugin());
            i.setVersion(require.getVersion());
            r.getImport().add(i);
         }
         feature.getInstallHandlerOrDescriptionOrCopyrightOrLicenseOrUrlOrIncludesOrRequiresOrPluginOrData().add(r);
      }
      return feature;
   }

   private static EclipseFeature fromFeature(Feature feature) {
      EclipseFeature eclipseFeature = new EclipseFeature();
      eclipseFeature.setId(feature.getId());
      eclipseFeature.setLabel(feature.getLabel());
      eclipseFeature.setVersion(feature.getVersion());
      eclipseFeature.setProviderName(feature.getProviderName());
      List<Object> others =
               feature.getInstallHandlerOrDescriptionOrCopyrightOrLicenseOrUrlOrIncludesOrRequiresOrPluginOrData();
      for (Object other : others) {
         if (other instanceof Copyright) {
            eclipseFeature.copyright.setText(((Copyright) other).getvalue());
            eclipseFeature.copyright.setUrl(((Copyright) other).getUrl());
         } else if (other instanceof Description) {
            eclipseFeature.description.setText(((Description) other).getvalue());
            eclipseFeature.description.setUrl(((Description) other).getUrl());
         } else if (other instanceof License) {
            eclipseFeature.license.setText(((License) other).getvalue());
            eclipseFeature.license.setUrl(((License) other).getUrl());
         } else if (other instanceof Includes) {
            Includes includes = (Includes) other;
            eclipseFeature.include(i -> {
               i.setId(includes.getId());
               i.setName(includes.getName());
               i.setRequired(!Boolean.parseBoolean(includes.getOptional()));
               i.setVersion(includes.getVersion());
            });
         } else if (other instanceof Plugin) {
            Plugin plugin = (Plugin) other;
            eclipseFeature.plugin(p -> {
               p.setId(plugin.getId());
               p.setVersion(plugin.getVersion());
               p.setFragment(Boolean.parseBoolean(plugin.getFragment()));
               p.setUnpack(Boolean.parseBoolean(plugin.getUnpack()));
            });
         } else if (other instanceof Requires) {
            for (Import i : ((Requires) other).getImport()) {
               eclipseFeature.requires(r -> {
                  r.setFeature(i.getFeature());
                  r.setPatch(Boolean.parseBoolean(i.getPatch()));
                  r.setPlugin(i.getPlugin());
                  r.setVersion(i.getVersion());
                  r.setMatch(EclipseFeatureRequire.Match.fromXmlName(i.getMatch()));
               });
            }
         }
      }

      return eclipseFeature;
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, label, version, providerName, description, copyright, license, includes, requires,
               plugins);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof EclipseFeature)) {
         return false;
      }
      EclipseFeature that = (EclipseFeature) o;
      return Objects.equals(this.id, that.id)
               && Objects.equals(this.label, that.label)
               && Objects.equals(this.version, that.version)
               && Objects.equals(this.providerName, that.providerName)
               && Objects.equals(this.description, that.description)
               && Objects.equals(this.copyright, that.copyright)
               && Objects.equals(this.license, that.license)
               && Objects.equals(this.includes, that.includes)
               && Objects.equals(this.requires, that.requires)
               && Objects.equals(this.plugins, that.plugins);
   }
}
