#!/bin/sh

##  Licensed to the Apache Software Foundation (ASF) under one or more
##  contributor license agreements.  See the NOTICE file distributed with
##  this work for additional information regarding copyright ownership.
##  The ASF licenses this file to You under the Apache License, Version 2.0
##  (the "License"); you may not use this file except in compliance with
##  the License.  You may obtain a copy of the License at
##
##  http://www.apache.org/licenses/LICENSE-2.0
##
##  Unless required by applicable law or agreed to in writing, software
##  distributed under the License is distributed on an "AS IS" BASIS,
##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##  See the License for the specific language governing permissions and
##  limitations under the License.


set -e

## resolve links - $0 may be a symlink
prog="$0"

real_path() {
    readlink -m "$1" 2>/dev/null || python -c 'import os,sys;print os.path.realpath(sys.argv[1])' "$1"
}

realprog=`real_path "$prog"`
taverna_home=`dirname "$realprog"`
javabin=java
if test -x "$JAVA_HOME/bin/java"; then
    javabin="$JAVA_HOME/bin/java"
fi

# 1 GB memory
exec "$javabin" -Xmx1g \
  "-Dlog4j.configuration=file://$taverna_home/conf/log4j.properties " \
  "-Djava.util.logging.config.file=$taverna_home/conf/logging.properties " \
  "-Dtaverna.app.startup=$taverna_home" \
  -jar "$taverna_home/lib/org.apache.taverna.commandline/taverna-commandline-launcher-3.1.0-incubating-SNAPSHOT.jar" \
  ${1+"$@"}
