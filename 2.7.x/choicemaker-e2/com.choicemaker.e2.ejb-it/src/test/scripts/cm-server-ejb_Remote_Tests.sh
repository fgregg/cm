#!/bin/sh
BASE="`dirname $0`"
POM_DIR="$BASE/../../.."
POM="$POM_DIR/pom.xml"
echo
echo "   BASE: $BASE"
echo "POM_DIR: $POM_DIR"
echo "    POM: $POM"
if [ -f "$POM" ] ;
then
 echo 
 start="`date`" 
 for p in arquillian-jbossas72-remote
 do
   date 
   echo $p 
   time mvn -f "$POM" clean test -P $p 2>/tmp/${p}.err | tee /tmp/${p}.log | grep -A 1 "Tests in error\|Tests run" 
   echo  
 done 
 finish="`date`" 
 echo " Start: $start"
 echo "Finish: $finish"
 echo
else
 echo "Missing file: $POM"
fi
