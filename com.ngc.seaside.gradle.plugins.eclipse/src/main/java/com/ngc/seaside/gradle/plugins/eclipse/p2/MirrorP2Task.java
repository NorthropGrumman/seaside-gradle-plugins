package com.ngc.seaside.gradle.plugins.eclipse.p2;

import com.ngc.seaside.gradle.plugins.eclipse.AbstractEclipseApplicationTask;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import java.util.Arrays;

public class MirrorP2Task extends AbstractEclipseApplicationTask<MirrorP2Task> {

   private DirectoryProperty p2Directory;
   private Property<String> url;

   /**
    * Constructor.
    */
   public MirrorP2Task() {
      super(MirrorP2Task.class);
      this.p2Directory = newOutputDirectory();
      this.url = getProject().getObjects().property(String.class);
      getArgumentProviders().add(() -> Arrays.asList("-nosplash", "-application",
               "org.eclipse.equinox.p2.artifact.repository.mirrorApplication",
               "-source", url.get(), "-destination", p2Directory.get().getAsFile().toString()));
   }

   /**
    * Returns the property of the directory for mirroring the p2 repository.
    * 
    * @return the property of the directory for mirroring the p2 repository
    */
   @Internal
   public DirectoryProperty getP2Directory() {
      return p2Directory;
   }

   /**
    * Returns the property of the p2 repository url.
    * 
    * @return the property of the p2 repository url
    */
   @Input
   public Property<String> getP2Url() {
      return url;
   }
}
