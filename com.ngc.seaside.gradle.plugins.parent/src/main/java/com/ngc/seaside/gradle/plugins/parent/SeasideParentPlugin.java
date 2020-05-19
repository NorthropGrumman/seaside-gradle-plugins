/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.parent;

import com.github.benmanes.gradle.versions.VersionsPlugin;
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask;
import com.github.ksoichiro.console.reporter.ConsoleReporterPlugin;
import com.ngc.seaside.gradle.api.AbstractProjectPlugin;
import com.ngc.seaside.gradle.plugins.checkstyle.SeasideCheckstylePlugin;
import com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin;
import com.ngc.seaside.gradle.plugins.maven.SeasideMavenPlugin;
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin;
import com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryPlugin;
import com.ngc.seaside.gradle.tasks.dependencies.DependencyReportTask;
import com.ngc.seaside.gradle.tasks.dependencies.DownloadDependenciesTask;
import com.ngc.seaside.gradle.util.GradleUtil;
import com.ngc.seaside.gradle.util.Versions;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.sonarqube.gradle.SonarQubeExtension;
import org.sonarqube.gradle.SonarQubePlugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

/**
 * The seaside parent plugin provides calls to common tasks, sets up the default dependencies for BLoCS and OSGi along
 * with providing nexus repository deployment settings.
 *
 * The following properties are required in your ~/.gradle/gradle.properties file to use this plugin.
 * <pre>
 *     nexusUsername     : the username to use when uploading artifacts to nexus
 *     nexusPassword     : the password to use when uploading artifacts to nexus
 *     nexusReleases     : url to the releases repository
 *     nexusSnapshots    : url to the snapshots repository
 *     nexusConsolidated : url to the maven public download site
 *                         usually a proxy to maven central and the releases and snapshots
 *     systemProp.sonar.host.url : url to the Sonarqube server
 * </pre>
 */
public class SeasideParentPlugin extends AbstractProjectPlugin {

   public static final String PARENT_TASK_GROUP_NAME = "parent";
   public static final String SOURCE_JAR_TASK_NAME = "sourcesJar";
   public static final String JAVADOC_JAR_TASK_NAME = "javadocJar";
   public static final String ANALYZE_TASK_NAME = "analyze";
   public static final String DOWNLOAD_DEPENDENCIES_TASK_NAME = "downloadDependencies";
   public static final String DEPENDENCY_UPDATES_TASK_NAME = "dependencyUpdates";
   public static final String DEPENDENCY_REPORT_TASK_NAME = "dependencyReport";
   public static final String CLEANUP_DEPENDENCIES_TASK_NAME = "cleanupDependencies";
   public static final String ALL_TASK_NAME = "all";
   public static final String LOCAL_TAG = "local-";

   public static String REMOTE_TAG = "";

