@REM
@REM UNCLASSIFIED
@REM Northrop Grumman Proprietary
@REM ____________________________
@REM
@REM Copyright (C) 2019, Northrop Grumman Systems Corporation
@REM All Rights Reserved.
@REM
@REM NOTICE:  All information contained herein is, and remains the property of
@REM Northrop Grumman Systems Corporation. The intellectual and technical concepts
@REM contained herein are proprietary to Northrop Grumman Systems Corporation and
@REM may be covered by U.S. and Foreign Patents or patents in process, and are
@REM protected by trade secret or copyright law. Dissemination of this information
@REM or reproduction of this material is strictly forbidden unless prior written
@REM permission is obtained from Northrop Grumman.
@REM

@echo off
rem Run this script to start the framework.  This script usually doesn't need
rem to be modified.  Set any custom environment variables in setenv.bat.

set CWD=%~dp0

rem Set environment.
for %%? in ("%~dp0..") do set NG_FW_HOME=%%~f?

rem The system properties that are set when the JVM is started.
set FRAMEWORK_OPTS=-DNG_FW_HOME="%NG_FW_HOME%" -Dgov.nasa.worldwind.app.config.document="%NG_FW_HOME%/resources/config/app/map/worldwind.xml" -Djavax.xml.accessExternalSchema=all -Dfelix.config.properties="file:%NG_FW_HOME%/platform/configuration/config.properties"

rem The arguments passed to Felix when it is started.
set FELIX_OPTS="%NG_FW_HOME%/platform/cache" -b "%NG_FW_HOME%/platform"

rem Require JAVA_HOME to be set.
if "%JAVA_HOME%" == "" goto :javaHomeNotSet

rem Require Java 1.8
for /f tokens^=2-5^ delims^=.-_^" %%j in ('"%JAVA_HOME%/bin/java" -fullversion 2^>^&1') do set "jver=%%j%%k%%l%%m"
if %jver% LSS 18000 goto :javaVersionError


"%JAVA_HOME%/bin/java" %FRAMEWORK_OPTS% -jar "%NG_FW_HOME%/platform/org.apache.felix.main-5.6.1.jar" %FELIX_OPTS%
@rem echo "%JAVA_HOME%/bin/java" %FRAMEWORK_OPTS% -jar "%NG_FW_HOME%/felix-platform/felix.jar" %FELIX_OPTS%
goto :eof

:javaHomeNotSet
echo The required environment variable JAVA_HOME is not set!
goto :eof

:javaVersionError
echo Java 1.8 is required for BLoCS!
goto :eof