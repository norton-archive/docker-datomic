#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

$dirname/wait-for-init-base.sh

# TODO remove hard-coded from n1 to n3 (and eventually n7)
ssh-keyscan -t rsa n1 n2 n3 > /root/.ssh/known_hosts

>&2 echo "Executing tester"
exec $@
