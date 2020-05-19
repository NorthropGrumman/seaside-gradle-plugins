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
         String url = eclipseDownloadUrl.get();
         getProject().getLogger().lifecycle("Downloading Eclipse SDK from " + url + "...");
         try (InputStream is = new URL(url).openStream()) {
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
