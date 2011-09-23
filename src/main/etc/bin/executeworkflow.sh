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

vmargs=''
i=0

for arg
do
	arg=`echo "$arg" | tr '\ ' '\013'`
	vmargs+="-Dtaverna.commandline.arg.$i=$arg "
	i=`expr $i + 1`
done

vmargs+=-Dtaverna.commandline.args=$i
vmargs+=" -Xmx300m -XX:MaxPermSize=140m"

exec "$javabin" -jar $taverna_home/pax-runner-1.7.0.jar --vmOptions="$vmargs" --cp=$taverna_home/../config --args=file:$taverna_home/../config/runner.args --workingDirectory=$taverna_home/../runner scan-dir:$taverna_home/../lib
