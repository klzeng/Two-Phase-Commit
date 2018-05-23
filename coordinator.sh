#!/bin/bash

rm *.class


javac -cp . *.java
java -cp  ".:sqlite-jdbc-3.21.0.jar" -Djava.security.policy=server.policy Coordinator 1100
