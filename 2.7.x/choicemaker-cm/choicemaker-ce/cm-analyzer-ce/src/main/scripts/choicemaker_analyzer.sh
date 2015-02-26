#!/bin/sh
#
# 2010-04-21 rphall
# Bash script to start CM Analyzer.

# Tweak the JAVA variable to point at a 1.4.2 JDK or JRE.
# Later or earlier JVM's won't work.
# See http://java.sun.com/products/archive
#
#JAVA="/mnt/sda5/usr/java/j2sdk1.4.2_19/bin/java"
JAVA="/usr/java/j2sdk1.4.2_19/bin/java"

# Recommended memory allocations
JAVA_OPTS="-Xms384M -Xmx512M"

# Uncomment the following line to enable Eclipse 2.1.3 remote debugging
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=28787,server=y,suspend=y"

# The sample modeling project from SourceForge
# Comment this line out if you want to use the ChoiceMaker launchpad
# to browse for an Analyzer configuration file
CONF="-conf projects/simple_person_matching/project.xml"

$JAVA $JAVA_OPTS -cp startup.jar org.eclipse.core.launcher.Main \
  -application com.choicemaker.cm.modelmaker.ModelMaker \
  -noupdate \
  -guiErrorMessages \
	$CONF

