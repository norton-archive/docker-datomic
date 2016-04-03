#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

$dirname/wait-for-init-base.sh

>&2 echo "Executing peer"
/opt/datomic-pro/bin/rest -p 8001 postgres 'datomic:sql://?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic'
