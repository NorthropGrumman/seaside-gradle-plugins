package com.ngc.seaside.gradle.util;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.internal.artifacts.dependencies.DefaultResolvedVersionConstraint;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelector;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme;
import org.gradle.api.tasks.Copy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class OsgiResolver {

   public static final List<String> EXCLUDED_BUNDLES = Collections.unmodifiableList(Arrays.asList(
            "org.osgi:org.osgi.core",
            "org.osgi:org.osgi.enterprise",
            "org.osgi:osgi.core",
            "org.osgi:osgi.enterprise"));

   public static final String BUNDLE_SYMBOLIC_NAME_PROPERTY = "Bundle-SymbolicName";
   public static final String BUNDLE_VERSION_PROPERTY = "Bundle-Version";

   /**
    * If a bundle configuration contains a {@link Boolean} attribute with the given name, and it is set to {@code true},
    * the configuration will use its configured dependency resolution strategy and not include duplicate dependencies
    * with differing versions.
    */
   public static final String INCLUDE_CONFLICTING_VERSIONS_ATTRIBUTE_NAME = "includeConflictingVersions";

   private final VersionSelectorScheme versionSelectorscheme;
   private final Project project;

   public OsgiResolver(Project project, VersionSelectorScheme versionSelectorscheme) {
      this.project = project;
      this.versionSelectorscheme = versionSelectorscheme;
   }

   /**
    * Returns {@code true} if the given artifact represents an OSGi-compliant bundle and if the bundle
    * should not be {@link #EXCLUDED_BUNDLES excluded}.
    * 
    * @param artifact artifact
    * @return {@code true} if the given artifact represents an OSGi compliant bundle
    */
   public static boolean isOsgiArtifact(ResolvedArtifact artifact) {
      ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
      String ga = String.format("%s:%s", id.getGroup(), id.getName());
      for (String excludedBundle : EXCLUDED_BUNDLES) {
         if (excludedBundle.equals(ga)) {
            return false;
         }
      }
      return isOsgiArtifact(artifact.getFile());
   }

   /**
    * Returns the osgi symbolic name of the given artifact, or {@link Optional#empty()} if the artifact is not an
    * OSGi-compliant bundle.
    * 
    * @param artifact artifact
    * @return the osgi symbolic name of the given artifact
    */
   public static Optional<String> getOsgiSymbolicName(ResolvedArtifact artifact) {
      return getOsgiSymbolicName(artifact.getFile());
   }

   /**
    * Returns the osgi version of the given artifact, or {@link Optional#empty()} if the artifact is not an
    * OSGi-compliant bundle.
    * 
    * @param artifact artifact
    * @return the osgi version of the given artifact
    */
   public static Optional<String> getOsgiVersion(ResolvedArtifact artifact) {
      return getOsgiVersion(artifact.getFile());
   }

   /**
    * Returns {@code true} if the given file represents an OSGi-compliant bundle and if the bundle
    * should not be {@link #EXCLUDED_BUNDLES excluded}.
    * 
    * @param file bundle
    * @return {@code true} if the given file represents an OSGi compliant bundle
    */
   public static boolean isOsgiArtifact(File file) {
      return getOsgiSymbolicName(file).isPresent();
   }

   /**
    * Returns the osgi symbolic name of the given bundle, or {@link Optional#empty()} if the file is not an
    * OSGi-compliant bundle.
    * 
    * @param file bundle
    * @return the osgi symbolic name of the given bundle
    */
   public static Optional<String> getOsgiSymbolicName(File file) {
      try (ZipFile zipFile = new ZipFile(file)) {
         ZipEntry entry = zipFile.getEntry("META-INF/MANIFEST.MF");
         if (entry == null) {
            return Optional.empty();
         }
         Manifest manifest = new Manifest(zipFile.getInputStream(entry));
         return getOsgiSymbolicName(manifest);
      } catch (ZipException e) {
         return Optional.empty();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   /**
    * Returns the osgi symbolic name from the given manifest, or {@link Optional#empty()} if the file is not an
    * OSGi-compliant bundle.
    * 
    * @param manifest manifest
    * @return the osgi symbolic name from the given manifest
    */
   public static Optional<String> getOsgiSymbolicName(Manifest manifest) {
      String name = manifest.getMainAttributes().getValue(BUNDLE_SYMBOLIC_NAME_PROPERTY);
      if (name != null) {
         int index = name.indexOf(';');
         if (index >= 0) {
            name = name.substring(0, index);
         }
         name = name.trim();
      }
      return Optional.ofNullable(name);
   }

   /**
    * Returns the osgi version of the given bundle, or {@link Optional#empty()} if the file is not an
    * OSGi-compliant bundle.
    * 
    * @param file bundle
    * @return the osgi version of the given bundle
    */
   public static Optional<String> getOsgiVersion(File file) {
      try (ZipFile zipFile = new ZipFile(file)) {
         ZipEntry entry = zipFile.getEntry("META-INF/MANIFEST.MF");
         Manifest manifest = new Manifest(zipFile.getInputStream(entry));
         return Optional.of(getOsgiVersion(manifest));
      } catch (ZipException e) {
         return Optional.empty();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   /**
    * Returns the osgi version found in the given manifest.
    * 
    * @param manifest manifest
    * @return the osgi version
    */
   public static String getOsgiVersion(Manifest manifest) {
      String name = manifest.getMainAttributes().getValue(BUNDLE_VERSION_PROPERTY);
      return name == null ? "0.0.0" : name;
   }

   /**
    * When the given configuration is resolved, this method will call the given action for all file dependencies,
    * including duplicate modules that vary only by version.
    * 
    * <p>
    * Note: some tasks (such as {@link Copy} will be skipped when used with this method. This can be worked around by
    * creating another task that will first resolve the given configuration:
    * <pre>
    * def resolvedTask = task {
    *    inputs.files(config)
    *    doLast { config.resolve() }
    * }
    * copyTask.dependsOn resolvedTask
    * </pre>
    * 
    * @param configuration configuration
    * @param action action for each dependency and file
    */
   public void resolveAllVersions(Configuration configuration, BiConsumer<ComponentIdentifier, File> action) {
      configuration.getIncoming().afterResolve(dependencies -> {
         Attribute<Boolean> attribute = Attribute.of(INCLUDE_CONFLICTING_VERSIONS_ATTRIBUTE_NAME, Boolean.class);
         final boolean shouldResolve = !configuration.getAttributes().contains(attribute)
                  || configuration.getAttributes().getAttribute(attribute);
         if (shouldResolve) {
            dependencies.getResolutionResult().allDependencies(result -> {
               // Gradle doesn't have a built-in way of including all versions of the same module dependency.
               // This will create extra configurations with the versions of dependencies that are overwritten
               // and resolve them separately.
               if (result instanceof ResolvedDependencyResult) {
                  ComponentSelector requested = result.getRequested();
                  ResolvedComponentResult selected = ((ResolvedDependencyResult) result).getSelected();
                  if (requested instanceof ModuleComponentSelector) {
                     ModuleComponentSelector c = (ModuleComponentSelector) requested;
                     DefaultResolvedVersionConstraint constraint =
                              new DefaultResolvedVersionConstraint(c.getVersionConstraint(), versionSelectorscheme);
                     VersionSelector selector = constraint.getPreferredSelector();
                     if (!selector.accept(selected.getModuleVersion().getVersion())) {
                        Configuration detached = project.getConfigurations()
                                 .detachedConfiguration(project.getDependencies()
                                          .module(String.format("%s:%s:%s", c.getGroup(), c.getModule(),
                                                   c.getVersion())));
                        resolveAllVersions(detached, action);
                        detached.resolve();
                     }
                  }
               }
            });
         }
         resolveDependencies(configuration, action);
      });
   }

   private static void resolveDependencies(Configuration configuration, BiConsumer<ComponentIdentifier, File> action) {
      for (Dependency dependency : configuration.getAllDependencies()) {
         resolveDependency(configuration, dependency, action);
      }
   }

   private static void resolveDependency(Configuration configuration, Dependency dependency,
            BiConsumer<ComponentIdentifier, File> action) {
      if (dependency instanceof ModuleDependency) {
         resolveModuleDependency(configuration, (ModuleDependency) dependency, action);
      } else {
         resolveOtherDependency(configuration, dependency, action);
      }
   }

   private static void resolveModuleDependency(Configuration configuration, ModuleDependency dependency,
            BiConsumer<ComponentIdentifier, File> action) {
      ResolvedConfiguration resolvedConfiguration = configuration.getResolvedConfiguration();
      for (ResolvedDependency resolved : resolvedConfiguration
               .getFirstLevelModuleDependencies(dependency::equals)) {
         for (ResolvedArtifact artifact : resolved.getAllModuleArtifacts()) {
            action.accept(artifact.getId().getComponentIdentifier(), artifact.getFile());
         }
      }
   }

   private static void resolveOtherDependency(Configuration configuration, Dependency dependency,
            BiConsumer<ComponentIdentifier, File> action) {
      for (File file : configuration.files(dependency)) {
         action.accept(new UnknownFileDependencyComponentIdentifier(dependency, file), file);
      }
   }

   private static class UnknownFileDependencyComponentIdentifier
            implements ModuleComponentIdentifier, ModuleIdentifier {

      private static final Pattern FILENAME_PATTERN = Pattern.compile(
               "(?<name>\\w+(?:\\.\\w+))*[\\._-](?<version>\\d+(?:\\.\\d+)*(?:[\\._-]\\w+)?)\\.(?<extension>jar|zip)");

      private String group;
      private String module;
      private String version;

      private UnknownFileDependencyComponentIdentifier(Dependency dependency, File file) {
         if (dependency.getGroup() != null) {
            this.group = dependency.getGroup();
         }
         if (dependency.getName() != null) {
            this.module = dependency.getName();
         }
         if (dependency.getVersion() != null) {
            this.version = dependency.getVersion();
         }
         if (group == null || module == null || version == null) {
            Matcher m = FILENAME_PATTERN.matcher(file.getName());
            if (m.matches()) {
               this.module = m.group("name");
               this.version = m.group("version");
            }
         }
         if (this.module == null) {
            this.module = FilenameUtils.getBaseName(file.getName());
         }
      }

      @Override
      public String getDisplayName() {
         StringBuilder builder = new StringBuilder();
         if (group != null) {
            builder.append(group).append(':');
         }
         builder.append(module);
         if (version != null) {
            builder.append(':').append(version);
         }
         return builder.toString();
      }

      @Override
      public String getGroup() {
         return group;
      }

      @Override
      public String getModule() {
         return module;
      }

      @Override
      public String getName() {
         return module;
      }

      @Override
      public String getVersion() {
         return version;
      }

      public ModuleIdentifier getModuleIdentifier() {
         return this;
      }

   }
}
