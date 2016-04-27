@ECHO OFF

REM Taverna startup script

REM distribution directory
set TAVERNA_HOME=%~dp0


REM 1 GB memory
set ARGS=-Xmx1g

REM Taverna system properties
set ARGS=%ARGS% "-Dlog4j.configuration=file:///%TAVERNA_HOME%conf/log4j.properties"
set ARGS=%ARGS% "-Djava.util.logging.config.file=%TAVERNA_HOME%conf/logging.properties"
set ARGS=%ARGS% "-Dtaverna.app.startup=%TAVERNA_HOME%."

set JAR="%TAVERNA_HOME%lib/org.apache.taverna.commandline/taverna-commandline-launcher-3.1.0-incubating.jar"
REM find *.launcher jar
for %%f in ("%TAVERNA_HOME%lib\org.apache.taverna.commandline\taverna-commandline-launcher*.jar") DO set JAR="%%f"

java %ARGS% -jar "%TAVERNA_HOME%lib/org.apache.taverna.commandline/taverna-commandline-launcher-3.1.0-incubating-SNAPSHOT.jar" %*
