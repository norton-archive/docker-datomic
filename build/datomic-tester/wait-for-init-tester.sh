#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

$dirname/wait-for-init-base.sh

>&2 echo "Executing tester"
java -jar app-standalone.jar
