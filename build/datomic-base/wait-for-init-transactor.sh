#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`
transactor=$1

sed -i.bak "s/host=0.0.0.0/host=${transactor}/g" config/sql-transactor.properties

$dirname/wait-for-init-base.sh

>&2 echo "Executing transactor $transactor daemon"
$dirname/daemon-transactor.sh &

>&2 echo "Executing peer"
bin/rest -p 8001 postgres 'datomic:sql://datomic-tester?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic'
