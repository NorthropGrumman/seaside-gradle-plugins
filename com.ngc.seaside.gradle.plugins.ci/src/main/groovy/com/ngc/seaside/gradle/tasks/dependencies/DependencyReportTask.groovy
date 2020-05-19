/*
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
package com.ngc.seaside.gradle.tasks.dependencies

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import com.ngc.seaside.gradle.util.TreeNode
import com.ngc.seaside.gradle.util.TreePath

/**
 * Gradle-Task that downloads all dependencies into a local directory based repository.
 */
class DependencyReportTask extends DefaultTask {

   boolean individualSubProjects = false
   String reportOutputDir = project.getProjectDir().path.toString()
   protected String projectDescription = "project"
   protected String dependencyDescription = "dependency"

   @TaskAction
   def dependencyReport() {

      /**
       * This structure is due to the need to compile all subproject dependencies in a project
       * in one method call, but the parent plugin is, as of this comment, exclusively applied to
       * subprojects. In order to correct that, this task will only actually perform its logic when
       * the first subproject is called, and it won't do anything for any subsequent subprojects. This
       * is so that each subproject that the task is run against doesn't crawl through the whole project
       * when it is ever only needed once.
       *
       * This may not work in the event that the seaside.parent plugin isn't applied to the first subproject.
       */

      Project tempProject
      if (individualSubProjects) {
         tempProject = project
      } else {
         tempProject = project.getParent()
      }

      TreeNode root = new TreeNode(new TreePath(project.getParent().name), projectDescription,
                                   new TreeNode.NameComparator())

      File outputDir
      if(reportOutputDir != project.getProjectDir().path){
         outputDir = new File(reportOutputDir)
      } else {
         if (individualSubProjects) {
            outputDir = tempProject.getProjectDir()
         } else {
            outputDir = tempProject.getBuildDir()
         }
      }

      if (tempProject.getSubprojects().size() > 0) {
         if (tempProject.getSubprojects()[0] == project) {
            tempProject.getSubprojects().each { subProject ->
               subProject.configurations.each { configuration ->
                  //Using a try-catch block since there doesn't seem to be a way to determine if the configuration
                  //was resolved prior to attempting the setTransitive method.
                  try {
                     configuration.setTransitive(true)
                  } catch(Exception e) {
                     project.getLogger().info("Failed to set configuration transitive property to true. ${configuration}")
                  }

               }
               if (!root.addChild(buildDependenciesForProject(subProject, root))) {
                  project.getLogger().info("Failed to add dependencies children to root node ${root}")
               }
            }

            printReport(tempProject, root, outputDir)
         }
      } else {
         if (!root.addChild(buildDependenciesForProject(tempProject, root))) {
            project.getLogger().info("Failed to add dependencies children to root node ${root}")
         }

         printReport(tempProject, root, outputDir)
      }
   }

   void printReport(Project project, TreeNode root, File outputDir) {

      if (!outputDir.exists()) {
         if (!outputDir.mkdir()) {
            project.getLogger().info("Failed to make directory  ${project.outputDir()}")
         }
      }

      def report = new File(outputDir, project.name + "_DependencyReport.txt")

      report.write("Non-Transitive Dependencies")
      report << "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n${stringifyTransients(root)}"

      report << "\n \nHierarchy of Dependencies"
      report << "\n~~~~~~~~~~~~~~~~~~~~~~~~~\n${stringifyNode(root, "")}"
   }

   Set gatherTransients(TreeNode node) {
      def dependencies = [] as Set
      if (node.description == projectDescription) {
         node.getChildren().each { childNode ->
            if (childNode.description == projectDescription) {
               dependencies.addAll(gatherTransients(childNode).collect())
            } else {
               dependencies.add(childNode.name)
            }
         }
      }

      return dependencies
   }

   String stringifyTransients(TreeNode node) {
      String outStr = ""
      gatherTransients(node).collect().each { dependency ->
         outStr += ("\n" + dependency)
      }

      return outStr
   }

   String stringifyNode(TreeNode node, String indent) {
      String str = indent

      if (node.isRoot()) {
         indent += "    "
      } else {
         if (node.getParent().indexOfChild(node.name) < node.getParent().getChildren().size() - 1) {
            indent += "|   "
         } else {
            indent += "    "
         }

         str += "|--"
      }

      str += node.name + "\n"

      for (TreeNode childNode in node.getChildren()) {
         str += stringifyNode(childNode, indent)
      }

      return str
   }

   /**
    *
    * @param currentProject the current top-level project
    * @param root the current root node of the dependency tree
    * @return
    */
   TreeNode buildDependenciesForProject(Project currentProject, TreeNode parentNode) {

      TreeNode projectNode = new TreeNode(new TreePath(parentNode.getPath(), currentProject.name), projectDescription,
                                          new TreeNode.NameComparator())

      if (!parentNode.addChild(projectNode)) {
         project.getLogger().info("Failed to add  ${projectNode} to parent node ${parentNode}")
      }

      (currentProject.configurations + currentProject.buildscript.configurations).each { configuration ->
         if (isConfigurationResolvable(configuration)) {
            Map<String, TreeNode> nodeMap = new HashMap<String, TreeNode>()
            //The nodeMap could potentially fail in the dependencies of a non-transitive dependency by
            //mistakenly putting a dependency branch onto the wrong dependency with the same id, but
            //this problem should be minor at most.

            configuration.incoming.resolutionResult.allDependencies.collect {
               if (it.hasProperty("from")) {

                  if (it.from.toString().contains("project")) {
                     nodeMap.clear()

                     TreeNode nonTransitiveNode = new TreeNode(
                           new TreePath(projectNode.getPath(), it.selected.id.toString()), dependencyDescription,
                           new TreeNode.NameComparator())

                     if (!projectNode.addChild(nonTransitiveNode)) {
                        project.getLogger().info("Failed to add  ${nonTransitiveNode} to parent node ${projectNode}")
                     }
                     nodeMap.put(it.selected.id.toString(), nonTransitiveNode)

                  } else {
                     if (nodeMap.containsKey(it.from.id.toString())) {

                        TreeNode transitiveNode = new TreeNode(
                              new TreePath(nodeMap.get(it.from.id.toString()).getPath(), it.selected.id.toString()),
                              dependencyDescription,
                              new TreeNode.NameComparator())

                        if (!nodeMap.get(it.from.id.toString()).addChild(transitiveNode)) {
                           project.getLogger().info(
                                 "Failed to add  ${transitiveNode} to parent node ${nodeMap.get(it.from.id.toString())}")
                        } else {
                           nodeMap.put(it.selected.id.toString(), transitiveNode)
                        }
                     } else {
                        project.getLogger().info("Expected to find ${it.from.id.toString()} in node map, but didn't")
                     }
                  }
               }
            }
         }
      }

      return projectNode
   }

   /**
    * Gradle 3.4 introduced the configuration 'apiElements' that isn't resolvable. So
    * we have to check before accessing it.
    */
   boolean isConfigurationResolvable(configuration) {
      if (!configuration.metaClass.respondsTo(configuration, 'isCanBeResolved')) {
         // If the recently introduced method 'isCanBeResolved' is unavailable, we
         // assume (for now) that the configuration can be resolved.
         return true
      }

      return configuration.isCanBeResolved()
   }
}
