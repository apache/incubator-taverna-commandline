@ECHO OFF

REM Taverna startup script

REM distribution directory
set TAVERNA_HOME=%~dp0

java -jar "%TAVERNA_HOME%pax-runner-1.7.5.jar"
