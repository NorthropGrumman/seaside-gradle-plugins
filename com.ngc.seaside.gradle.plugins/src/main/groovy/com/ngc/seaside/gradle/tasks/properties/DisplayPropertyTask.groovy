package com.ngc.seaside.gradle.tasks.properties

import com.google.common.base.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.TaskAction

class DisplayPropertyTask extends DefaultTask {

    @Option(option = 'name',
          description = 'Sets the name of the property to get the value of.')
    String propertyName

    @TaskAction
    void displayPropertyValue() {
        Preconditions.checkState(propertyName != null && propertyName.trim() != '',
                                 "propertyName may not be null or empty!")
        def value = project.findProperty(propertyName)
        project.logger.quiet(value == null ? '' : value.toString())
    }
}
