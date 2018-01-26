package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import com.ngc.seaside.gradle.tasks.DefaultTaskAction;
import com.ngc.seaside.gradle.util.GradleUtil;

import org.eclipse.aether.resolution.ArtifactResult;
import org.gradle.api.InvalidUserDataException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class CreateM2DeploymentScriptAction extends DefaultTaskAction<PopulateMaven2Repository> {

   private ArtifactResultStore store;

   @Override
   public void validate(PopulateMaven2Repository task) throws InvalidUserDataException {
      GradleUtil.checkUserData(!task.isCreateDeployScript() || task.getDeployScriptFile() != null,
                               "deployScriptFile must be set!");
   }


   public CreateM2DeploymentScriptAction setStore(ArtifactResultStore store) {
      this.store = store;
      return this;
   }

   @Override
   protected void doExecute() {
      if (task.isCreateDeployScript()) {
         Preconditions.checkState(store != null, "store must be set!");
         createScript();
      }
   }

   static String formatMavenDeployCommand(ArtifactResult mainArtifact,
                                          ArtifactResultStore store,
                                          Path scriptFile) {
      StringBuilder sb = new StringBuilder("mvn deploy:deploy-file")
            .append(" --settings ${SETTINGS}")
            .append(" -Durl=${URL}")
            .append(" -DrepositoryId=${REPO}")
            .append(" -Dfile=")
            .append(relativizeToParentOf(scriptFile, store.getRelativePathToMainArtifact(mainArtifact)))
            .append(" -DpomFile=")
            .append(relativizeToParentOf(scriptFile, store.getRelativePathToPom(mainArtifact)));
      if (store.hasOtherClassifiers(mainArtifact)) {
         Stream<String> files = store.getRelativePathsToOtherClassifiers(mainArtifact)
               .stream()
               .map(p -> relativizeToParentOf(scriptFile, p).toString());

         sb.append(" -Dclassifiers=")
               .append(String.join(",", store.getOtherClassifiers(mainArtifact)))
               .append(" -Dtypes=")
               .append(String.join(",", store.getOtherExtensions(mainArtifact)))
               .append(" -Dfiles=")
               .append(String.join(",", (Iterable<String>) files::iterator));
      }
      return sb.toString();
   }

   private void createScript() {
      Set<String> lines = new TreeSet<>();

      Path script = task.getDeployScriptFile().toPath();
      for (ArtifactResult mainResult : store.getMainResults()) {
         lines.add(formatMavenDeployCommand(mainResult, store, script));
      }
      writeLines(lines);
   }

   private void writeLines(Set<String> lines) {
      // Create parent directories if needed.
      File dir = task.getDeployScriptFile().getParentFile();
      if (dir != null && !dir.isDirectory()) {
         dir.mkdirs();
      }

      try {
         Files.write(task.getDeployScriptFile().toPath(),
                     lines,
                     StandardOpenOption.WRITE,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.APPEND);
      } catch (IOException e) {
         logger.error("Unexpected error while creating deployment script at {}.", e, task.getDeployScriptFile());
      }
   }

   private static Path relativizeToParentOf(Path path, Path other) {
      Path relative = other;
      if (path.getParent() != null) {
         relative = path.getParent().relativize(other);
      }
      return relative;
   }
}
