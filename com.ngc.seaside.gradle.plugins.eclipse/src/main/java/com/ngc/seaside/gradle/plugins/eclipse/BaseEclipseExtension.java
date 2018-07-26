package com.ngc.seaside.gradle.plugins.eclipse;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;

/**
 * Extension for the {@link BaseEclipsePlugin}. This extension is used for getting and setting properties relating to
 * the downloaded eclipse distribution.
 */
public class BaseEclipseExtension {

   public static final String DEFAULT_ECLIPSE_CACHE_DIRECTORY_NAME = "caches/eclipse";

   private final Project project;

   private DirectoryProperty cacheDirectory;
   private String linuxDownloadUrl;
   private String linuxVersion;
   private String windowsDownloadUrl;
   private String windowsVersion;
   private boolean pluginsRepository;

   /**
    * Constructor.
    * 
    * @param project project
    */
   public BaseEclipseExtension(Project project) {
      this.project = project;
      this.cacheDirectory = project.getLayout().directoryProperty();
      this.cacheDirectory.set(new File(project.getGradle().getGradleUserHomeDir(), DEFAULT_ECLIPSE_CACHE_DIRECTORY_NAME)
               .getAbsoluteFile());
   }

   /**
    * Returns the property of the directory used to cache the eclipse distributions. The default is in the
    * {@code eclipse} folder in {@link Gradle#getGradleUserHomeDir()}.
    * 
    * @return the property of the directory used to cache the eclipse distributions
    */
   public DirectoryProperty getCacheDirectory() {
      return cacheDirectory;
   }

   /**
    * Returns the download url to the linux eclipse distribution.
    * 
    * @return the download url to the linux eclipse distribution
    */
   public String getLinuxDownloadUrl() {
      return linuxDownloadUrl;
   }

   /**
    * Sets the download url to the linux eclipse distribution.
    * 
    * @param linuxDownloadUrl linux download url
    */
   public void setLinuxDownloadUrl(String linuxDownloadUrl) {
      this.linuxDownloadUrl = linuxDownloadUrl;
   }

   /**
    * Returns the name of the linux eclipse version.
    * 
    * @return the name of the linux eclipse version
    */
   public String getLinuxVersion() {
      return linuxVersion;
   }

   /**
    * Sets the name of the linux eclipse version.
    * 
    * @param linuxEclipseVersion linux eclipse version
    */
   public void setLinuxVersion(String linuxEclipseVersion) {
      this.linuxVersion = linuxEclipseVersion;
   }

   /**
    * Returns the download url to the windows eclipse distribution.
    * 
    * @return the download url to the windows eclipse distribution
    */
   public String getWindowsDownloadUrl() {
      return windowsDownloadUrl;
   }

   /**
    * Sets the download url to the windows eclipse distribution.
    * 
    * @param windowsDownloadUrl windows download url
    */
   public void setWindowsDownloadUrl(String windowsDownloadUrl) {
      this.windowsDownloadUrl = windowsDownloadUrl;
   }

   /**
    * Returns the name of the windows eclipse version.
    * 
    * @return the name of the windows eclipse version
    */
   public String getWindowsVersion() {
      return windowsVersion;
   }

   /**
    * Sets the name of the windows eclipse version.
    * 
    * @param windowsEclipseVersion windows eclipse version
    */
   public void setWindowsVersion(String windowsEclipseVersion) {
      this.windowsVersion = windowsEclipseVersion;
   }

   /**
    * Returns the provider of the download url to the eclipse distribution.
    * 
    * @return the provider of the download url to the eclipse distribution
    */
   public Provider<String> getDownloadUrl() {
      return project.getProviders().provider(() -> isLinux() ? getLinuxDownloadUrl() : getWindowsDownloadUrl());
   }

   /**
    * Returns the provider of the name of the eclipse version.
    * 
    * @return the provider of the name of the eclipse version
    */
   public Provider<String> getEclipseVersion() {
      return project.getProviders().provider(() -> isLinux() ? getLinuxVersion() : getWindowsVersion());
   }

   /**
    * Returns the provider of the eclipse archive zip file.
    * 
    * @return the provider of the eclipse archive zip file
    */
   public Provider<RegularFile> getArchive() {
      return cacheDirectory.file(project.getProviders().provider(() -> getEclipseVersion().get() + ".zip"));
   }

   /**
    * Returns the provider of the directory of the unzipped eclipse distribution.
    * 
    * @return the provider of the directory of the unzipped eclipse distribution
    */
   public Provider<Directory> getDistributionDirectory() {
      return cacheDirectory.dir(getEclipseVersion());
   }

   /**
    * Returns the directory where eclipse plugins are stored.
    * 
    * @return the directory where eclipse plugins are stored
    */
   public File getPluginsDirectory() {
      return getPluginsDirectoryProperty().get().getAsFile();
   }

   /**
    * Returns the provider of the directory where eclipse plugins are stored.
    * 
    * @return the provider of the directory where eclipse plugins are stored
    */
   public Provider<Directory> getPluginsDirectoryProperty() {
      return getDistributionDirectory().map(dir -> dir.dir("plugins"));
   }

   /**
    * Returns the provider of the eclipse executable file.
    * 
    * @return the provider of the eclipse executable file
    */
   public Provider<RegularFile> getExecutable() {
      return getDistributionDirectory().map(dir -> dir.file("eclipse" + (isLinux() ? "" : "c.exe")));
   }

   /**
    * Returns whether or not a directory repository should be created for the eclipse plugins.
    * 
    * @return whether or not a directory repository should be created for the eclipse plugins
    */
   public boolean isPluginsRepository() {
      return pluginsRepository;
   }

   /**
    * Sets whether or not a directory repository should be created for the eclipse plugins.
    * 
    * @param pluginsRepository whether or not a directory repository should be created for the eclipse plugins
    */
   public void setPluginsRepository(boolean pluginsRepository) {
      this.pluginsRepository = pluginsRepository;
   }

   /**
    * Enables the plugin to create a directory repository for the eclipse plugins.
    */
   public void enablePluginsRepository() {
      this.pluginsRepository = true;
   }

   private boolean isLinux() {
      if (OperatingSystem.current().isLinux()) {
         return true;
      } else if (OperatingSystem.current().isWindows()) {
         return false;
      }
      throw new IllegalStateException("Invalid operating system: " + OperatingSystem.current());
   }
}
