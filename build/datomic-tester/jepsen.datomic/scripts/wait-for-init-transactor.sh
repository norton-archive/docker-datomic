#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`
transactor=$1

sed -i.bak "s/host=0.0.0.0/host=${transactor}/g" /opt/datomic-pro/config/sql-transactor.properties

$dirname/wait-for-init-base.sh

>&2 echo "Executing transactor $transactor daemon"
$dirname/daemon-transactor.sh &

>&2 echo "Executing peer"
/opt/datomic-pro/bin/rest -p 8001 postgres 'datomic:sql://?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic'
