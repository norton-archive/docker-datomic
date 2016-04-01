#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

$dirname/wait-for-init-base.sh

>&2 echo "Executing console daemon"
$dirname/daemon-console.sh &

>&2 echo "Executing peer"
bin/rest -p 8001 postgres 'datomic:sql://datomic-tester?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic'
