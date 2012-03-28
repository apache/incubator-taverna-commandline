@ECHO OFF

REM Taverna startup script

REM distribution directory
set TAVERNA_HOME=%~dp0


REM 300 MB memory, 140 MB for classes
set ARGS=-Xmx300m -XX:MaxPermSize=140m

REM Taverna system properties
set ARGS=%ARGS% "-Dlog4j.configuration=file://%TAVERNA_HOME%conf/log4j.properties"
set ARGS=%ARGS% "-Djava.util.logging.config.file=%TAVERNA_HOME%conf/logging.properties"
set ARGS=%ARGS% "-Dtaverna.app.startup=%TAVERNA_HOME%."

java %ARGS% -jar "%TAVERNA_HOME%lib\taverna-command-line-0.0.1-SNAPSHOT.jar" %*
