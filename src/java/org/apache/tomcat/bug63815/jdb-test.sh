#!/bin/sh

# Adjust classpath appropriately

# First calling Java directly
java "-Dfoo=b a" -classpath ../../../../../../build/classes org.apache.tomcat.bug63815.EchoArgs

# Then try jdb
# JDB will start should be able to type run and see same results as with Java
jdb "-Dfoo=b a" -classpath ../../../../../../build/classes org.apache.tomcat.bug63815.EchoArgs