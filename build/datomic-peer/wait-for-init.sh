#!/bin/bash

set -e

host="postgres"
user="datomic"
pass="datomic"

until PGPASSWORD="$pass" psql -h "$host" -U "$user" -c '\l'; do
    >&2 echo "Postgres is unavailable - sleeping"
    sleep 0.1
done

>&2 echo "Postgres is up - executing rest"
bin/rest -p 8001 postgres 'datomic:sql://datomic?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic'
