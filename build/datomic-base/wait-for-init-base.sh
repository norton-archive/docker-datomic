#!/bin/bash

set -e

host="postgres"
user="datomic"
pass="datomic"

until PGPASSWORD="$pass" psql -h "$host" -U "$user" -c '\l'; do
    >&2 echo "Datomic Postgres is unavailable - sleeping"
    sleep 0.1
done

>&2 echo "Datomic Postgres is up"
