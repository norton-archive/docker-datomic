#!/bin/bash

set -e

host="postgres"
user="datomic"
pass="datomic"

if [ ! -f /root/.initialized-base ];
then
    /etc/init.d/ssh stop
    rm -f /etc/ssh/ssh_host_*
    sed -i.bak "s/exit 101/exit 0/g" /usr/sbin/policy-rc.d
    dpkg-reconfigure openssh-server
    sed -i.bak "s/exit 0/exit 101/g" /usr/sbin/policy-rc.d
    touch /root/.initialized-base
else
    /etc/init.d/ssh start || true
fi

until PGPASSWORD="$pass" psql -h "$host" -U "$user" -c 'SELECT NULL;'; do
    >&2 echo "Datomic Postgres is unavailable - sleeping"
    sleep 0.1
done

>&2 echo "Datomic Postgres is up"
