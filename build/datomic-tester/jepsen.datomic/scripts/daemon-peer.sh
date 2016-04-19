#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

while true
do
    >&2 echo "Executing peer"
    /opt/datomic-pro/bin/rest -p 8001 postgres 'datomic:sql://?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic' || true

    $dirname/wait-for-init-base.sh
done
