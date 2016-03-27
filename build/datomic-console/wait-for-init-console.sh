#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

$dirname/wait-for-init-base.sh

>&2 echo "Executing console"
bin/console -p 8080 postgres 'datomic:sql://datomic?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic'
