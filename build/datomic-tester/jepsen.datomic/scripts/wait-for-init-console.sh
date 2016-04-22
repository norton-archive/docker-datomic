#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

$dirname/wait-for-init-base.sh

>&2 echo "Executing console daemon"
$dirname/daemon-console.sh &

>&2 echo "Executing noop"
tail -f /dev/null
