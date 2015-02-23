#!/bin/sh
#
# 2014-11-29 rphall
# Bash script to start CM Analyzer as an embedded E2 application.
# Tweak the JAVA variable to point at a 1.7 JRE or JDK.
# Earlier JVMs won't work, and later JVMs haven't been tested.
# See http://java.sun.com/products/archive
#
APP_DIR="`dirname "$0"`"
LIB_DIR="$APP_DIR/lib"
APP="com.choicemaker.cm.util.app.ModelArtifactBuilderApp"

# Java command (Java 1.7 is required)
#JAVA="/usr/java/jdk1.7.0_55/bin/java"
JAVA=${JAVA_HOME}/bin/java

# Recommended memory allocations
JAVA_OPTS="-Xms384M -Xmx512M"

# Logging configuration
JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.config.file=logging.properties"

# Embedded E2 settings
JAVA_OPTS="$JAVA_OPTS -DcmInstallablePlatform=com.choicemaker.e2.embed.EmbeddedPlatform"
JAVA_OPTS="$JAVA_OPTS -DcmInstallableConfigurator=com.choicemaker.cm.core.xmlconf.XmlConfigurator"

# Uncomment the following line to enable Eclipse 2.1.3 remote debugging
#JAVA_OPTS="$JAVA_OPTS -debug"
JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=38787,server=y,suspend=y"

# Parse the command line
if [[ $# == 0 ]]; then
	echo
	echo "Usage: $0 <conf-file>" 2>&1
	echo
  exit 1
elif [[ $# == 1 ]]
then
	CONF="$1"
else
	echo
	echo "Usage: $0 <conf-file>" 2>&1
	echo
  exit 1
fi

if [ ! -f "$CONF" ]
then
  echo
  echo "ERROR: Configuration file '"$CONF"' doesn't exit or isn't a file." 1>&2
  echo
  exit 1
fi

echo
echo "Using configuration file: '"$CONF"'"
if [[ $# > 2 ]]; then
	echo "Ignoring extra arguments"
fi

# Compute the classpath
if [ ! -d "$LIB_DIR" ]
then
  echo
  echo "ERROR: Missing library directory (${LIB_DIR})"
  echo
  exit 1
fi
CP=""
for f in "${LIB_DIR}"/*.jar ; do
  CP="${CP}${f}:"
done
# Remove the trailing ':'
CP="`echo $CP | sed -e "s%\(.*\):$%\1%"`"
if [ "x$CP" = "x" ]
then
  echo
  echo "ERROR: No JAR files in library directory (${LIB_DIR})"
  echo
  exit 1
fi

# Compute, display and evaluate the command to start CM Analyzer
CMD="$JAVA $JAVA_OPTS -cp $CP $APP -conf \"$CONF\""
echo
echo "$CMD"
echo
eval "$CMD"

