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

currentDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# convert script to unix format
find . -type f -name "*.sh" -exec sed -i 's/\x0d//g' {} \+

# make sure scripts are executable
find ./ -name "*.sh" -exec chmod +x {} \;

printf "Starting services...\n"
for folder in `ls -d $currentDir/*/`; do
   printf "Entering $folder ...\n"
   $folder/bin/start.sh -Dgosh.args=--nointeractive "$@" &
done

wait
