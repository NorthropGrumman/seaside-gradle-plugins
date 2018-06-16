#!/bin/sh
#
#
#  Northrop Grumman Proprietary
#  ____________________________
#
#   Copyright (C) 2018, Northrop Grumman Systems Corporation
#   All Rights Reserved.
#
#  NOTICE:  All information contained herein is, and remains the property of
#  Northrop Grumman Systems Corporation. The intellectual and technical concepts
#  contained herein are proprietary to Northrop Grumman Systems Corporation and
#  may be covered by U.S. and Foreign Patents or patents in process, and are
#  protected by trade secret or copyright law. Dissemination of this information
#  or reproduction of this material is strictly forbidden unless prior written
#  permission is obtained from Northrop Grumman.
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
