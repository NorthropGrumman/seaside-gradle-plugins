#!/bin/bash
#
# UNCLASSIFIED
# Northrop Grumman Proprietary
# ____________________________
#
# Copyright (C) 2019, Northrop Grumman Systems Corporation
# All Rights Reserved.
#
# NOTICE:  All information contained herein is, and remains the property of
# Northrop Grumman Systems Corporation. The intellectual and technical concepts
# contained herein are proprietary to Northrop Grumman Systems Corporation and
# may be covered by U.S. and Foreign Patents or patents in process, and are
# protected by trade secret or copyright law. Dissemination of this information
# or reproduction of this material is strictly forbidden unless prior written
# permission is obtained from Northrop Grumman.
#


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

DEPENDENCY_FILE=$1
REPOSITORY_URL=$2
#see servers section in the settings.xml 
REPOSITORY_ID=nexus

#
# determine if the script is in test mode. This requires that a value of 
# test is sent in to the script for the REPOSITORY_URL
#
function isTest() {
  if [ "$REPOSITORY_URL" = "test" ]; then
   return 0;
  else
   return 1;
  fi
}

#
# Prompt the user for input
#
# param1: the return value
# param2: the key
#
function promptForInput() {
  local returnValue=$1
  local key=$2

  echo -n "$key:"
  read -s value

  eval $returnValue="'$value'"  
}

#
# Check the usage
# if we are in test mode, the prompt for username and password will be skipped.
#
if ! isTest; then
  if [ -z "$REPOSITORY_URL" ]; then
    echo "usage: ./deploy.sh <dependencies.tsv> <address_to_maven2_repository>"
    exit
  fi

  promptForInput USERNAME "Username"
  echo " "
  promptForInput PASSWORD "Password"
fi


#
# Deploy the given line from the dependencies tab separated value file.
#
# param1: the line array. Order is hard-coded based on latest dependencies.tsv file header
#
function deploy() {
  name=$1[@]
  arr=("${!name}")
  
  local group="${arr[0]}"
  local artifact="${arr[1]}"
  local version="${arr[2]}"
  local pomFile="${arr[3]}"
  local file="${arr[4]}"
  local packaging="${arr[5]}"
  local classifier="${arr[6]}"
  local files="${arr[7]}"
  local classifiers="${arr[8]}"
  local types="${arr[9]}"
  
  echo " "
  echo "deploying artifact '$group:$artifact:$version'"
  echo "----------------------------------------------------------------------"
  echo "pom        : $pomFile"
  echo "file       : $file"
  echo "packaging  : $packaging"
  echo "classifier : $classifier"
  echo "files      : $files"
  echo "classifiers: $classifiers"
  echo "types      : $types"
  echo "----------------------------------------------------------------------"
 
  local cmd="mvn deploy:deploy-file -s $DIR/settings.xml \
    -Dusername=$USERNAME -Dpassword=$PASSWORD \
    -DrepositoryId=$REPOSITORY_ID -Durl=$REPOSITORY_URL \
    -DpomFile=$pomFile -Dfile=$file -Dpackaging=$packaging"
    
  if [ ! -z $classifier ]; then
    cmd="$cmd -Dclassifier=$classifier"
  fi
  if [ ! -z $files ]; then
    cmd="$cmd -Dfiles=$files -Dclassifiers=$classifiers -Dtypes=$types"
  fi
     
  if ! isTest; then
     #Do not print the command, it will display the username and password  
     eval $cmd
  else
     echo "Would have run command : $cmd"
  fi
}

#
# Parse the dependencies file and upload the artifacts using Maven (mvn)
#
# param1: the file to read (usually called dependencies.tsv)
#
function parseAndUploadArtifacts() {
  local file=$1
  
  fileIndex=0
  declare -a header=()
  while IFS=$'\t' read -r -a lineArray; do  
    #save the header values in the header array
    if [ $fileIndex -eq 0 ]; then 
      for itr in ${!lineArray[@]}; do
        temp="${lineArray[$itr]}"
        temp="$(echo $temp | tr '\t' ' ')"
        header[$itr]=${temp%$'\n'}
      done #copy of the header
    else # process each line other than the header. 
      echo "Uploading file #$fileIndex"
      
      # minimum number of values on a line. the GAV, pom, file, packaging are always required.
      if [ ${#lineArray[@]} -ge 6 ]; then
        deploy lineArray
      fi # ensure array length is long enough (i.e. has all required fields)
      
    fi # end test for header 
    let fileIndex=fileIndex+1
  done < $file
}


parseAndUploadArtifacts "$DEPENDENCY_FILE"

