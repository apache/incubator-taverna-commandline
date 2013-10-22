#!/bin/sh

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

# 1 GB memory, 400 MB for classes
exec "$javabin" -Xmx1g -XX:MaxPermSize=400m \
  "-Dlog4j.configuration=file://$taverna_home/conf/log4j.properties " \
  "-Djava.util.logging.config.file=$taverna_home/conf/logging.properties " \
  "-Dtaverna.app.startup=$taverna_home" \
  -jar "$taverna_home/lib/uk.org.taverna.commandline/taverna-command-line-0.1.2-SNAPSHOT.jar" \
  ${1+"$@"}
