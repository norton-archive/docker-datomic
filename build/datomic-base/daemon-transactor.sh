#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

while true
do
    >&2 echo "Executing transactor $transactor"
    bin/transactor config/sql-transactor.properties || true

    $dirname/wait-for-init-base.sh
done
