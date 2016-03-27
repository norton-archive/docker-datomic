# docker-datomic
Docker musings for a Datomic installation and jepsen test automation.

## Quickstart

1. Execute `make`
2. Execute `docker-compose logs tester` to view the output of lein test.

Test results are stored under the var/tester-data directory.

Execute `docker-compose run tester lein test` to repeat the test.

See [Makefile](./Makefile) for details.

## Prerequisites
- Place your Datomic credentials for downloading the software in the file 'priv/.credentials'.
- Place your Datomic license key in the file 'priv/.license-key'.

## Prerequisites (Mac OS X)
- https://github.com/norton/docker-machine

## Resources
- http://www.datomic.com
- https://github.com/aphyr/jepsen
