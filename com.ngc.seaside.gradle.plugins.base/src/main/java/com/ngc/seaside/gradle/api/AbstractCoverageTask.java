/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
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
