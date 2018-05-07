package com.ngc.seaside.gradle.api;

import com.ngc.seaside.gradle.util.TaskResolver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public interface IProjectPlugin extends Plugin<Project> {

   /**
    * @return instance of TaskResolver instance from parent
    */
   TaskResolver getTaskResolver();

}
