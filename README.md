# docker-datomic
Docker musings for a Datomic installation and jepsen test automation.

## Quickstart

```
$ make
$ make test
```

See [Makefile](./Makefile) for details.

## Prerequisites
- Place your Datomic credentials for downloading the software in the file 'priv/.credentials'.
- Place your Datomic license key in the file 'priv/.license-key'.

## Resources
- http://www.datomic.com
- https://github.com/aphyr/jepsen
- https://github.com/aphyr/jepsen/blob/master/zookeeper/src/jepsen/zookeeper.clj (Sample Jepsen test for Zookeeper)
- https://github.com/aphyr/jepsen/blob/master/doc/scaffolding.md (Tutorial for writing a Jepsen test)
- https://github.com/norton/docker-machine

## Prerequisites (Mac OS X)

- Install and setup [homebrew](http://brew.sh).
- Install virtualbox, docker, docker-compose, and docker-machine.

```
$ brew cask install virtualbox
$ brew install docker docker-compose docker-machine
```

- Create your own 'dev' docker machine.

```
$ git clone https://github.com/norton/docker-machine.git
$ cd docker-machine
$ make
```

- Add 'dev' docker machine to your environment.

```
$ eval "$(docker-machine env dev)"
```

## ToDo
- Add checksums for curl downloads.
- Figure out persistent storage for test logs (i.e. Test results are stored under the var/tester-data directory.) in 'docker-compose.yml7.
- Implement connections to jepsen.datomic test.
- Implement read, write, and cas operations to jepsen.datomic test.
- Implement model checker to jepsen.datomic test.
- Implement performance checker to jepsen.datomic test.
- Implement a "chaos monkey" approach for killing peer containers to jepsen.datomic test.
- Implement a "chaos monkey" approach for creating and healing network partitions on peer containers to jepsen.datomic test.
- Add transactors (with embedded peers) to jepsen.datomic test.
- Add console (with embedded peer) to jepsen.datomic test.
- Add postgres (with embedded peer) to jepsen.datomic test.
- BONUS ... implement a postgres "active" and "standby" node to jepsen.datomic test.
