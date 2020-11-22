#!/bin/sh
# Configuration variables
#
# JAVA_HOME
#   Home of Java installation.

if [ "$TERM" = "cygwin" ] ; then
  S=';'
else
  S=':'
fi

usage()
{
    echo "Usage: $0 (action)"
    echo "actions:"
    echo "  LoadDb    Load RDF data into a database"
    exit 1
}

[ $# -gt 0 ] || usage

TOOL=$1
shift
ARGS="$*"

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

if [ "$LONGWELL_TOOLS" = "" ] ; then
  LONGWELL_TOOLS="build/longwell-tools.jar"
fi

if [ ! -f $LONGWELL_TOOLS ] ; then
  echo "You must build the tool .jar's first by running ./build.sh tools"
  exit 1
fi

# ----- Local variables
CP="`echo lib/*.jar lib/**/*.jar $LONGWELL_TOOLS build/*.jar | tr ' ' $S`"
JAVA="$JAVA_HOME/bin/java"
JAVA_OPTS="-Xmx512m"

$JAVA $JAVA_OPTS -cp $CP edu.mit.simile.tools.$TOOL $ARGS
