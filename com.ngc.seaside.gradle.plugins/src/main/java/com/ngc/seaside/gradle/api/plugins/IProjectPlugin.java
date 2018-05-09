package com.ngc.seaside.gradle.api.plugins;

import com.ngc.seaside.gradle.util.TaskResolver;
import com.ngc.seaside.gradle.util.VersionResolver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public interface IProjectPlugin extends Plugin<Project> {

   /**
    * @return instance of TaskResolver instance from parent
    */
   TaskResolver getTaskResolver();

   /**
    * @return instance of VersionResolver instance from parent
    */
   VersionResolver getVersionResolver();
   
}
