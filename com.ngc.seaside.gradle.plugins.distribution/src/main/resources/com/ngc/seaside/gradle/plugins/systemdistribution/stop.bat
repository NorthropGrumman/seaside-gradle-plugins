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
for /d %%D in ("%~dp0\*") DO (
 echo Stopping "%%~nD" ...
 wmic process where "commandline like '%%%%~nD%%'" call terminate > nul
)