   @Override
   protected void doApply(Project project) {
      applyPlugins(project);
      createTasks(project);

      project.afterEvaluate(__ -> {
         TaskContainer tasks = project.getTasks();
         ExtensionContainer extensions = project.getExtensions();
         project.getLogger().lifecycle(project.getName() + ": Setting project version to " + project.getVersion());
         // Ensure to add the doclint option to the javadoc task if using Java 8.
         if (JavaVersion.current().isJava8Compatible()) {
            tasks.named(JavaPlugin.JAVADOC_TASK_NAME).configure(task -> {
               CoreJavadocOptions options = (CoreJavadocOptions) ((Javadoc) task).getOptions();
               options.addStringOption("Xdoclint:none", "-quiet");
            });
         }
         if (JavaVersion.current().isJava9Compatible()) {
            tasks.named(JavaPlugin.JAVADOC_TASK_NAME).configure(task -> {
               CoreJavadocOptions options = (CoreJavadocOptions) ((Javadoc) task).getOptions();
               //options.addBooleanOption("html5", true);
            });
         }
         extensions.getByType(ExtraPropertiesExtension.class).set("bundleName",
                                                                  project.getGroup() + "." + project.getName());
         tasks.withType(DependencyUpdatesTask.class).named(DEPENDENCY_UPDATES_TASK_NAME).configure(task -> {
            task.setOutputDir("build/" + DEPENDENCY_UPDATES_TASK_NAME);
         });
         tasks.withType(Jar.class).named(SOURCE_JAR_TASK_NAME).configure(task -> {
            task.setArchiveName(project.getGroup() + "." + project.getName() + "-" + project.getVersion() + "-"
                                      + task.getClassifier() + ".jar");
         });
         tasks.withType(Jar.class).named(JAVADOC_JAR_TASK_NAME).configure(task -> {
            task.setArchiveName(project.getGroup() + "." + project.getName() + "-" + project.getVersion() + "-"
                                      + task.getClassifier() + ".jar");
         });
         tasks.withType(Jar.class).named(JavaPlugin.JAR_TASK_NAME).configure(task -> {
            task.setArchiveName(project.getGroup() + "." + project.getName() + "-" + project.getVersion() + ".jar");
            // TODO: can we just apply the bnd plugin here?
            task.manifest(manifest -> {
               Object bundleName = extensions.getByType(ExtraPropertiesExtension.class).get("bundleName");
               manifest.getAttributes().put("Bundle-Name", bundleName.toString());
               manifest.getAttributes().put("Bundle-SymbolicName", bundleName.toString());
               manifest.getAttributes().put("Bundle-Version",
                                            Versions.makeOsgiCompliantVersion(project.getVersion().toString()));
            });
         });
         extensions.getByType(SonarQubeExtension.class).properties(properties -> {
            if (new File(project.getBuildDir().getPath() + "/jacoco/test.exec").exists()) {
               properties.property("sonar.jacoco.reportPaths",
                                   Collections.singletonList(project.getBuildDir().getPath() + "/jacoco/test.exec"));
            }
            Object bundleName = extensions.getByType(ExtraPropertiesExtension.class).get("bundleName");
            properties.property("sonar.projectName", bundleName.toString());
            properties.property("sonar.branch", getBranchName(project));
         });
         tasks.named(SonarQubeExtension.SONARQUBE_TASK_NAME).configure(task -> {
            task.doFirst(___ -> GradleUtil.requireSystemProperties(project.getProperties(), "sonar.host.url"));
         });
      });
   }

   /**
    * This will get the current working branch to pass to sonarqube
    *
    * @return String with of the Git Branch you are on otherwise an empty string
    */
   private String getBranchName(Project project) {
      StringBuilder branchName = new StringBuilder();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      project.exec(spec -> {
         spec.executable("git");
         spec.args("rev-parse", "--abbrev-ref", "HEAD");
         spec.setStandardOutput(out);
      });

      if (isBuildLocal()) {
         branchName.append(LOCAL_TAG);
      } else {
         branchName.append(REMOTE_TAG);
      }
      branchName.append(out.toString().trim());
      return branchName.toString().trim();
   }

   /**
    * @return true if local and false if not
    */
   private static boolean isBuildLocal() {
      boolean isLocal = true;
      if (System.getenv("JENKINS_HOME") != null) {
         REMOTE_TAG = "jenkins-";
         isLocal = false;
      }
      return isLocal;
   }

   private static void applyPlugins(Project project) {
      PluginContainer plugins = project.getPlugins();
      plugins.apply(JavaLibraryPlugin.class);
	  plugins.apply("biz.aQute.bnd.builder");
      plugins.apply(SeasideMavenPlugin.class);
      plugins.apply(SeasideRepositoryPlugin.class);
      plugins.apply(EclipsePlugin.class);
      plugins.apply(SonarQubePlugin.class);
      plugins.apply(VersionsPlugin.class);
      plugins.apply(ConsoleReporterPlugin.class);
      plugins.apply(SeasideReleasePlugin.class);
      plugins.apply(SeasideCiPlugin.class);
      plugins.apply(SeasideCheckstylePlugin.class);
   }

