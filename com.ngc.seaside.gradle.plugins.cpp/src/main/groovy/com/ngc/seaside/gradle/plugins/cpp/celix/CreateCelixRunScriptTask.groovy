/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.plugins.cpp.celix

import com.google.common.base.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * A task that generates a script file to run Celix with.  The actual script itself determines which bundles to start
 * so there is no need to make the script dynamic.
 */
class CreateCelixRunScriptTask extends DefaultTask {

    /**
     * The script file to write.
     */
    String scriptFile

    /**
     * The contents of the script.
     */
    String scriptTemplate = '''
#!/bin/sh

export BASE_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

export LD_LIBRARY_PATH=$BASE_DIRECTORY/lib/linux_x86_64:${LD_LIBRARY_PATH}

rm -rf .cache

# Generate the configuration for config.properties.
echo -n 'cosgi.auto.start.1=' > $BASE_DIRECTORY/config.properties
shopt -s nullglob
for b in $BASE_DIRECTORY/bundles/*.zip
do
    echo -n "$b " >> $BASE_DIRECTORY/config.properties
done

$BASE_DIRECTORY/bin/celix $BASE_DIRECTORY/config.properties $@
'''

    @TaskAction
    void createRunScript() {
        Preconditions.checkState(scriptFile != null && !scriptFile.trim().isEmpty(),
                                 "scriptFile may not be null or an empty string!")

        File file = getProject().file(scriptFile)
        // Create the parent directories if necessary.
        File parent = file.getParentFile()
        if (parent != null) {
            parent.mkdirs()
        }

        file.withPrintWriter { out ->
            out.println(scriptTemplate)
        }
    }
}
