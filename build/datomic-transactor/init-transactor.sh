#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`
host="postgres"
user="postgres"
pass="postgres"

until PGPASSWORD="$pass" psql -h "$host" -U "$user" -c '\l'; do
    >&2 echo "Postgres is unavailable - sleeping"
    sleep 0.1
done

>&2 echo "Postgres is up - initializing"
PGPASSWORD="$pass" psql -f bin/sql/postgres-db.sql -h "$host" -U "$user"
PGPASSWORD="$pass" psql -f bin/sql/postgres-table.sql -h "$host" -U "$user" -d datomic
PGPASSWORD="$pass" psql -f bin/sql/postgres-user.sql -h "$host" -U "$user" -d datomic

exec $dirname/wait-for-init-transactor.sh $@
