#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`
host="postgres"
user="postgres"
pass="postgres"

until PGPASSWORD="$pass" psql -h "$host" -U "$user" -c 'SELECT NULL;'; do
    >&2 echo "Postgres is unavailable - sleeping"
    sleep 0.1
done

# TODO remove hard-coded from n1 to n3 (and eventually n7)
ssh-keyscan -t rsa n1 n2 n4 n5 n7 > /root/.ssh/known_hosts

>&2 echo "Executing tester"
exec $@
