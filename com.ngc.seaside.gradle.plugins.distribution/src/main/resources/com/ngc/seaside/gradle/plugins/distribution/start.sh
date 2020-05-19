#!/bin/sh
#
# UNCLASSIFIED
#
# Copyright 2020 Northrop Grumman Systems Corporation
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of
# this software and associated documentation files (the "Software"), to deal in
# the Software without restriction, including without limitation the rights to use,
# copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
# Software, and to permit persons to whom the Software is furnished to do so,
# subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
# INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
# PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
# OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
# SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#


# Run this script to start the application.

SCRIPT_DIRECTORY="\$( cd "\$( dirname "\${BASH_SOURCE[0]}" )" && pwd )"
export NG_FW_HOME=`dirname "\$SCRIPT_DIRECTORY"`
FRAMEWORK_OPTS="-DNG_FW_HOME=\$NG_FW_HOME -Djavax.xml.accessExternalSchema=all -Dfelix.config.properties=file:\$NG_FW_HOME/platform/configuration/config.properties" ${FELIX_JVM_PROPERTIES}
FELIX_OPTS="\$NG_FW_HOME/platform/cache -b \$NG_FW_HOME/platform" ${FELIX_PROGRAM_ARGUMENTS}
MAIN_JAR="\$( find "\$NG_FW_HOME/platform" -name 'org.apache.felix.main-*.jar' | head -1 )"

# Require JAVA_HOME to be set.
: \${JAVA_HOME:?"Environment variable JAVA_HOME not set!  This variable must be set."}

"\$JAVA_HOME/bin/java" "\$@" \$FRAMEWORK_OPTS -jar "\$MAIN_JAR" \$FELIX_OPTS
