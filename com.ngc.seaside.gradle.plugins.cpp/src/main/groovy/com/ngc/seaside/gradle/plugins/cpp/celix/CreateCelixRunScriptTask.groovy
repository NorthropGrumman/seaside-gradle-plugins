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
