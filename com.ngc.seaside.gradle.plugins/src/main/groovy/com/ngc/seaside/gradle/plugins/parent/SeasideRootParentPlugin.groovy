package com.ngc.seaside.gradle.plugins.parent

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.tasks.properties.DisplayPropertyTask
import com.ngc.seaside.gradle.tasks.properties.SetPropertyTask
import org.gradle.api.Project

class SeasideRootParentPlugin extends AbstractProjectPlugin {

    @Override
    void doApply(Project project) {
        project.task('nothing') {
            //disabled = true
        }

        project.configure(project) {
            project.afterEvaluate {
                String displayPropertyName = System.getProperty("display.property.name")
                if(displayPropertyName != null) {
                    def value = project.findProperty(displayPropertyName)
                    project.logger.quiet(value == null ? '' : value.toString())
                }
            }

            project.beforeEvaluate {
                String updatePropertyName = System.getProperty("update.property.name")
                String updatePropertyValue = System.getProperty("update.property.value")
                if(updatePropertyName != null && updatePropertyValue != null) {
                    project.setProperty(updatePropertyName, updatePropertyValue)
                    project.logger.info(String.format("Set '%s' to '%s'.", updatePropertyName, updatePropertyValue))
                } else {
                    System.out.println("!@!@!@ NOTHING TO DO");
                }
            }
        }
    }
}
