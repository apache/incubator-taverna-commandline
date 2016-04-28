@ECHO OFF
REM   Licensed to the Apache Software Foundation (ASF) under one or more
REM   contributor license agreements.  See the NOTICE file distributed with
REM   this work for additional information regarding copyright ownership.
REM   The ASF licenses this file to You under the Apache License, Version 2.0
REM   (the "License"); you may not use this file except in compliance with
REM   the License.  You may obtain a copy of the License at
REM
REM   http://www.apache.org/licenses/LICENSE-2.0
REM
REM   Unless required by applicable law or agreed to in writing, software
REM   distributed under the License is distributed on an "AS IS" BASIS,
REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM   See the License for the specific language governing permissions and
REM   limitations under the License.


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
REM echo %JAR%
java %ARGS% -jar %JAR% %*
