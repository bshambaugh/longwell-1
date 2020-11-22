#!/bin/sh

chmod u+x ./tools/bin/antRun
chmod u+x ./tools/bin/ant

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$TERM" = "cygwin" ] ; then
  S=';'
else
  S=':'
fi

# ----- Ignore system CLASSPATH variable
OLD_CLASSPATH="$CLASSPATH"
unset CLASSPATH
CLASSPATH="`echo lib/endorsed/*.jar | tr ' ' $S`"
export CLASSPATH

# ----- Use Ant shipped. Ignore installed in the system Ant
OLD_ANT_HOME="$ANT_HOME"
export ANT_HOME=tools
OLD_ANT_OPTS="$ANT_OPTS"
#export ADDITIONAL_ANT_OPTS="-listener net.sf.antcontrib.perf.AntPerformanceListener"
export ANT_OPTS="-Xms32M -Xmx512M -Djava.endorsed.dirs=lib/endorsed"

"$ANT_HOME/bin/ant" -logger org.apache.tools.ant.DefaultLogger -emacs $ADDITIONAL_ANT_OPTIONS $@

# ----- Restore ANT_HOME and ANT_OPTS
export ANT_HOME="$OLD_ANT_HOME"
unset OLD_ANT_HOME
export ANT_OPTS="$OLD_ANT_OPTS"
unset OLD_ANT_OPTS

# ----- Restore CLASSPATH
export CLASSPATH="$OLD_CLASSPATH"
unset OLD_CLASSPATH
