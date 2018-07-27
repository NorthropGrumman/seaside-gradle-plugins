package com.ngc.seaside.gradle.plugins.eclipse;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Task for downloading an Eclipse distribution.
 */
public class DownloadEclipseTask extends DefaultTask {

   private final RegularFileProperty eclipseArchive;
   private final Property<String> eclipseDownloadUrl;

   public DownloadEclipseTask() {
      this.eclipseArchive = newInputFile();
      this.eclipseDownloadUrl = getProject().getObjects().property(String.class);
   }

   /**
    * Download the Eclipse SDK from the specified URL, if it doesn't already exist in the specified location.
    */
   @TaskAction
   public void downloadEclipse() {
      File destFile = eclipseArchive.get().getAsFile();
      destFile.getParentFile().mkdirs();

      if (!destFile.exists()) {
         getProject().getLogger().lifecycle("Downloading Eclipse SDK from " + eclipseDownloadUrl + "...");
         try (InputStream is = new URL(eclipseDownloadUrl.get()).openStream()) {
            FileUtils.copyInputStreamToFile(is, destFile);
         } catch (IOException e) {
            throw new TaskExecutionException(this, e);
         }
      }
   }

   /**
    * Returns the property of the download destination of the Eclipse distribution archive zip.
    * 
    * @return the property of the download destination of the Eclipse distribution archive zip
    */
   @Internal
   public RegularFileProperty getEclipseArchive() {
      return eclipseArchive;
   }

   /**
    * Returns the property of the url for the Eclipse distribution archive zip.
    * 
    * @return the property of the url for the Eclipse distribution archive zip
    */
   @Input
   public Property<String> getEclipseDownloadUrl() {
      return eclipseDownloadUrl;
   }

}
