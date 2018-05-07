package com.ngc.seaside.gradle.api;

import org.gradle.api.DefaultTask;

public class AbstractCoverageTask extends DefaultTask {

   /**
    * Finds the lcov archive location within the project classpath
    * 
    * @param filename archive filename
    * @return path to archive filename
    */
   protected String findTheReleaseArchiveFile(String filename) {
      return getProject().getConfigurations()
                         .getByName("compile")
                         .filter(file -> file.getName().endsWith(filename))
                         .getAsPath();
   }
}
