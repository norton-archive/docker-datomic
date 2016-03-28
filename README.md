# docker-datomic
Docker musings for a Datomic installation and jepsen test automation.

## Quickstart

```
make
make test
```
See [Makefile](./Makefile) for details.

## Prerequisites
- Place your Datomic credentials for downloading the software in the file 'priv/.credentials'.
- Place your Datomic license key in the file 'priv/.license-key'.

## Prerequisites (Mac OS X)
- https://github.com/norton/docker-machine

## Resources
- http://www.datomic.com
- https://github.com/aphyr/jepsen

## ToDo
- Add checksums for curl downloads
- Figure out persistent storage for test logs (i.e. Test results are stored under the var/tester-data directory.).
