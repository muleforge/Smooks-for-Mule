@echo off
setlocal
REM There is no need to call this if you set the MULE_HOME in your environment properties
if "%MULE_HOME%" == "" SET MULE_HOME=..\..
if "%MULE_BASE%" == "" SET MULE_BASE=%MULE_HOME%

REM Any changes to the files in .\conf will take precedence over those deployed to %MULE_HOME%\lib\user
SET MULE_LIB=.\conf

copy /Y target\mule-smooks-example-edi2java-1.0.jar "%MULE_BASE%\lib\user"

call "%MULE_BASE%\bin\mule.bat" -config .\conf\mule-edi2java-config.xml
