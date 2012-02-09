@ECHO OFF

REM Taverna startup script

REM This contains a special Vertical tab character
REM Do not edit with an editor that will remove that character

REM distribution directory
set TAVERNA_HOME=%~dp0

REM Convert arguement into Taverna format to handle spaces
set VMARGS=
set /a COUNT=0
:Loop
    if [%1]==[] goto Continue
    set TEMP=%1
    set TEMP=%TEMP: =%
    set VMARGS=%VMARGS%-Dtaverna.commandline.arg.%COUNT%=%TEMP%
    set /a COUNT = COUNT +1
    shift
    goto Loop
:Continue

set VMARGS=%VMARGS%-Dtaverna.commandline.args=%COUNT%
set VMARGS=%VMARGS% -Dtaverna.app.startup=%TAVERNA_HOME%..
set VMARGS=%VMARGS% -Xmx300m -XX:MaxPermSize=140m
REM strip out any quotes and then quote the whole thing
set vmargs="%vmargs:"=%"

java -jar "%TAVERNA_HOME%pax-runner-1.7.0.jar" --vmOptions=%vmargs% --cp="%TAVERNA_HOME%..\config" --args="file:%TAVERNA_HOME%..\config\runner.args" --workingDirectory="%TAVERNA_HOME%..\runner"  scan-dir:"%TAVERNA_HOME%..\lib"
