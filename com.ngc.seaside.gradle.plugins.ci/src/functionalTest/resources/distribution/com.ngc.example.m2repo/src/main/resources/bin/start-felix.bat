@REM
@REM UNCLASSIFIED
@REM
@REM Copyright 2020 Northrop Grumman Systems Corporation
@REM
@REM Permission is hereby granted, free of charge, to any person obtaining a copy of
@REM this software and associated documentation files (the "Software"), to deal in
@REM the Software without restriction, including without limitation the rights to use,
@REM copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
@REM Software, and to permit persons to whom the Software is furnished to do so,
@REM subject to the following conditions:
@REM
@REM The above copyright notice and this permission notice shall be included in
@REM all copies or substantial portions of the Software.
@REM
@REM THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
@REM INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
@REM PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
@REM HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
@REM OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
@REM SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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