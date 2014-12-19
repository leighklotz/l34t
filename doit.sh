#!/bin/sh

export CLASSPATH=$HOME/java/SaxonHE9/saxon9he.jar:classes

rm -rf classes && mkdir classes
javac -d classes -Xlint:deprecation L34T.java || exit

java org.l34t.L34T L34T.xml instance.xml x int 3 pizza string fred
