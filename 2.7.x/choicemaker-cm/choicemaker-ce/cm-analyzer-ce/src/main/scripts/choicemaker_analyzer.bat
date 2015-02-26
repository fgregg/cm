REM 2010-04-21 rphall
REM Win32 script to start CM Analyzer.

REM Tweak the JAVA variable to point at a 1.4.2 JDK or JRE.
REM Later or earlier JVM's won't work.
REM See http://java.sun.com/products/archive
REM
REM set JAVA=/mnt/sda5/usr/java/j2sdk1.4.2_19/bin/java
set JAVA=C:/j2sdk1.4.2_19/bin/java.exe

REM Recommended memory allocations
set JAVA_OPTS=-Xms384M -Xmx512M

REM The sample modeling project from SourceForge
REM Uncomment this line out if you don't want to use the ChoiceMaker launchpad
REM to browse for an Analyzer configuration file
set CONF=-conf projects/simple_person_matching/project.xml

%JAVA% %JAVA_OPTS% -cp startup.jar org.eclipse.core.launcher.Main -application com.choicemaker.cm.modelmaker.ModelMaker -noupdate -guiErrorMessages %CONF%

