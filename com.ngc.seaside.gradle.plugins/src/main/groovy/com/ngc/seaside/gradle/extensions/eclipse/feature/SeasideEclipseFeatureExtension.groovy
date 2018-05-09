package com.ngc.seaside.gradle.extensions.eclipse.feature

import com.ngc.seaside.gradle.util.Versions
import org.gradle.api.Project

/**
 * Extension for the seaside eclipse feature plugin.
 */
class SeasideEclipseFeatureExtension {
   /**
    * The archive name of the feature jar. By default this is {@code group.artifact-version.jar}
    */
   String archiveName

   /**
    * Properties in the feature.xml file that will get expanded to their corresponding values when copied on build.
    * @see groovy.text.SimpleTemplateEngine#SimpleTemplateEngine()
    */
   Map<String, ?> templateProperties

   /**
    * Create an instance of the SeasideEclipseFeatureExtension
    * @param project the project on which to create the extension
    */
   SeasideEclipseFeatureExtension(Project project) {
      archiveName = "${project.group}.${project.name}-${project.version}.jar"
      templateProperties = new LinkedHashMap()
      // Set the default version to an Eclipse acceptable value.
      templateProperties.put('version', Versions.makeOsgiCompliantVersion(project.getVersion().toString()))
   }
}
