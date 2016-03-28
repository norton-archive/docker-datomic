#!/bin/bash

set -e

dirname=`dirname $(realpath $0)`

$dirname/drop-tester.sh
$dirname/init-tester.sh
