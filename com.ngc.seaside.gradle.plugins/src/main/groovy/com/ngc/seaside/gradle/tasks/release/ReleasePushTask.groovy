package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Pushes the released version to GitHub with the latest created tag
 */
class ReleasePushTask extends DefaultTask {

   /**
    * CTOR
    */
   ReleasePushTask() {}

   /**
    * function required to be a task within the gradle framework
    */
    @TaskAction
    def releasePush() {
       pushChanges()
    }

   /**
    * This will grab the last created tag and then push changes to gitHub
    */
   private void pushChanges() {
        def tag = git("describe", "--abbrev=0", "--tags")
        git("push", "origin", tag)
        git("push", "origin", "HEAD")
    }

   /**
    *
    * @param arguments
    * @return The return from the git call
    */
   private String git(Object[] arguments) {

      def output = new ByteArrayOutputStream()

      project.exec {
         executable "git"
         args arguments
         standardOutput output
         ignoreExitValue = true
      }
      output = output.toString().trim()
      if (!output.isEmpty()) {
         project.logger.debug(output)
      }

      return output
   }

}
