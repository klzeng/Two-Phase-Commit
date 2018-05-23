#!/bin/bash

java -cp  ".:sqlite-jdbc-3.21.0.jar" -Djava.security.policy=server.policy TPCServer 1100
