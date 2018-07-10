@REM
@REM
@REM  Northrop Grumman Proprietary
@REM  ____________________________
@REM
@REM   Copyright (C) 2018, Northrop Grumman Systems Corporation
@REM   All Rights Reserved.
@REM
@REM  NOTICE:  All information contained herein is, and remains the property of
@REM  Northrop Grumman Systems Corporation. The intellectual and technical concepts
@REM  contained herein are proprietary to Northrop Grumman Systems Corporation and
@REM  may be covered by U.S. and Foreign Patents or patents in process, and are
@REM  protected by trade secret or copyright law. Dissemination of this information
@REM  or reproduction of this material is strictly forbidden unless prior written
@REM  permission is obtained from Northrop Grumman.
@REM

@echo off
rem Run this script to start the framework.  This script usually doesn't need
rem to be modified.  Set any custom environment variables in setenv.bat.

set CWD=%~dp0

rem Set environment.
for %%? in ("%~dp0..") do set NG_FW_HOME=%%~f?

rem The system properties that are set when the JVM is started.
set FRAMEWORK_OPTS=-DNG_FW_HOME="%NG_FW_HOME%" -Djavax.xml.accessExternalSchema=all -Dfelix.config.properties="file:%NG_FW_HOME%/platform/configuration/config.properties" ${FELIX_JVM_PROPERTIES}

rem The arguments passed to Felix when it is started.
set FELIX_OPTS="%NG_FW_HOME%/platform/cache" -b "%NG_FW_HOME%/platform" ${FELIX_PROGRAM_ARGUMENTS}

rem The main jar for running Felix
for /f %%i in ('dir "%NG_FW_HOME%\\platform\\org.apache.felix.main-*.jar" /s /b') do set MAIN_JAR=%%i

rem Require JAVA_HOME to be set.
if "%JAVA_HOME%" == "" goto :javaHomeNotSet

"%JAVA_HOME%\\bin\\java" %* %FRAMEWORK_OPTS% -jar "%MAIN_JAR%" %FELIX_OPTS%
goto :eof

:javaHomeNotSet
echo The required environment variable JAVA_HOME is not set!
goto :eof
