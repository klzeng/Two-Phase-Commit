#!/bin/bash

args=("$@")
java -cp . -Djava.security.policy=client.policy Client ${args[0]} ${args[1]} < input
