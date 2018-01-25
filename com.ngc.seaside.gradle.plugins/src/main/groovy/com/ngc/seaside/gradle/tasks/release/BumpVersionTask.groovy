package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BumpVersionTask extends DefaultTask {

   /**
    * CTOR
    */
   BumpVersionTask(){}

   /**
    * function required to be a task within the gradle framework
    *
    * @return
    */
   @TaskAction
   def bumpTheVersion() {

   }
}
