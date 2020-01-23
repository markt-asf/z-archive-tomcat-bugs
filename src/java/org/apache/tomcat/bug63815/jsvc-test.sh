#!/bin/sh

JAVA_OPTS="\"-Dfoo=b a\" -Dfoo2=bar"

# First calling Java directly
#eval exec java "$JAVA_OPTS" -classpath ../../../../../../build/classes:/home/mark/libs/commons-daemon-1.2.2/commons-daemon-1.2.2.jar org.apache.tomcat.bug63815.EchoArgs

# Then try jsvc
eval exec ./jsvc -java-home /opt/java/adopt-jdk-08.222_10 \
       -nodetach \
       -outfile "\"&1\"" \
       -errfile "\"&2\"" \
       -pidfile /tmp/jsvc-test.pid \
       "$JAVA_OPTS" \
       -classpath /home/mark/repos/gh-tomcat-bugs/build/classes:/home/mark/libs/commons-daemon-1.2.2/commons-daemon-1.2.2.jar \
        org.apache.tomcat.bug63815.EchoArgs
       