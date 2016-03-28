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

PGPASSWORD="$pass" psql -c "UPDATE pg_database SET datallowconn = 'false' WHERE datname = 'datomic';" -h "$host" -U "$user"
PGPASSWORD="$pass" psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'datomic';" -h "$host" -U "$user"
PGPASSWORD="$pass" psql -c "DROP ROLE datomic;" -h "$host" -U "$user" || true
PGPASSWORD="$pass" psql -c "DROP TABLE datomic_kvs;" -h "$host" -U "$user" || true
if PGPASSWORD="$pass" psql -lqtA -h "$host" -U "$user" | grep -q "^datomic"; then
    until PGPASSWORD="$pass" psql -c "DROP DATABASE datomic;" -h "$host" -U "$user"; do
        >&2 echo "Postgres DATABASE datomic is busy - sleeping"
        sleep 0.1
    done
fi
