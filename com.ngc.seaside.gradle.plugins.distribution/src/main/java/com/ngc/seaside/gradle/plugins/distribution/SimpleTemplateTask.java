package com.ngc.seaside.gradle.plugins.distribution;

import com.google.common.base.Preconditions;

import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A task for converting a template to a file using find-and-replace properties.
 */
public class SimpleTemplateTask extends DefaultTask {

   @Input
   private String templateText;

   @Input
   private Map<String, String> properties = new LinkedHashMap<>();

   @OutputFile
   private File destination;

   @TaskAction
   public void convertTemplate() throws IOException {
      String text = templateText;
      for (Map.Entry<String, String> entry : properties.entrySet()) {
         String key = entry.getKey();
         String value = entry.getValue();
         text = text.replace(key, value);
      }
      Files.write(destination.toPath(), text.getBytes());
   }

   /**
    * Sets the location of the template. The template can either be a URL, or a file-like object resolved using
    * {@link Project#file(Object)}.
    * 
    * @param template the location of the template
    * @return this
    */
   public SimpleTemplateTask setTemplate(Object template) {
      Preconditions.checkNotNull(template, "template cannot be null");
      if (template instanceof URL) {
         URL url = (URL) template;
         try (InputStream in = url.openStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            IOUtils.copy(in, out);
            templateText = new String(out.toByteArray());
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      } else {
         try {
            templateText = new String(Files.readAllBytes(getProject().file(template).toPath()));
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      }
      return this;
   }

   /**
    * Sets the properties for the template. When the task is executed, the task will simply find and replace
    * all keys in the template with their corresponding values.
    * 
    * @param properties template properties
    * @return this
    */
   public SimpleTemplateTask setTemplateProperties(Map<String, String> properties) {
      Preconditions.checkNotNull(properties, "properties cannot be null");
      this.properties.clear();
      for (Map.Entry<String, String> entry : properties.entrySet()) {
         String key = entry.getKey();
         String value = entry.getValue();
         Preconditions.checkNotNull(key, "properties cannot contain a null key");
         Preconditions.checkArgument(!key.isEmpty(), "properties cannot contain an empty key");
         Preconditions.checkNotNull(value, "properties cannot contain a null value");
         this.properties.put(key, value);
      }
      return this;
   }

   /**
    * Sets the destination of the post-processed template. The value of the parameter will be resolved using
    * {@link Project#file(Object)}.
    * 
    * @param file destination file of the post-processed template
    * @return this
    */
   public SimpleTemplateTask setDestination(Object file) {
      Preconditions.checkNotNull(file, "file cannot be null");
      destination = getProject().file(file);
      return this;
   }

   /**
    * Returns the destination of the post-processed template.
    * 
    * @return the destination of the post-processed template
    */
   public File getDestination() {
      return destination;
   }

}
