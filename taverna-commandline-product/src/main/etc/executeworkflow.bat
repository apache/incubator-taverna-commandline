@ECHO OFF

REM Taverna startup script

REM distribution directory
set TAVERNA_HOME=%~dp0


REM 1 GB memory, 400 MB for classes
set ARGS=-Xmx1g -XX:MaxPermSize=400m

REM Taverna system properties
set ARGS=%ARGS% "-Dlog4j.configuration=file:///%TAVERNA_HOME%conf/log4j.properties"
set ARGS=%ARGS% "-Djava.util.logging.config.file=%TAVERNA_HOME%conf/logging.properties"
set ARGS=%ARGS% "-Dtaverna.app.startup=%TAVERNA_HOME%."

java %ARGS% -jar "%TAVERNA_HOME%lib\/lib/org.apache.taverna.commandline/taverna-commandline-launcher-3.1.0.incubating-SNAPSHOT.jar" %*