   private void createTasks(Project project) {
      TaskContainer tasks = project.getTasks();
      ExtensionContainer extensions = project.getExtensions();
      tasks.register(SOURCE_JAR_TASK_NAME, Jar.class, task -> {
         task.setClassifier("sources");
         JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
         task.from(convention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getAllSource());
         task.setGroup(PARENT_TASK_GROUP_NAME);
         task.dependsOn(JavaPlugin.CLASSES_TASK_NAME);
         PublishingExtension extension = extensions.getByType(PublishingExtension.class);
         extension.publications(publications -> {
            MavenPublication publication = (MavenPublication) publications
                  .getByName(SeasideMavenPlugin.MAVEN_JAVA_PUBLICATION_NAME);
            publication.artifact(task);
         });
      });
      tasks.register(JAVADOC_JAR_TASK_NAME, Jar.class, task -> {
         task.setClassifier("javadoc");
         task.from(tasks.named(JavaPlugin.JAVADOC_TASK_NAME));
         task.setGroup(PARENT_TASK_GROUP_NAME);
         task.dependsOn(JavaPlugin.JAVADOC_TASK_NAME, JavaPlugin.CLASSES_TASK_NAME);
         PublishingExtension extension = extensions.getByType(PublishingExtension.class);
         extension.publications(publications -> {
            MavenPublication publication = (MavenPublication) publications
                  .getByName(SeasideMavenPlugin.MAVEN_JAVA_PUBLICATION_NAME);
            publication.artifact(task);
         });
      });

      tasks.register(ANALYZE_TASK_NAME, task -> {
         task.dependsOn(LifecycleBasePlugin.BUILD_TASK_NAME);
         task.dependsOn(SonarQubeExtension.SONARQUBE_TASK_NAME);
         task.setDescription("Runs sonarqube");
         task.setGroup(PARENT_TASK_GROUP_NAME);
      });
      tasks.register(DOWNLOAD_DEPENDENCIES_TASK_NAME, DownloadDependenciesTask.class, task -> {
         task.setGroup(PARENT_TASK_GROUP_NAME);
         task.setDescription("Downloads all dependencies into the build/dependencies/ folder using maven2 layout.");
      });
      tasks.register(CLEANUP_DEPENDENCIES_TASK_NAME, DownloadDependenciesTask.class, task -> {
         task.setCustomRepo(project.getBuildDir().getPath() + "/dependencies-tmp");
         task.setGroup(PARENT_TASK_GROUP_NAME);
         task.setDescription("Remove unused dependencies from dependencies folder.");
         task.doLast(__ -> {
            DownloadDependenciesTask downloadDependencies =
                  (DownloadDependenciesTask) tasks.getByName(DOWNLOAD_DEPENDENCIES_TASK_NAME);
            final File localRepo = task.getLocalRepository();
            File otherRepo = downloadDependencies.getLocalRepository();
            if (otherRepo == null) {
               otherRepo = project.file(project.getBuildDir().getPath() + "/dependencies");
            }
            final File actualRepo = localRepo;
            extensions.getByType(ExtraPropertiesExtension.class).set("actualRepository", actualRepo);
            project.getLogger().info("Moving cleaned up repository from " + localRepo.getAbsolutePath() + " to "
                                           + actualRepo.getAbsolutePath() + ".");
            project.delete(actualRepo);
            project.copy(spec -> {
               spec.from(localRepo);
               spec.into(actualRepo);
            });
            project.delete(localRepo);
         });
      });
      tasks.register(DEPENDENCY_REPORT_TASK_NAME, DependencyReportTask.class, task -> {
         task.setDescription(
               "Lists all dependencies. Use -DshowTransitive=<bool> to show/hide transitive dependencies");
      });
      tasks.register(ALL_TASK_NAME, task -> {
         task.dependsOn(LifecycleBasePlugin.BUILD_TASK_NAME, JAVADOC_JAR_TASK_NAME, SOURCE_JAR_TASK_NAME);
         task.setDescription("Performs a full build and generates all artifacts including Javadoc and source JARs.");
         task.setGroup(PARENT_TASK_GROUP_NAME);
      });
      tasks.withType(PublishToMavenRepository.class).configureEach(task -> {
         task.dependsOn(JAVADOC_JAR_TASK_NAME, SOURCE_JAR_TASK_NAME);
      });
   }
}
