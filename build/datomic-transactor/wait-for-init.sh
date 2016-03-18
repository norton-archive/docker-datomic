#!/bin/bash

set -e

host="postgres"
user="datomic"
pass="datomic"

until PGPASSWORD="$pass" psql -h "$host" -U "$user" -c '\l'; do
    >&2 echo "Postgres is unavailable - sleeping"
    sleep 0.1
done

transactor=$1
sed -i.bak "s/host=0.0.0.0/host=${transactor}/g" config/sql-transactor.properties

>&2 echo "Postgres is up - executing transactor $transactor"
exec bin/transactor config/sql-transactor.properties
