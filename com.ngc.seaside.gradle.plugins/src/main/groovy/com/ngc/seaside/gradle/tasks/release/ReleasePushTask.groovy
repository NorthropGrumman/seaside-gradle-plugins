package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.plugins.release.SeasideReleaseRootProjectPlugin
import com.ngc.seaside.gradle.util.ReleaseUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Pushes the released version to GitHub with the latest created tag
 */
class ReleasePushTask extends DefaultTask {

   // Will be used in the future
   boolean dryRun = false

   /**
    * CTOR
    */
   ReleasePushTask() {}

   /**
    * function required to be a task within the
    * gradle framework and is the entry point for
    * gradle
    *
    * @return
    */
    @TaskAction
    def releasePush() {
       pushChanges()
    }

   /**
    * push all the committed changes done for the release to our GitHub repository
    */
   private void pushChanges() {
      if (dryRun) {
         project.logger.lifecycle("Would have Pushed Release")
         project.exec ReleaseUtil.git("reset", "--hard", "origin")
      }
      else{
         project.logger.lifecycle("Pushing Release")
         project.exec ReleaseUtil.git("push", "origin", "--tags")
         project.exec ReleaseUtil.git("push", "origin", "HEAD")
      }
    }


}
