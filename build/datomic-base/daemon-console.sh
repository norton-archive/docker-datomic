#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

while true
do
    >&2 echo "Executing console"
    bin/console -p 8080 postgres 'datomic:sql://datomic-tester?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic' || true

    $dirname/wait-for-init-base.sh
done
