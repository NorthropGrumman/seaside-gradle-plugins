package com.ngc.seaside.gradle.tasks.dependencies

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import com.ngc.seaside.gradle.plugins.util.TreeNode
import com.ngc.seaside.gradle.plugins.util.TreePath

/**
 * Gradle-Task that downloads all dependencies into a local directory based repository.
 */
class DependencyReportTask extends DefaultTask {

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
       */
      if (project.getParent().getSubprojects()[0] == project) {
         TreeNode root = new TreeNode(new TreePath(project.getParent().name), projectDescription,
                                      new TreeNode.NameComparator())

         project.getParent().getSubprojects().each { subProject ->
            subProject.configurations.each { configuration ->
               configuration.setTransitive(true)
            }
            root = buildDependenciesForProject(subProject, root)
         }

         printReport(project.getParent(), root)
      }
   }

   void printReport(Project project, TreeNode root) {

      if(!project.getBuildDir().exists()) {
         if(!project.getBuildDir().mkdir()){
            project.getLogger().info("Failed to make directory  ${project.getBuildDir()}")
         }
      }

      def report = new File(project.getBuildDir(), project.name + "_DependencyReport.txt")

      report.write("Non-Transitive Dependencies")
      report << "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
      report << "\n" + stringifyTransients(root)

      report << "\n \nHierarchy of Dependencies"
      report << "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
      report << "\n" + stringifyNode(root, "")
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
      String str = ""

      if (node.isRoot()) {
         str += node.name
         str += "\n"
         indent += "    "
      } else {
         str += indent
         if (node.getParent().indexOfChild(node.name) < node.getParent().getChildren().size() - 1) {
            indent += "|   "
         } else {
            indent += "    "
         }

         str += "|--"
         str += node.name
         str += "\n"
      }
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
   TreeNode buildDependenciesForProject(Project currentProject, TreeNode root) {
      TreeNode subProjectNode = new TreeNode(new TreePath(root.getPath(), currentProject.name), projectDescription,
                                             new TreeNode.NameComparator())
      root.addChild(subProjectNode)

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
                           new TreePath(subProjectNode.getPath(), it.selected.id.toString()), dependencyDescription,
                           new TreeNode.NameComparator())
                     if (!subProjectNode.addChild(nonTransitiveNode)) {
                        project.getLogger().info("Failed to add  ${nonTransitiveNode} to parent node ${subProjectNode}")
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

      return root
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
