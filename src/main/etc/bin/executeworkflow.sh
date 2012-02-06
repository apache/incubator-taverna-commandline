#!/bin/sh

set -e

## resolve links - $0 may be a symlink
prog="$0"

real_path() {
    readlink -m "$1" 2>/dev/null || python -c 'import os,sys;print os.path.realpath(sys.argv[1])' "$1"
}

realprog=`real_path "$prog"`
taverna_startup=`dirname "$realprog"`
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
vmargs+=" -Dtaverna.app.startup=$taverna_startup/.."
vmargs+=" -Xmx300m -XX:MaxPermSize=140m"

exec "$javabin" -jar $taverna_startup/pax-runner-1.7.0.jar --vmOptions="$vmargs" --cp=$taverna_startup/../config --args=file:$taverna_startup/../config/runner.args --workingDirectory=$taverna_startup/../runner scan-dir:$taverna_startup/../lib
