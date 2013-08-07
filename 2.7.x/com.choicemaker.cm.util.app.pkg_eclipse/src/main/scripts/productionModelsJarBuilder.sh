#!/bin/sh
#
# 2013-08-01 rphall
# Bash script to start ProductionModelsJarBuilder 
# Tweak the JAVA variable to point at a 1.4.2 JDK or JRE.
# Later or earlier JVM's won't work.
# See http://java.sun.com/products/archive
#
APP_DIR="`dirname "$0"`"
LAUNCHER="org.eclipse.core.launcher.Main"
STARTUP_JAR="$APP_DIR/startup.jar"
APP="com.choicemaker.cm.util.app.ProductionModelsJarBuilderApp"
LOG4J="log4j.properties"
WORKSPACE="/tmp"

#XCP="/usr/java/j2sdk1.4.2_19/jre/lib/charsets.jar"
#XCP="/usr/java/j2sdk1.4.2_19/jre/lib/jce.jar:${XCP}"
#XCP="/usr/java/j2sdk1.4.2_19/jre/lib/jsse.jar:${XCP}"
#XCP="/usr/java/j2sdk1.4.2_19/jre/lib/plugin.jar:${XCP}"
#XCP="/usr/java/j2sdk1.4.2_19/jre/lib/sunrsasign.jar:${XCP}"
#XCP="/usr/java/j2sdk1.4.2_19/jre/lib/rt.jar:${XCP}"
#XBOOTCLASSPATH="-Xbootclasspath:$XCP"

# Java command
#JAVA="/usr/java/j2sdk1.4.2_19/bin/java"
#JAVA="/usr/java/jdk1.7.0_13/bin/java"
JAVA=/usr/bin/java

# Recommended memory allocations
JAVA_OPTS="-Xms384M -Xmx512M"

# Uncomment the following line to enable Eclipse 2.1.3 remote debugging
#JAVA_OPTS="$JAVA_OPTS -debug"
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=28787,server=y,suspend=y"

# Parse the command line
if [[ $# == 0 ]]; then
	echo
	echo "Usage: $0 <conf-file> [<output-dir>]" 2>&1
	echo
  exit 1
elif [[ $# == 1 ]]
then
	CONF="$1"
  OUTDIR="`pwd`"
else
	CONF="$1"
  OUTDIR="$2"
fi

if [ ! -f "$CONF" ]
then
  echo
  echo "ERROR: Configuration file '"$CONF"' doesn't exit or isn't a file." 1>&2
  echo
  exit 1
fi
if [ ! -d "$OUTDIR" ]
then
  echo
  echo "ERROR: '" $OUTDIR "' doesn't exist or isn't a directory" 1>&2
  echo
	exit 1
fi

echo
echo "Using configuration file: '"$CONF"'"
echo "Using output directory: '"$OUTDIR"'"
if [[ $# > 2 ]]; then
	echo "Ignoring extra arguments"
fi

CMD="$JAVA $JAVA_OPTS \\
      -Dlog4j.configuration=$LOG4J \\
      -cp \"$STARTUP_JAR\" \"$LAUNCHER\" \\
      -data $WORKSPACE \\
      -noupdate \\
      -application \"$APP\" \\
      -conf \"$CONF\" \\
      -outDir \"$OUTDIR\""

echo
echo "$CMD"
echo
eval "$CMD"

