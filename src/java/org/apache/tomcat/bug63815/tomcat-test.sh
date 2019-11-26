#!/bin/sh

set -x

#JAVA_OPTS="-Dfoo3=\"0 0 * * ?\""

#eval exec java \
#          "$JAVA_OPTS" \
#          -cp /home/mark/repos/gh-tomcat-bugs/build/classes org.apache.tomcat.bug63815.EchoArgs \
#          "$@" \
#          start
          

JAVA_OPTS="-Dfoo=\"b a\" -Dbar=la"
          
eval exec java \
          "$JAVA_OPTS" \
          -classpath /home/mark/repos/gh-tomcat-bugs/build/classes \
          org.apache.tomcat.bug63815.EchoArgs